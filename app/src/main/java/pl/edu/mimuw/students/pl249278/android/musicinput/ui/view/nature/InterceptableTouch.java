package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import android.view.MotionEvent;

public interface InterceptableTouch {

	interface InterceptTouchDelegate {
		boolean onInterceptTouchEvent(android.view.View interceptableView, MotionEvent ev);
	}

	void setInterceptTouchDelegate(InterceptTouchDelegate interceptTouchDelegate);
}
