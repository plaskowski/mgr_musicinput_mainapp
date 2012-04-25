package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public abstract class LockScrollingStrategy extends ViewGroup implements ScrollingLockable {
	
	public LockScrollingStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LockScrollingStrategy(Context context) {
		super(context);
	}

	private boolean scrollingLocked = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(scrollingLocked)
			return false;
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void setScrollingLocked(boolean scrollingLocked) {
		this.scrollingLocked = scrollingLocked;
	}
}
