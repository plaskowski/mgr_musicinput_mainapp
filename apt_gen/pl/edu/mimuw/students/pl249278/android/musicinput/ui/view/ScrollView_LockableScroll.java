package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class ScrollView_LockableScroll extends
		ScrollView_LockableScroll_internals.LockScrollingStrategy {
	public ScrollView_LockableScroll(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}

interface ScrollView_LockableScroll_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LockScrollingStrategy
	 */
	abstract class LockScrollingStrategy extends android.widget.ScrollView
			implements ScrollingLockable {
		public LockScrollingStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		private boolean scrollingLocked = false;

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			if (scrollingLocked)
				return false;
			return super.onInterceptTouchEvent(ev);
		}

		@Override
		public void setScrollingLocked(boolean scrollingLocked) {
			this.scrollingLocked = scrollingLocked;
		}
	}
}