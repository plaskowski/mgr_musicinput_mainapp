package pl.edu.mimuw.students.pl249278.android.common;

public class Macros {
	public static <T> T ifNotNull(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}
}
