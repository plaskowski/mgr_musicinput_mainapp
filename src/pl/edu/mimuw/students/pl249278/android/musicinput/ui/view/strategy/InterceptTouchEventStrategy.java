package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public abstract class InterceptTouchEventStrategy extends ViewGroup implements InterceptableTouch {

	public InterceptTouchEventStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private InterceptTouchDelegate interceptTouchDelegate;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(interceptTouchDelegate != null) {
			return interceptTouchDelegate.onInterceptTouchEvent(this, ev);
		}
		return super.onInterceptTouchEvent(ev);
	}

	public InterceptTouchDelegate getInterceptTouchDelegate() {
		return interceptTouchDelegate;
	}

	public void setInterceptTouchDelegate(
			InterceptTouchDelegate interceptTouchDelegate) {
		this.interceptTouchDelegate = interceptTouchDelegate;
	}

}
