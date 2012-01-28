package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LinearLayout extends android.widget.LinearLayout implements InterceptableView {

	public LinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		ExtendedResourcesFactory.loadExtendedBackground(this, context, attrs);
	}

	public LinearLayout(Context context) {
		super(context);
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
