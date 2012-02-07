package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {

	public LockableScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LockableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LockableScrollView(Context context) {
		super(context);
	}
	
	private boolean verticalScrollingLocked = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(verticalScrollingLocked)
			return false;
		return super.onInterceptTouchEvent(ev);
	}

	public void setVerticalScrollingLocked(boolean verticalScrollingLocked) {
		this.verticalScrollingLocked = verticalScrollingLocked;
	}
}
