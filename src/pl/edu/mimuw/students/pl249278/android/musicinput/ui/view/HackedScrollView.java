package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

public class HackedScrollView extends ScrollView_LockableScroll {

	public HackedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		((HackedScrollViewChild) getChildAt(0)).setScrollViewportHeight(getMeasuredHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(postLayoutScrollY) {
			postLayoutScrollY = false;
			scrollTo(0, scrollToY);
		}
	} 	
	
	private int scrollToY;
	private boolean postLayoutScrollY = false;

	void setScrollToY(int scrollToY) {
		this.scrollToY = scrollToY;
		postLayoutScrollY = true;
	}

}
