package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.content.Context;
import android.os.SystemClock;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;

public class TemporaryFilesFactory {
	private static final String PREFIX = "sessiontemp";
	private static final String SEPARATOR = "_";
	private static int sequence = 0;
	private static long sessionId;
	private static boolean isInitialized = false;
	
	public static File[] listOldFiles(Context ctx) {
		init();
		final String validFilesPrefix = PREFIX + sessionId + SEPARATOR;
		File dir = ctx.getFilesDir();
		return dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() 
				&& pathname.getName().startsWith(PREFIX)
				&& !pathname.getName().startsWith(validFilesPrefix);
			}
		});
	}
	
	/**
	 * get unique name (in scope of current application process lifetime) of temporary file.
	 * Dot is not automatically appended before suffix
	 * @param ctx unique in scope of ctx application package
	 */
	public static File getUniqueName(Context ctx, String suffix) throws IOException {
		init();
		String name = PREFIX + sessionId + SEPARATOR + sequence++ + SEPARATOR + suffix;
		FileOutputStream file = ctx.openFileOutput(name, Context.MODE_PRIVATE);
		file.close();
		File cacheDir = ctx.getFilesDir();
		return new File(cacheDir, name);
	}

	private synchronized static void init() {
		if(!isInitialized) {
			sessionId = SystemClock.uptimeMillis();
			isInitialized = true;
		}
	}
}
