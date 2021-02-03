package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.MotionEvent;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;

public class LockScrollingStrategy extends ViewGroupStrategyBase {
	
	private boolean scrollingLocked = false;

	public LockScrollingStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ScrollingLockable.class);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall) {
		if(scrollingLocked)
			return false;
		return super.onInterceptTouchEvent(ev, superCall);
	}

	public void setScrollingLocked(boolean scrollingLocked) {
		this.scrollingLocked = scrollingLocked;
	}

}
