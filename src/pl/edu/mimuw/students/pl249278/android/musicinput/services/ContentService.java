package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asFlagVal;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
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
		/**
		 * Parameters: {@link #EXTRAS_SCORE}, {@link #EXTRAS_SCORE_VISUAL_CONF}
		 * Output: {@link #RESPONSE_EXTRAS_ENTITY_ID}
		 */
		public static final String INSERT_SCORE = ContentService.class.getName()+".insert_score";
		public static final String EXTRAS_SCORE = "score";
		public static final String EXTRAS_SCORE_VISUAL_CONF = "score_vis_conf";
		/** inserted entity id (of type long) */
		public static final String RESPONSE_EXTRAS_ENTITY_ID = "entity_id";
		
		/**
		 * Parameters: {@link #EXTRAS_ENTITY_ID} Score id, {@link #EXTRAS_ATTACH_SCORE_VISUAL_CONF}, {@link #EXTRAS_ATTACH_SCORE_PLAY_CONF}
		 * <br />
		 * Output: {@link #RESPONSE_EXTRAS_ENTITY} Score object, {@link #RESPONSE_EXTRAS_VISUAL_CONF}, {@link #RESPONSE_EXTRAS_PLAY_CONF} or null
		 */
		public static String GET_SCORE_BY_ID = ContentService.class.getName()+".get_score";
		/** whether service have to attach visual configuration of requested score (type boolean) */
		public static final String EXTRAS_ATTACH_SCORE_VISUAL_CONF = "attach_vis_conf";
		/** whether service have to attach {@link PlayingConfiguration} of requested score (type boolean) */
		public static final String EXTRAS_ATTACH_SCORE_PLAY_CONF = "attach_play_conf";
		/** requested entity id (of type long) */
		public static final String EXTRAS_ENTITY_ID = "entity_id";
		public static final String RESPONSE_EXTRAS_ENTITY = "entity";
		public static final String RESPONSE_EXTRAS_VISUAL_CONF = "vis_conf";
		public static final String RESPONSE_EXTRAS_PLAY_CONF = "play_conf";
		
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
		 * Parameters: {@link #EXTRAS_SCORE}, {@link #EXTRAS_SCORE_VISUAL_CONF} (optional), {@link #EXTRAS_KEEP_BACKUP}
		 */
		public static final String UPDATE_SCORE = ContentService.class.getName()+".update_score";
		public static final String EXTRAS_KEEP_BACKUP = "keep_backup";
		/**
		 * Removes session copy.
		 * Parameters: {@link #EXTRAS_ENTITY_ID} - original id
		 */
		public static final String CLEAN_SCORE_SESSION_COPY = ContentService.class.getName()+".clean_copy";
		/**
		 * Saves {@link PlayingConfiguration} of given Score.
		 * Parameters: {@link #EXTRAS_ENTITY_ID} id of Score, {@link #EXTRAS_SCORE_PLAY_CONF} configuration object
		 */
		public static final String SAVE_PLAY_CONF = ContentService.class.getName()+".save_playconf";
		public static final String EXTRAS_SCORE_PLAY_CONF = "score_play_conf";
		/**
		 * Parameters: {@link #EXTRAS_ATTACH_SCORE_VISUAL_CONF} (optional) attach each retrieved Score its {@link ScoreVisualizationConfig}
		 * <br />
		 * Output: {@link #RESPONSE_EXTRAS_SCORES} - array of Score objects, {@link #RESPONSE_EXTRAS_VISUAL_CONFS} - array of {@link ScoreVisualizationConfig} matching returned Scores
		 */
		public static final String LIST_SCORES = ContentService.class.getName()+".list_scores";
		public static final String RESPONSE_EXTRAS_SCORES = "scores_array";
		public static final String RESPONSE_EXTRAS_VISUAL_CONFS = "vis_conf_array";
		 /**
		  * Parameters: {@link #EXTRAS_ENTITY_ID} - id of Score
		 */
		public static final String DELETE_SCORE = ContentService.class.getName()+".delete_score";
		 /**
		  * Parameters: {@link #EXTRAS_ENTITY_ID} - id of Score to copy, {@link #EXTRAS_NEW_TITLE} 
		  * Output: {@link #RESPONSE_EXTRAS_ENTITY} - copy with valid id and time stamps
		 */
		public static final String DUPLICATE_SCORE = ContentService.class.getName()+".duplicate_score";
		public static final String EXTRAS_NEW_TITLE = "new_title";
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.INSERT_SCORE.equals(action)) {
			insertScore(requestIntent);
		} else if(ACTIONS.DUPLICATE_SCORE.equals(action)) {
			duplicateScore(requestIntent);
		} else if(ACTIONS.GET_SCORE_BY_ID.equals(action)) {
			findScore(requestIntent);
		} else if(ACTIONS.SAVE_SCORE_COPY.equals(action)) {
			saveScoreCopy(requestIntent);
		} else if(ACTIONS.UPDATE_SCORE.equals(action)) {
			updateScore(requestIntent);
		} else if(ACTIONS.DELETE_SCORE.equals(action)) {
			deleteScore(requestIntent);
		} else if(ACTIONS.LIST_SCORES.equals(action)) {
			listScores(requestIntent);
		} else if(ACTIONS.SAVE_PLAY_CONF.equals(action)) {
			savePlayConfiguration(requestIntent);
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

	private void duplicateScore(Intent requestIntent) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		long id = requestIntent.getLongExtra(ACTIONS.EXTRAS_ENTITY_ID, -1);
		Score score = loadScore(db, id);
		if(score == null) {
			onRequestError(requestIntent, "No Score#"+id);
			return;
		}
		score.setTitle(requestIntent.getStringExtra(ACTIONS.EXTRAS_NEW_TITLE));
		long copyId = insertAsNew(score, db);
		if(copyId == -1) {
			onRequestError(requestIntent, "Failed to create copy of Score#"+id);
			return;
		}
		score.setId(copyId);
		// copy all meta data
		try {
			db.execSQL(String.format(
				"INSERT INTO %s (%s, %s, %s) SELECT %d, %s, %s FROM %s WHERE %s = %d", 
				SCORES_INTMETA_TABLE_NAME, ScoresMeta._ID, ScoresMeta.META_NAME, ScoresMeta.META_VALUE,
				copyId, ScoresMeta.META_NAME, ScoresMeta.META_VALUE, SCORES_INTMETA_TABLE_NAME,
				ScoresMeta._ID, id
			));
		} catch(Exception e) {
			Log.w(TAG, "Failed to copy meta data of Score#"+id, e);
		}
		try {
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY, score.prepareParcelable());
			onRequestSuccess(requestIntent, outData);
		} catch (SerializationException e) {
			onRequestError(requestIntent, "Failed to serialize Score#"+copyId);
		}
	}

	private void deleteScore(Intent requestIntent) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		long scoreId = requestIntent.getLongExtra(ACTIONS.EXTRAS_ENTITY_ID, -1);
		int count = db.delete(SCORES_TABLE_NAME, Scores._ID + " = " + scoreId, null);
		if(count == 0) {
			onRequestError(requestIntent, "No Score to delete with id "+scoreId);
		} else {
			Log.v(TAG, "Deleted Score#"+scoreId);
			db.delete(SCORES_INTMETA_TABLE_NAME, ScoresMeta._ID + " = " + scoreId, null);
			onRequestSuccess(requestIntent, new Intent());
		}
	}

	private Map<Long, Integer> idToIndex = new HashMap<Long, Integer>();
	private static final String[] METAS_VISUAL = new String[] {
		ScoresMeta.IntMeta.DISPLAY_MODE,
		ScoresMeta.IntMeta.MIN_LINESPACE,
		ScoresMeta.IntMeta.MAX_LINESPACE
	};
	
	private void listScores(Intent requestIntent) {
		Cursor scoreCursor = null, metaCursor = null; 
		try {
			SQLiteDatabase db = mDb.getReadableDatabase();
			scoreCursor = db.query(
					SCORES_TABLE_NAME, null, 
					null, null, 
					null, null,
					Scores.MODIFIED_UTC_TIME + " DESC"
			);
			int total = scoreCursor.getCount();
			ParcelableScore[] scores = new ParcelableScore[total];
			idToIndex.clear();
			for(int i = 0; scoreCursor.moveToNext(); i++) {
				Score score = rowToScore(scoreCursor);
				scores[i] = score.prepareParcelable();
				idToIndex.put(score.getId(), i);
			}
			Intent result = new Intent();
			result.putExtra(ACTIONS.RESPONSE_EXTRAS_SCORES, scores);
			if(requestIntent.getBooleanExtra(ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, false)) {
				ScoreVisualizationConfig[] confs = new ScoreVisualizationConfig[total];
				for(int i = 0; i < total; i++) {
					confs[i] = ScoreVisualizationConfigFactory.createWithDefaults(this);
				}
				metaCursor = db.query(
					SCORES_INTMETA_TABLE_NAME, null,
					ScoresMeta.META_NAME + " IN (?, ?, ?)" , METAS_VISUAL,
					null, null, null);
				while(metaCursor.moveToNext()) {
					long scoreId = metaCursor.getLong(metaCursor.getColumnIndex(ScoresMeta._ID));
					if(idToIndex.containsKey(scoreId)) {
						fillField(metaCursor, confs[idToIndex.get(scoreId)]);
					}
				}
				result.putExtra(ACTIONS.RESPONSE_EXTRAS_VISUAL_CONFS, confs);
			}
			onRequestSuccess(requestIntent, result);
		} catch (SerializationException e) {
			Log.w(TAG, "Impossible error", e);
			onRequestError(requestIntent, "Failed to read Scores from DB");
		} catch (Exception e) {
			Log.w(TAG, "", e);
			onRequestError(requestIntent, "Failed to read Scores from DB");
		} finally {
			if(scoreCursor != null) {
				scoreCursor.close();
			}
			if(metaCursor != null) {
				metaCursor.close();
			}
		}
	}

	private void savePlayConfiguration(Intent requestIntent) {
		long scoreId = requestIntent.getLongExtra(ACTIONS.EXTRAS_ENTITY_ID, -1);
		if(scoreId == -1) {
			onRequestError(requestIntent, "No ENTITY_ID provided");
			return;
		}
		PlayingConfiguration config = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_SCORE_PLAY_CONF);
		if(config == null) {
			onRequestError(requestIntent, "No PlayConfiguration object provided");
			return;
		}
		try {
			SQLiteDatabase writableDatabase = mDb.getWritableDatabase();
			ContentValues metaEntry = new ContentValues();
			metaEntry.put(ScoresMeta._ID, scoreId);
			insertOrUpdateIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.TEMPO, 
				config.getTempo()
			);
			insertOrUpdateIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.METRONOME, 
				asFlagVal(config.isPlayMetronome())
			);
			insertOrUpdateIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.LOOP, 
				asFlagVal(config.isLoop())
			);
			insertOrUpdateIntMeta(
				writableDatabase, metaEntry, 
				ScoresMeta.IntMeta.INTRO, 
				asFlagVal(config.isPrependEmptyBar())
			);
		} finally {
			onRequestSuccess(requestIntent, new Intent());
		}
	}

	private void updateScore(Intent requestIntent) {
		Score score = getScoreWithValidId(requestIntent);
		if(score == null) {
			return;
		}
		if(updateScore(requestIntent, score) && !requestIntent.getBooleanExtra(ACTIONS.EXTRAS_KEEP_BACKUP, false)) {
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
		Cursor metaCursor = null, playConfCursor = null; 
		try {
			SQLiteDatabase db = mDb.getReadableDatabase();
			Score score = loadScore(db, id);
			if(score == null) {
				onRequestError(requestIntent, "No Score row with id "+id);
				return;
			}
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY, score.prepareParcelable());
			if(requestIntent.getBooleanExtra(ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, false)) {
				ScoreVisualizationConfig scoreConf = ScoreVisualizationConfigFactory.createWithDefaults(this);
				metaCursor = db.query(
					SCORES_INTMETA_TABLE_NAME, null,
					ScoresMeta._ID + " = " + id, null,
					null, null, null);
				while(metaCursor.moveToNext()) {
					fillField(metaCursor, scoreConf);
				}
				outData.putExtra(ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF, scoreConf);
			}
			if(requestIntent.getBooleanExtra(ACTIONS.EXTRAS_ATTACH_SCORE_PLAY_CONF, false)) {
				playConfCursor = db.query(
					SCORES_INTMETA_TABLE_NAME, null,
					ScoresMeta._ID + " = " + id, null,
					null, null, null);
				PlayingConfiguration playConf = new PlayingConfiguration(-1, false, false, false);
				int presence = 0;
				while(playConfCursor.moveToNext()) {
					String metaName = playConfCursor.getString(playConfCursor.getColumnIndex(ScoresMeta.META_NAME));
					long metaValue = playConfCursor.getLong(playConfCursor.getColumnIndex(ScoresMeta.META_VALUE));
					if(ScoresMeta.IntMeta.TEMPO.equals(metaName)) {
						playConf.setTempo((int) metaValue);
						presence |= 1 << 0;
					} else if(ScoresMeta.IntMeta.LOOP.equals(metaName)) {
						playConf.setLoop(IntUtils.asBool((int) metaValue)); 
						presence |= 1 << 1;
					} else if(ScoresMeta.IntMeta.METRONOME.equals(metaName)) {
						playConf.setPlayMetronome(IntUtils.asBool((int) metaValue)); 
						presence |= 1 << 2;
					} else if(ScoresMeta.IntMeta.INTRO.equals(metaName)) {
						playConf.setPrependEmptyBar(IntUtils.asBool((int) metaValue)); 
						presence |= 1 << 3;
					}
				}
				if(presence == 0x0F) {
					outData.putExtra(ACTIONS.RESPONSE_EXTRAS_PLAY_CONF, playConf);
				} else if(presence != 0) {
					Log.d(TAG, "Missing meta entries for PlayingConfiguration for Score#"+id);
				}
			}
 			onRequestSuccess(requestIntent, outData);
		} catch (Exception e) {
			Log.e(TAG, "Exception while fetching Score row", e);
			onRequestError(requestIntent, "findScore() exception occured "+e.getMessage());
		}
		finally {
			if(metaCursor != null) {
				metaCursor.close();
			}
			if(playConfCursor != null) {
				playConfCursor.close();
			}
		}
	}
	
	private Score loadScore(SQLiteDatabase db, long scoreId) {
		Cursor scoreCursor = null;
		try {
			scoreCursor = db.query(
				SCORES_TABLE_NAME, null, 
				Scores._ID + " = " + scoreId, null, 
				null, null, null);
			if(!scoreCursor.moveToFirst()) {
				return null;
			}
			return rowToScore(scoreCursor);
		} finally {
			if(scoreCursor != null) {
				scoreCursor.close();
			}
		}
	}

	private static void fillField(Cursor metaCursor, ScoreVisualizationConfig scoreConf) {
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
		}
	}

	private static Score rowToScore(Cursor scoreTableCursor) {
		return new Score(
			scoreTableCursor.getLong(scoreTableCursor.getColumnIndex(Scores._ID)),
			scoreTableCursor.getLong(scoreTableCursor.getColumnIndex(Scores.ORIGINAL_ID)),
			scoreTableCursor.getString(scoreTableCursor.getColumnIndex(Scores.TITLE)),
			scoreTableCursor.getString(scoreTableCursor.getColumnIndex(Scores.CONTENT)),
			scoreTableCursor.getLong(scoreTableCursor.getColumnIndex(Scores.CREATED_UTC_TIME)),
			scoreTableCursor.getLong(scoreTableCursor.getColumnIndex(Scores.MODIFIED_UTC_TIME))
		);
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
	
	private long insertScore(Intent requestIntent, Score score) {
		SQLiteDatabase writableDatabase = mDb.getWritableDatabase();
		long id = insertAsNew(score, writableDatabase);
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

	/**
	 * Insert given Score as new row into DB table. Fills in stamps as side effect.
	 * @return row id or -1 on failure
	 */
	private long insertAsNew(Score score, SQLiteDatabase writableDatabase) {
		ContentValues values = new ContentValues();
		values.put(Scores.TITLE, score.getTitle());
		try {
			values.put(Scores.CONTENT, score.getRawContent());
		} catch (SerializationException e) {
			Log.e(TAG, "Exception while deserializing Score from requestIncent", e);
			return -1;
		}
		values.put(Scores.ORIGINAL_ID, score.getOriginalId());
		long time = System.currentTimeMillis();
		values.put(Scores.CREATED_UTC_TIME, time);
		values.put(Scores.MODIFIED_UTC_TIME, time);
		score.setStamps(time);
		return writableDatabase.insert(SCORES_TABLE_NAME, null, values);
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
			public static final String INTRO = "intro";
			public static final String LOOP = "loop";
			public static final String METRONOME = "metronome";
			public static final String DISPLAY_MODE = "displaymode";
			public static final String MIN_LINESPACE = "minlsp";
			public static final String MAX_LINESPACE = "maxlsp";
			public static final String TEMPO = "tempo";
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
