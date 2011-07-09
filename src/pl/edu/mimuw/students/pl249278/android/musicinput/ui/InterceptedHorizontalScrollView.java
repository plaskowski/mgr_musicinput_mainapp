package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class InterceptedHorizontalScrollView extends HorizontalScrollView {
	private static LogUtils log = new LogUtils(InterceptedHorizontalScrollView.class);  
	
	public InterceptedHorizontalScrollView(Context context) {
		super(context);
	}

	public InterceptedHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public InterceptedHorizontalScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(listener != null) {
			listener.onScrollChanged(l, oldl);
		}
	}
	
	private OnScrollChangedListener listener = null;
	

	public static interface OnScrollChangedListener {
		void onScrollChanged(int l, int oldl);
	}


	public void setListener(OnScrollChangedListener listener) {
		this.listener = listener;
	}
}
