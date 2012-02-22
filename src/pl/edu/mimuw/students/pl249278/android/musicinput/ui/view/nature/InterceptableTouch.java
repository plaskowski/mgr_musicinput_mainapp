package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import android.view.MotionEvent;

public interface InterceptableTouch {

	public interface InterceptTouchDelegate {
		public boolean onInterceptTouchEvent(android.view.View interceptableView, MotionEvent ev);
	}

	public InterceptTouchDelegate getInterceptTouchDelegate();

	public void setInterceptTouchDelegate(InterceptTouchDelegate interceptTouchDelegate);
}
