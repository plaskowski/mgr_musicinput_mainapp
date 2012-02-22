package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.topMargin;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class RelativeLayout_CorrectTopMargin extends
		RelativeLayout_CorrectTopMargin_internals.CorrectTopMarginStrategy {
	public RelativeLayout_CorrectTopMargin(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RelativeLayout_CorrectTopMargin(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
}

interface RelativeLayout_CorrectTopMargin_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.CorrectTopMarginStrategy
	 */
	abstract class CorrectTopMarginStrategy extends
			android.widget.RelativeLayout {
		public CorrectTopMarginStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public CorrectTopMarginStrategy(Context context, AttributeSet attrs,
				int defStyle) {
			super(context, attrs, defStyle);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int topMarginMin = Integer.MAX_VALUE;
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				View view = getChildAt(i);
				topMarginMin = Math.min(topMargin(view), topMarginMin);
			}
			if (topMarginMin != 0) {
				for (int i = 0; i < childCount; i++) {
					View view = getChildAt(i);
					changeTopMargin(view, -topMarginMin);
				}
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		private static void changeTopMargin(View view, int dx) {
			ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view
					.getLayoutParams();
			layoutParams.topMargin += dx;
		}
	}
}