package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class LinearLayout_InterceptTouch extends
		LinearLayout_InterceptTouch_internals.InterceptTouchEventStrategy {
	public LinearLayout_InterceptTouch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}

interface LinearLayout_InterceptTouch_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptTouchEventStrategy
	 */
	abstract class InterceptTouchEventStrategy extends
			android.widget.LinearLayout implements InterceptableTouch {
		public InterceptTouchEventStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		private InterceptTouchDelegate interceptTouchDelegate;

		@Override
		public boolean onInterceptTouchEvent(MotionEvent ev) {
			if (interceptTouchDelegate != null) {
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
}