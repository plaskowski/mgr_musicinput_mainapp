package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;

public class ViewHeightAnimation<Context> extends LayoutAnimator.LayoutAnimation<Context, View> {
	ViewHeightAnimation(View view, int start_value, int delta,
			long duration) {
		super(view, start_value, delta, duration);
	}
	@Override
	protected void apply(Context ctx, float state) {
		MarginLayoutParams layoutParams = (MarginLayoutParams) view.getLayoutParams();
		layoutParams.height = startValue() + (int) (delta * state);
		view.requestLayout();
	}
	
	public static class CollapseAnimation<Context> extends ViewHeightAnimation<Context> {
		public CollapseAnimation(View view, long duration) {
			super(view, view.getHeight(), -view.getHeight(), duration);
		}
	}
	public static class ExpandAnimation<Context> extends ViewHeightAnimation<Context> {
		public ExpandAnimation(View view, long duration) {
			super(view, 0, measuredHeight(view), duration);
		}

		private static int measuredHeight(View view) {
			view.measure(
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
			);
			return view.getMeasuredHeight();
		}		
	}
}