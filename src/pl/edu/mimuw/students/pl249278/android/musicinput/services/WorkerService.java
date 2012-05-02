package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.musicinput.FileOutputWrapper;
import pl.edu.mimuw.students.pl249278.android.musicinput.MidiBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.TemporaryFilesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.midi.MidiFile;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class WorkerService extends AsynchronousRequestsService {
	private static final String TAG = WorkerService.class.getName();
	
	public static void scheduleCleanOldFiles(Context ctx) {
		Intent request = AsyncHelper.prepareServiceIntent(
			ctx, WorkerService.class, 
			WorkerService.ACTIONS.CLEAN_UNUSED_TEMPORARY_FILES, 
			null, null, false
		);
		ctx.startService(request);
	}

	public WorkerService() {
		super(WorkerService.class.getSimpleName());
	}
	
	public static class ACTIONS {
		/** Deletes files created by {@link TemporaryFilesFactory} in previous sessions */
		public static final String CLEAN_UNUSED_TEMPORARY_FILES = WorkerService.class.getName()+".clean_temp_files";
		/**
		 * Parameters: {@link #EXTRAS_SCORE_ID}, {@value #EXTRAS_DEST_FILE}
		 */
		public static final String EXPORT_TO_MIDI = WorkerService.class.getName()+".export_to_midi";
		public static final String EXTRAS_SCORE_ID = "score_id";
		public static final String EXTRAS_DEST_FILE = "dest_file";
		/**
		 * Used as internal callback action from ContentService for request sent from {@link #EXPORT_TO_MIDI}
		 * Parameters: {@link #EXTRAS_ORIGINAL_REQUEST}, {@link ContentService.ACTIONS#RESPONSE_EXTRAS_ENTITY}
		 */
		public static final String SCORE_TO_MIDI = WorkerService.class.getName()+".score_to_midi";
		public static final String EXTRAS_ORIGINAL_REQUEST = "original_request";
	}
	
	public static File getExportDir() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.EXPORT_TO_MIDI.equals(action)) {
			Intent callbackIntent = AsyncHelper.getServiceCallback(getApplicationContext(), WorkerService.class);
			callbackIntent.setAction(ACTIONS.SCORE_TO_MIDI);
			callbackIntent.putExtra(ACTIONS.EXTRAS_ORIGINAL_REQUEST, requestIntent);
			Intent i = AsyncHelper.prepareServiceIntent(this, ContentService.class, 
				ContentService.ACTIONS.GET_SCORE_BY_ID, null, callbackIntent, false);
			i.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, requestIntent.getLongExtra(ACTIONS.EXTRAS_SCORE_ID, -1));
			i.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_PLAY_CONF, true);
			startService(i);
		} else if(ACTIONS.SCORE_TO_MIDI.equals(action)) {
			Intent originalRequest = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_ORIGINAL_REQUEST);
			if(!AsyncHelper.isSuccess(requestIntent)) {
				Log.v(TAG, "Failed to get response from ContentService");
				onRequestError(originalRequest, "Failed to get Score from ContentService");
				return;
			}
			ParcelableScore score = requestIntent.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			PlayingConfiguration playConf = requestIntent.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_PLAY_CONF);
			if(playConf == null) {
				playConf = new PlayingConfiguration(
					getResources().getInteger(R.integer.defaultPlayTempoBPM),
					false,
					false,
					false
				);
			}
			String filename = originalRequest.getStringExtra(ACTIONS.EXTRAS_DEST_FILE);
			FileOutputStream stream = null;
			try {
				// save to file on external storage
				MidiFile midiFile = MidiBuilder.build(score.getSource().getContent(), 0, playConf);
				getExportDir().mkdirs();
				String path = new File(getExportDir(), filename).getCanonicalPath();
				stream = new FileOutputStream(path);
				midiFile.writeTo(new FileOutputWrapper(stream));
				// insert meta information into global ContentProvider to make it accessible worldwide
				ContentValues values = new ContentValues();
				values.put(Media.ARTIST, getString(R.string.midiArtist));
				String title = score.getSource().getTitle();
				if(title != null) {
					values.put(Media.TITLE, title);
				}
				values.put(Media.ALBUM, getString(R.string.midiAlbum));
				values.put(Media.MIME_TYPE, "audio/midi");
				values.put(Media.IS_MUSIC, true);
				values.put(Media.DATA, path);
				int updated = getContentResolver().update(Media.EXTERNAL_CONTENT_URI, values, Media.DATA+" = ?", new String[] { path });
				if(updated > 0) {
					Log.v(TAG, "Updated meta for " + path);
				} else {
					Uri uri = this.getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
					Log.v(TAG, "Inserted "+path+" as "+uri);
				}
				onRequestSuccess(originalRequest, new Intent());
			} catch (Exception e) {
				Log.w(TAG, "Failed to parse Score and write it to .midi file", e);
				onRequestError(originalRequest, "Exception occured");
			} finally {
				if(stream != null) try {
					stream.close();
				} catch(Exception e) {
					Log.w(TAG, "Failed to close .midi output stream", e);
				}
			}
		} else if(ACTIONS.CLEAN_UNUSED_TEMPORARY_FILES.equals(action)) {
			File[] oldFiles = TemporaryFilesFactory.listOldFiles(this);
			List<File> success = new ArrayList<File>(), failure = new ArrayList<File>();
			for (int i = 0; i < oldFiles.length; i++) {
				File file = oldFiles[i];
				if(file.delete()) {
					success.add(file);
				} else {
					failure.add(file);
				}
			}
			if(success.size() > 0) {
				Log.v(TAG, String.format(
					"Deleted %d unused files: %s",
					success.size(),
					Arrays.toString(success.toArray())
				));
			}
			if(failure.size() > 0) {
				Log.w(TAG, String.format(
					"Failed to delete %d unused files: %s",
					failure.size(),
					Arrays.toString(failure.toArray())
				));
			}
			onRequestSuccess(requestIntent, new Intent());
		} else {
			super.onHandleIntent(requestIntent);
		}
	}

}
