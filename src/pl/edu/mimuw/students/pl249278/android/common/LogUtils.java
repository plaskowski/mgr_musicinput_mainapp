package pl.edu.mimuw.students.pl249278.android.common;

import android.util.Log;

public class LogUtils {
	private static final String TAG = "pl249278.android";

	public static void info(String format, Object... args) {
		Log.i(TAG, String.format(format, args));
	}
	
	public static void tinfo(String tag, String format, Object... args) {
		Log.i(tag, String.format(format, args));
	}
	
	private String tag;
	public LogUtils(Class<?> cl) {
		this(cl.getName());
	}
	public LogUtils(String tag) {
		this.tag = tag;
	}

	public void i(String format, Object... args) {
		tinfo(tag, format, args);
	}
}
