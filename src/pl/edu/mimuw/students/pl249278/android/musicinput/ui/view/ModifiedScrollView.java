package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ModifiedScrollView extends ScrollView {

	public ModifiedScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ModifiedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ModifiedScrollView(Context context) {
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
