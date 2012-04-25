package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService;
import pl.edu.mimuw.students.pl249278.android.musicinput.TemporaryFilesFactory;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WorkerService extends AsynchronousRequestsService {
	static final String TAG = WorkerService.class.getName();
	
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
	}
	
	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.CLEAN_UNUSED_TEMPORARY_FILES.equals(action)) {
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
