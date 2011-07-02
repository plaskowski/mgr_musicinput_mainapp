package pl.edu.mimuw.students.pl249278.android.common;

import android.util.Log;

public class LogUtils {
	private static final String TAG = "pl249278.android";

	public static void info(String format, Object... args) {
		Log.i(TAG, String.format(format, args));
	}
}
