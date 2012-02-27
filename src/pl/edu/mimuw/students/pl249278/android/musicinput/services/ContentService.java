package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.test.UiThreadTest;
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
		public static String INSERT_SCORE = ContentService.class.getName()+".insert_score";
		public static final String EXTRAS_SCORE = "score";
		/** inserted entity id (of type long) */
		public static final String RESPONSE_EXTRAS_ENTITY_ID = "entity_id";
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.INSERT_SCORE.equals(action)) {
			insertScore(requestIntent);
		} else {
			super.onHandleIntent(requestIntent);
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
		// TODO add displayMode support
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
			// send back created score id
			Intent outData = new Intent();
			outData.putExtra(ACTIONS.RESPONSE_EXTRAS_ENTITY_ID, id);
			onRequestSuccess(requestIntent, outData);
		}
		
	}

	private static final String SCORES_TABLE_NAME = "scores";
	private static class Scores implements BaseColumns {
		public static final String TITLE = "title";
		public static final String CREATED_UTC_TIME = "creation_UTCdate";
		public static final String MODIFIED_UTC_TIME = "modification_UTCtime";
		public static final String CONTENT = "content";
	}
	
	private static class DbHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "scores_db";
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
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ SCORES_TABLE_NAME);
            onCreate(db);
        }
		
	}

}
