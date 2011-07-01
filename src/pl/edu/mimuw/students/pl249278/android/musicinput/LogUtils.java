package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.util.Log;

public class LogUtils {
	private static final String TAG = "MusicInput";

	public static void info(String format, Object... args) {
		Log.i(TAG, String.format(format, args));
	}
}
