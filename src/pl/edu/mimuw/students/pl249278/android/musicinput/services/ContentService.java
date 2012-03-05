package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig.DisplayMode;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfigFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class ContentService extends AsynchronousRequestsService {
	static final String TAG = ContentService.class.getName();

	private DbHelper mDb;

	public ContentService() {
		super(ContentService.class.getSimpleName());
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mDb = new DbHelper(this);
	}
	
	@Override
	public void onDestroy() {
		mDb.close();
		super.onDestroy();
	}
	
	public static class ACTIONS {
		public static final String INSERT_SCORE = ContentService.class.getName()+".insert_score";
		public static final String EXTRAS_SCORE = "score";
		public static final String EXTRAS_SCORE_VISUAL_CONF = "score_vis_conf";
		/** inserted entity id (of type long) */
		public static final String RESPONSE_EXTRAS_ENTITY_ID = "entity_id";
		
		public static String GET_SCORE_BY_ID = ContentService.class.getName()+".get_score";
		public static final String EXTRAS_ATTACH_SCORE_VISUAL_CONF = "attach_vis_conf";
		/** requested entity id (of type long) */
		public static final String EXTRAS_ENTITY_ID = "entity_id";
		public static final String RESPONSE_EXTRAS_ENTITY = "entity";
		public static final String RESPONSE_EXTRAS_VISUAL_CONF = "vis_conf";
		
		/** 
		 * Saves a "session" backup copy of score. 
		 * Session === lifetime of application process.
		 * It will be removed when successful SAVE or DISCARD on original score is performed
		 * during same session. 
		 * Following COPY request during same session will overwrite previous copy.
		 * Parameters: {@link #EXTRAS_SCORE}, {@link #EXTRAS_SCORE_VISUAL_CONF}
		 */
		public static final String SAVE_SCORE_COPY = ContentService.class.getName()+".score_copy";
		/** 
		 * Updates Score.
		 * Discards "session" backup.
		 * Parameters: {@link #EXTRAS_SCORE}, {@link #EXTRAS_SCORE_VISUAL_CONF} (optional)
		 */
		public static final String UPDATE_SCORE = ContentService.class.getName()+".update_score";
		/**
		 * Removes session copy.
		 * Parameters: {@link #EXTRAS_ENTITY_ID} - original id
		 */
		public static final String CLEAN_SCORE_SESSION_COPY = ContentService.class.getName()+".clean_copy";
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.INSERT_SCORE.equals(action)) {
			insertScore(requestIntent);
		} else if(ACTIONS.GET_SCORE_BY_ID.equals(action)) {
			findScore(requestIntent);
		} else if(ACTIONS.SAVE_SCORE_COPY.equals(action)) {
			saveScoreCopy(requestIntent);
		} else if(ACTIONS.UPDATE_SCORE.equals(action)) {
			updateScore(requestIntent);
		} else if(ACTIONS.CLEAN_SCORE_SESSION_COPY.equals(action)) {
			long id = requestIntent.getLongExtra(ACTIONS.EXTRAS_ENTITY_ID, -1);
			if(id == -1) {
				onRequestError(requestIntent, "No entity ID provided");
				return;
			}
			dropSessionCopy(id);
			onRequestSuccess(requestIntent, new Intent());
		} else {
			super.onHandleIntent(requestIntent);
		}
	}

	private void updateScore(Intent requestIntent) {
		Score score = getScoreWithValidId(requestIntent);
		if(score == null) {
			return;
		}
		if(updateScore(requestIntent, score)) {
			dropSessionCopy(score.getId());
		}
	}
	
	private void dropSessionCopy(long originalId) {
		if(scoreCopies.containsKey(originalId)) {
			long copyId = scoreCopies.get(originalId);
			SQLiteDatabase db = mDb.getWritableDatabase();
			int count = db.delete(SCORES_TABLE_NAME, Scores._ID + " = " + copyId, null);
			db.delete(SCORES_INTMETA_TABLE_NAME, ScoresMeta._ID + " = " + copyId, null);
			if(count != 1) {
				Log.w(TAG, String.format(
					"Trying to drop %d session copy of %d caused %d deletions",
					copyId,
					originalId,
					count
				));
			} else {
				scoreCopies.remove(originalId);
				Log.v(TAG, "Dropped session copy #"+copyId+" of "+originalId);
			}
		}
	}

	private Score getScoreWithValidId(Intent requestIntent) {
		ParcelableScore parcelable = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE);
		if(parcelable == null) {
			Log.e(TAG, "Invalid request, no score provided");
			onRequestError(requestIntent, "No score provided");
			return null;
		}
		Score score = parcelable.getSource();
		if(score.getId() == Score.NO_ID) {
			Log.e(TAG, "Invalid request, score has invalid id");
			onRequestError(requestIntent, "Score has invalid id");
			return null;
		}
		return score;
	}

	private void saveScoreCopy(Intent requestIntent) {
		Score score = getScoreWithValidId(requestIntent);
		if(score == null) {
			return;
		}
		long sourceId = score.getId();
		String title = score.getTitle();
		if(title == null) {
			title = getString(android.R.string.untitled);
		}
		score.setTitle(getString(R.string.format_auto_copy, title));
		if(scoreCopies.containsKey(sourceId)) {
			long copyId = scoreCopies.get(sourceId);
			score.setId(copyId);
			updateScore(requestIntent, score);
		} else {
			score.setOriginalId(sourceId);
			long copyId = insertScore(requestIntent, score);
			if(copyId != -1) {
				scoreCopies.put(sourceId, copyId);
			}
		}
	}
	
	/**
	 * Keeps mapping of original score id -> it's copy id.
	 * It's lifetime equals application process lifetime.
	 */
	private static Map<Long, Long> scoreCopies = new HashMap<Long, Long>();
	
	/**
	 * Saves Score: title, content; (if present) ScoreConfig: *; into DB.
	 * Automatically updates MODIFIED field.
	 * @return if succeeded 
	 */
	private boolean updateScore(Intent requestIntent, Score score) {
		SQLiteDatabase writableDatabase = mDb.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Scores.TITLE, score.getTitle());
		try {
			values.put(Scores.CONTENT, score.getRawContent());
		} catch (SerializationException e) {
			Log.e(TAG, "Exception while deserializing Score from requestIncent", e);
			onRequestError(requestIntent, "Unable to deserialize Score");
			return false;
		}
		long time = System.currentTimeMillis();
		values.put(Scores.MODIFIED_UTC_TIME, time);
		int count = writableDatabase.update(SCORES_TABLE_NAME, values, Scores._ID + " = " + score.getId(), null);
		if(count != 1) {
			Log.e(TAG, String.format(
				"Updated %d rows, when trying to update just one Score#%d(parent: %d)",
				count, score.getId(), score.getOriginalId()
			));
			// send back error
			onRequestError(requestIntent, "Failed to update DB row");
			return false;
		} else {
			Log.v(TAG, "Updated Score#"+score.getId()+" row in DB storage");
			ScoreVisualizationConfig config = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE_VISUAL_CONF);
			updateScoreMeta(writableDatabase, score.getId(), config);
			onRequestSuccess(requestIntent, new Intent());
			return true;
		}
	}

	private void findScore(Intent requestIntent) {
		long id = requestIntent.getLongExtra(ACTIONS.EXTRAS_ENTITY_ID, -1);
		if(id == -1) {
			onRequestError(requestIntent, "No entity ID provided");
			return;
		}
		Cursor scoreCursor = null, metaCursor = null; 
		try {
			SQLiteDatabase db = mDb.getReadableDatabase();
			scoreCursor = db.query(
					SCORES_TABLE_NAME, null, 
					Scores._ID + " = " + id, null, 
					null, null, null);
			if(!scoreCursor.moveToFirst()) {
				onRequestError(requestIntent, "No row with id "+id);
				return;
			}
			Score score = new Score(
				id,
				scoreCursor.getLong(scoreCursor.getColumnIndex(Scores.ORIGINAL_ID)),
				scoreCursor.getString(scoreCursor.getColumnIndex(Scores.TITLE)),
				scoreCursor.getString(scoreCursor.getColumnIndex(Scores.CONTENT)),
				scoreCursor.getLong(scoreCursor.getColumnIndex(Scores.CREATED_UTC_TIME)),
				scoreCursor.getLong(scoreCursor.getColumnIndex(Scores.MODIFIED_UTC_TIME))
			);
			metaCursor = db.query(
				SCORES_INTMETA_TABLE_NAME, null,
				ScoresMeta._ID + " = " + id, null,
				null, null, null);
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY, score.prepareParcelable());
			if(requestIntent.getBooleanExtra(ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, false)) {
				ScoreVisualizationConfig scoreConf = ScoreVisualizationConfigFactory.createWithDefaults(this);
				while(metaCursor.moveToNext()) {
					String metaName = metaCursor.getString(metaCursor.getColumnIndex(ScoresMeta.META_NAME));
					long metaValue = metaCursor.getLong(metaCursor.getColumnIndex(ScoresMeta.META_VALUE));
					if(ScoresMeta.IntMeta.DISPLAY_MODE.equals(metaName)) {
						if(metaValue < 0 || metaValue >= DisplayMode.values().length) {
							Log.w(TAG, "Score meta DISPLAY_MODE = "+metaValue+" outside of Enum scope");
						} else {
							scoreConf.setDisplayMode(DisplayMode.values()[(int) metaValue]);
						}
					} else if(ScoresMeta.IntMeta.MIN_LINESPACE.equals(metaName)) {
						scoreConf.setMinSpaceAnchor((int) metaValue);
					} else if(ScoresMeta.IntMeta.MAX_LINESPACE.equals(metaName)) {
						scoreConf.setMaxSpaceAnchor((int) metaValue);
					} else {
						Log.d(TAG, "Uknown meta "+metaName+" for Score#"+id);
					}
				}
				outData.putExtra(ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF, scoreConf);
			}
			onRequestSuccess(requestIntent, outData);
		} catch (Exception e) {
			Log.e(TAG, "Exception while fetching Score row", e);
			onRequestError(requestIntent, "findScore() exception occured "+e.getMessage());
		}
		finally {
			if(scoreCursor != null) {
				scoreCursor.close();
			}
			if(metaCursor != null) {
				metaCursor.close();
			}
		}
	}

	@SuppressWarnings("unused")
	private void debuggingDelay(String action, final int secs) {
		for(int i = secs; i > 0; i--) {
			Log.v(TAG, i+" secs left to handle "+action);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return inserted Scores row id or -1 on failure
	 */
	private void insertScore(Intent requestIntent) {
		ParcelableScore parcelable = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE);
		if(parcelable == null) {
			Log.e(TAG, "Invalid request, no score provided");
			onRequestError(requestIntent, "No score provided");
			return;
		}
		Score score = parcelable.getSource();
		insertScore(requestIntent, score);
	}
	
	/**
	 * @return inserted Scores row id or -1 on failure
	 */

	private long insertScore(Intent requestIntent, Score score) {
		SQLiteDatabase writableDatabase = mDb.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Scores.TITLE, score.getTitle());
		try {
			values.put(Scores.CONTENT, score.getRawContent());
		} catch (SerializationException e) {
			Log.e(TAG, "Exception while deserializing Score from requestIncent", e);
			onRequestError(requestIntent, "Unable to deserialize Score");
			return -1;
		}
		values.put(Scores.ORIGINAL_ID, score.getOriginalId());
		long time = System.currentTimeMillis();
		values.put(Scores.CREATED_UTC_TIME, time);
		values.put(Scores.MODIFIED_UTC_TIME, time);
		long id = writableDatabase.insert(SCORES_TABLE_NAME, null, values);
		if(id == -1) {
			// send back error
			onRequestError(requestIntent, "Failed to insert into DB");
		} else {
			Log.v(TAG, "Creted Score in DB storage, id = "+id);
			ScoreVisualizationConfig config = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE_VISUAL_CONF);
			updateScoreMeta(writableDatabase, id, config);
			
			// send back created score id
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY_ID, id);
			onRequestSuccess(requestIntent, outData);
		}
		return id;
	}

	private void updateScoreMeta(SQLiteDatabase writableDatabase, long scoreId,
			ScoreVisualizationConfig config) {
		if(config == null) {
			return;
		}
		ContentValues metaEntry = new ContentValues();
		metaEntry.put(ScoresMeta._ID, scoreId);
		if(config.getDisplayMode() != null) {
			insertOrUpdateIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.DISPLAY_MODE, 
				config.getDisplayMode().ordinal());
		}
		insertOrUpdateIntMeta(
			writableDatabase, metaEntry, 
			ScoresMeta.IntMeta.MIN_LINESPACE, 
			config.getMinSpaceAnchor()
		);
		insertOrUpdateIntMeta(
			writableDatabase, metaEntry, 
			ScoresMeta.IntMeta.MAX_LINESPACE, 
			config.getMaxSpaceAnchor()
		);
	}
	

	private void insertOrUpdateIntMeta(SQLiteDatabase writableDatabase, ContentValues metaEntry, String metaName, int metaValue) {
		metaEntry.put(ScoresMeta.META_NAME, metaName);
		metaEntry.put(ScoresMeta.META_VALUE, metaValue);
		long id = writableDatabase.insertWithOnConflict(SCORES_INTMETA_TABLE_NAME, null, metaEntry, SQLiteDatabase.CONFLICT_REPLACE);
		if(id == -1) {
			Log.w(TAG, "Failed to save integer meta "+metaName);
		}
	}
	
	private static final String SCORES_TABLE_NAME = "scores";
	private static class Scores implements BaseColumns {
		public static final String TITLE = "title";
		public static final String CREATED_UTC_TIME = "creation_UTCdate";
		public static final String MODIFIED_UTC_TIME = "modification_UTCtime";
		public static final String CONTENT = "content";
		public static final String ORIGINAL_ID = "original_id";
	}
	private static final String SCORES_INTMETA_TABLE_NAME = "scores_meta_int";
	private static class ScoresMeta implements BaseColumns {
		public static final String META_NAME = "name";
		public static final String META_VALUE = "value";
		
		public static class IntMeta {
			public static final String DISPLAY_MODE = "displaymode";
			public static final String MIN_LINESPACE = "minlsp";
			public static final String MAX_LINESPACE = "maxlsp";
		}
	}
	
	private static class DbHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "scores.sqlite";
		private static final int DATABASE_VERSION = 2;

		DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
		
		 @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SCORES_TABLE_NAME + " ("
                + Scores._ID + " INTEGER PRIMARY KEY,"
                + Scores.TITLE + " TEXT,"
                + Scores.CONTENT + " TEXT,"
                + Scores.CREATED_UTC_TIME + " INTEGER,"
                + Scores.MODIFIED_UTC_TIME + " INTEGER,"
                + Scores.ORIGINAL_ID + " INTEGER"
            + ");");
            db.execSQL("CREATE TABLE " + SCORES_INTMETA_TABLE_NAME + " ("
                + ScoresMeta._ID + " INTEGER,"
                + ScoresMeta.META_NAME + " TEXT,"
                + ScoresMeta.META_VALUE + " INTEGER,"
                + String.format("PRIMARY KEY (%s, %s)", ScoresMeta._ID, ScoresMeta.META_NAME)
            + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ SCORES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+ SCORES_INTMETA_TABLE_NAME);
            onCreate(db);
        }
		
	}

}
