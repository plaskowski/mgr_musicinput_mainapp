package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class ViewUtils {
	public static interface OnLayoutListener {
		void onLayoutPassed();
	}
	
	public static void addActivityOnLayout(Activity activity, OnLayoutListener listener) {
		View rootView = activity.findViewById(android.R.id.content);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ContextViewListener(rootView, listener));
	}
	
	private static class ContextViewListener implements OnGlobalLayoutListener {
		private View contextView;
		private OnLayoutListener listener;
				
		public ContextViewListener(View contextView, OnLayoutListener listener) {
			this.contextView = contextView;
			this.listener = listener;
		}

		@Override
		public void onGlobalLayout() {
			contextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			listener.onLayoutPassed();
			contextView = null;
			listener = null;
		}
	}
	
	/**
	 * @return at which index is child directly in container, or -1 if is not in.
	 */
	public static int indexOf(ViewGroup container, View child) {
		int total = container.getChildCount();
		for(int i = 0; i < total; i++) {
			if(container.getChildAt(i) == child) {
				return i;
			}
		}
		return -1;
	}
}
