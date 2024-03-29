package pl.edu.mimuw.students.pl249278.android.common;

import android.util.Log;

public class LogUtils {
	public static final LogUtils commonLog = new LogUtils("pl249278.android");

	private static void log(int priority, String tag, String format, Object... args) {
		String msg;
		if(args.length > 0) {
			msg = String.format(format, args);
		} else {
			msg = format;
		}
		Log.println(priority, tag, msg);
	}
	
	private String tag;
	public LogUtils(Class<?> cl) {
		this(cl.getName());
	}
	public LogUtils(String tag) {
		this.tag = tag;
	}

	public void i(String format, Object... args) {
		log(Log.INFO, tag, format, args);
	}
	
	public void d(String format, Object... args) {
		log(Log.DEBUG, tag, format, args);
	}
	
	public void v(String format, Object... args) {
		log(Log.VERBOSE, tag, format, args);
	}

	public void w(String msg, Throwable tr) {
		Log.w(tag, msg, tr);
	}

	public void w(String format, Object... args) {
		log(Log.WARN, tag, format, args);
	}
	
	public void e(String msg) {
		Log.e(tag, msg);
	}
	
	public void e(String msg, Throwable tr) {
		Log.e(tag, msg, tr);
	}

}
