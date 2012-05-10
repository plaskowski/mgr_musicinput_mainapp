package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.topMargin;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CorrectTopMarginStrategy extends DummyViewGroup {

	public CorrectTopMarginStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CorrectTopMarginStrategy(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/* find min(child.layoutParams.topMargin) */
		int topMarginMin = Integer.MAX_VALUE;
		int childCount = getChildCount();
		for(int i = 0; i < childCount; i++) {
			View view = getChildAt(i);
			topMarginMin = Math.min(topMargin(view), topMarginMin);
		}
		if(topMarginMin != 0) {
			/* virtually translate all children so min(.topMargin) = 0 */
			for(int i = 0; i < childCount; i++) {
				View view = getChildAt(i);
				changeTopMargin(view, -topMarginMin);
			}
//			LogUtils.log(Log.DEBUG, LogUtils.COMMON_TAG, "MarginDrivenLayout::onMeasure(): changed margins by %d", -topMarginMin);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private static void changeTopMargin(View view, int dx) {
		ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
		layoutParams.topMargin += dx;
		// we don't call setLayoutParams() because we are in layout pass
	}
}
