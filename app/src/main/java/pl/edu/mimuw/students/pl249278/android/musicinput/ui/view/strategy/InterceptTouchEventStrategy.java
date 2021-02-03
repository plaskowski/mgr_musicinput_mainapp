package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.MotionEvent;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch.InterceptTouchDelegate;

public class InterceptTouchEventStrategy extends ViewGroupStrategyBase {

	private InterceptTouchDelegate interceptTouchDelegate;

	public InterceptTouchEventStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(InterceptableTouch.class);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall) {
		if(interceptTouchDelegate != null) {
			return interceptTouchDelegate.onInterceptTouchEvent(internals().viewObject(), ev);
		}
		return super.onInterceptTouchEvent(ev, superCall);
	}

	public void setInterceptTouchDelegate(
			InterceptTouchDelegate interceptTouchDelegate) {
		this.interceptTouchDelegate = interceptTouchDelegate;
	}

}
