package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

public class HackedScrollView extends ScrollView_LockableScroll_LazyScrolling {

	public HackedScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		((HackedScrollViewChild) getChildAt(0)).setScrollViewportHeight(getMeasuredHeight());
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
