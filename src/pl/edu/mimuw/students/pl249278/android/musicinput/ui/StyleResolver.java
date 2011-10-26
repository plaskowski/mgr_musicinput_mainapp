package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.content.res.TypedArray;

public interface StyleResolver {
	TypedArray obtainStyledAttributes(int[] attrs);
	TypedArray obtainStyledAttributes(int[] attrs, int styleId);
}
