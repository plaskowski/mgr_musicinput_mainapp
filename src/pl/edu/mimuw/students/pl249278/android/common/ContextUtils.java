package pl.edu.mimuw.students.pl249278.android.common;

import android.content.Context;
import android.view.LayoutInflater;

public class ContextUtils {
	public static LayoutInflater getLayoutInflater(Context context) {
		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
