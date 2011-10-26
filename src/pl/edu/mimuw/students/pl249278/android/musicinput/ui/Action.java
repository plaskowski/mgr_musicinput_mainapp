package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;

public interface Action {
	SvgImage icon();
	void perform();
	/** if action is toogle kind, return current state, otherwise null */
	Boolean getState();
}