package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asFlagVal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig.DisplayMode;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfigFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;

public class ScoreRepository implements AutoCloseable {
    static final String DATABASE_NAME = "scores.sqlite";

    private static final LogUtils log = new LogUtils(ScoreRepository.class);
    private static final String SCORES_TABLE_NAME = "scores";
    private static final String SCORES_INTMETA_TABLE_NAME = "scores_meta_int";
    private static final String[] METAS_VISUAL = new String[] {
        ScoresMeta.IntMeta.DISPLAY_MODE,
        ScoresMeta.IntMeta.MIN_LINESPACE,
        ScoresMeta.IntMeta.MAX_LINESPACE
    };

    private final Context context;
    private final DbHelper dbHelper;

    public ScoreRepository(Context context) {
        this.context = context;
        this.dbHelper = new DbHelper(context);
    }

    public static boolean clearStorage(Context context) {
        return context.deleteDatabase(DATABASE_NAME);
    }

    public long insertScore(Score score, ScoreVisualizationConfig config) throws SerializationException {
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        long id = insertAsNew(score, writableDatabase);
        if(id != -1) {
            updateScoreMeta(writableDatabase, id, config);
        }
        return id;
    }

    public ListResult listScores(boolean attachVisualConfig) throws SerializationException {
        Cursor scoreCursor = null;
        Cursor metaCursor = null;
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            scoreCursor = db.query(
                SCORES_TABLE_NAME, null,
                null, null,
                null, null,
                Scores.MODIFIED_UTC_TIME + " DESC"
            );
            int total = scoreCursor.getCount();
            ParcelableScore[] scores = new ParcelableScore[total];
            Map<Long, Integer> idToIndex = new HashMap<Long, Integer>();
            for(int i = 0; scoreCursor.moveToNext(); i++) {
                Score score = rowToScore(scoreCursor);
                scores[i] = score.prepareParcelable();
                idToIndex.put(score.getId(), i);
            }

            ScoreVisualizationConfig[] configs = null;
            if(attachVisualConfig) {
                configs = new ScoreVisualizationConfig[total];
                for(int i = 0; i < total; i++) {
                    configs[i] = ScoreVisualizationConfigFactory.createWithDefaults(context);
                }
                metaCursor = db.query(
                    SCORES_INTMETA_TABLE_NAME, null,
                    ScoresMeta.META_NAME + " IN (?, ?, ?)" , METAS_VISUAL,
                    null, null, null);
                while(metaCursor.moveToNext()) {
                    long scoreId = metaCursor.getLong(metaCursor.getColumnIndex(ScoresMeta._ID));
                    if(idToIndex.containsKey(scoreId)) {
                        fillField(metaCursor, configs[idToIndex.get(scoreId)]);
                    }
                }
            }
            return new ListResult(scores, configs);
        } finally {
            if(scoreCursor != null) {
                scoreCursor.close();
            }
            if(metaCursor != null) {
                metaCursor.close();
            }
        }
    }

    public ScoreWithConfig findScore(long id, boolean attachVisualConfig, boolean attachPlayConfig)
            throws SerializationException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Score score = loadScore(db, id);
        if(score == null) {
            return null;
        }
        ScoreVisualizationConfig visualConfig = attachVisualConfig ? parseVisualConfig(db, id) : null;
        PlayingConfiguration playConfig = attachPlayConfig ? parsePlayConfig(db, id) : null;
        return new ScoreWithConfig(score, visualConfig, playConfig);
    }

    private PlayingConfiguration parsePlayConfig(SQLiteDatabase db, long scoreId) {
        Cursor playConfCursor = null;
        try {
            playConfCursor = db.query(
                SCORES_INTMETA_TABLE_NAME, null,
                ScoresMeta._ID + " = " + scoreId, null,
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
                return playConf;
            } else if(presence != 0) {
                log.d("Missing meta entries for PlayingConfiguration for Score#"+scoreId);
            }
            return null;
        } finally {
            if(playConfCursor != null) {
                playConfCursor.close();
            }
        }
    }

    private ScoreVisualizationConfig parseVisualConfig(SQLiteDatabase db, long scoreId) {
        ScoreVisualizationConfig scoreConf = ScoreVisualizationConfigFactory.createWithDefaults(context);
        Cursor metaCursor = null;
        try {
            metaCursor = db.query(
                SCORES_INTMETA_TABLE_NAME, null,
                ScoresMeta._ID + " = " + scoreId, null,
                null, null, null);
            while(metaCursor.moveToNext()) {
                fillField(metaCursor, scoreConf);
            }
        } finally {
            if(metaCursor != null) {
                metaCursor.close();
            }
        }
        return scoreConf;
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

    private long insertAsNew(Score score, SQLiteDatabase writableDatabase) throws SerializationException {
        ContentValues values = new ContentValues();
        values.put(Scores.TITLE, score.getTitle());
        values.put(Scores.CONTENT, score.getRawContent());
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

    private void insertOrUpdateIntMeta(SQLiteDatabase writableDatabase, ContentValues metaEntry,
            String metaName, int metaValue) {
        metaEntry.put(ScoresMeta.META_NAME, metaName);
        metaEntry.put(ScoresMeta.META_VALUE, metaValue);
        long id = writableDatabase.insertWithOnConflict(
            SCORES_INTMETA_TABLE_NAME, null, metaEntry, SQLiteDatabase.CONFLICT_REPLACE);
        if(id == -1) {
            log.w("Failed to save integer meta "+metaName);
        }
    }

    private static void fillField(Cursor metaCursor, ScoreVisualizationConfig scoreConf) {
        String metaName = metaCursor.getString(metaCursor.getColumnIndex(ScoresMeta.META_NAME));
        long metaValue = metaCursor.getLong(metaCursor.getColumnIndex(ScoresMeta.META_VALUE));
        if(ScoresMeta.IntMeta.DISPLAY_MODE.equals(metaName)) {
            if(metaValue < 0 || metaValue >= DisplayMode.values().length) {
                log.w("Score meta DISPLAY_MODE = "+metaValue+" outside of Enum scope");
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

    @Override
    public void close() {
        dbHelper.close();
    }

    public static class ListResult {
        public final ParcelableScore[] scores;
        public final ScoreVisualizationConfig[] visualConfigs;

        private ListResult(ParcelableScore[] scores, ScoreVisualizationConfig[] visualConfigs) {
            this.scores = scores;
            this.visualConfigs = visualConfigs;
        }
    }

    public static class ScoreWithConfig {
        public final Score score;
        public final ScoreVisualizationConfig visualConfig;
        public final PlayingConfiguration playConfig;

        private ScoreWithConfig(Score score, ScoreVisualizationConfig visualConfig,
                PlayingConfiguration playConfig) {
            this.score = score;
            this.visualConfig = visualConfig;
            this.playConfig = playConfig;
        }
    }

    private static class Scores implements BaseColumns {
        public static final String TITLE = "title";
        public static final String CREATED_UTC_TIME = "creation_UTCdate";
        public static final String MODIFIED_UTC_TIME = "modification_UTCtime";
        public static final String CONTENT = "content";
        public static final String ORIGINAL_ID = "original_id";
    }

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
            log.w("Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+ SCORES_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS "+ SCORES_INTMETA_TABLE_NAME);
            onCreate(db);
        }
    }
}
