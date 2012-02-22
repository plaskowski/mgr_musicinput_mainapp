package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class HorizontalScrollView_InterceptOnScrollChanged
		extends
		HorizontalScrollView_InterceptOnScrollChanged_internals.InterceptOnScrollChangedStrategy {
	public HorizontalScrollView_InterceptOnScrollChanged(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}
}

interface HorizontalScrollView_InterceptOnScrollChanged_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptOnScrollChangedStrategy
	 */
	abstract class InterceptOnScrollChangedStrategy extends
			android.widget.HorizontalScrollView implements
			InterceptableOnScrollChanged {
		public InterceptOnScrollChangedStrategy(Context context,
				AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			if (listener != null) {
				listener.onScrollChanged(l, oldl);
			}
		}

		private OnScrollChangedListener listener = null;

		public void setListener(OnScrollChangedListener listener) {
			this.listener = listener;
		}
	}
}