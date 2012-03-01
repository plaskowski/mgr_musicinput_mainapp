package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
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
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.INSERT_SCORE.equals(action)) {
			insertScore(requestIntent);
		} else if(ACTIONS.GET_SCORE_BY_ID.equals(action)) {
			findScore(requestIntent);
		} else {
			super.onHandleIntent(requestIntent);
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

	private void insertScore(Intent requestIntent) {
		SQLiteDatabase writableDatabase = mDb.getWritableDatabase();
		ContentValues values = new ContentValues();
		ParcelableScore parcelable = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE);
		Score score = parcelable.getSource();
		values.put(Scores.TITLE, score.getTitle());
		try {
			values.put(Scores.CONTENT, score.getRawContent());
		} catch (SerializationException e) {
			Log.e(TAG, "Exception while deserializing Score from requestIncent", e);
			onRequestError(requestIntent, "Unable to deserialize Score");
			return;
		}
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
			ContentValues metaEntry = new ContentValues();
			metaEntry.put(ScoresMeta._ID, id);
			if(config.getDisplayMode() != null) {
				insertIntMeta(
					writableDatabase, metaEntry, 
					ScoresMeta.IntMeta.DISPLAY_MODE, 
					config.getDisplayMode().ordinal());
			}
			insertIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.MIN_LINESPACE, 
				config.getMinSpaceAnchor()
			);
			insertIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.MAX_LINESPACE, 
				config.getMaxSpaceAnchor()
			);
			
			// send back created score id
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY_ID, id);
			onRequestSuccess(requestIntent, outData);
		}
		
	}

	private void insertIntMeta(SQLiteDatabase writableDatabase, ContentValues metaEntry, String metaName, int metaValue) {
		metaEntry.put(ScoresMeta.META_NAME, metaName);
		metaEntry.put(ScoresMeta.META_VALUE, metaValue);
		long id = writableDatabase.insert(SCORES_INTMETA_TABLE_NAME, null, metaEntry);
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
		private static final int DATABASE_VERSION = 1;

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
                + Scores.MODIFIED_UTC_TIME + " INTEGER"
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
