package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockScrollingStrategy extends DummyViewGroup implements ScrollingLockable {
	
	public LockScrollingStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public LockScrollingStrategy(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
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
