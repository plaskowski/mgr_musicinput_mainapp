package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;

@SuppressWarnings("deprecation")
public class CorrectTopPositionStrategy extends ViewGroupStrategyBase {

	public CorrectTopPositionStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ViewGroup.class);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec, OnMeasureSuperCall superCall) {
		/* find min(child.layoutParams.topMargin) */
		int topMin = Integer.MAX_VALUE;
		int childCount = getChildCount();
		for(int i = 0; i < childCount; i++) {
			View view = getChildAt(i);
			topMin = Math.min(((AbsoluteLayout.LayoutParams) view.getLayoutParams()).y, topMin);
		}
		if(topMin != 0) {
			/* virtually translate all children so min(.topMargin) = 0 */
			for(int i = 0; i < childCount; i++) {
				View view = getChildAt(i);
				changeTop(view, -topMin);
			}
//			LogUtils.log(Log.DEBUG, LogUtils.COMMON_TAG, "MarginDrivenLayout::onMeasure(): changed margins by %d", -topMarginMin);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec, superCall);
	}
	
	private static void changeTop(View view, int delta) {
		AbsoluteLayout.LayoutParams layoutParams = (AbsoluteLayout.LayoutParams) view.getLayoutParams();
		layoutParams.y += delta;
		// we don't call setLayoutParams() because we are in layout pass
	}
}
