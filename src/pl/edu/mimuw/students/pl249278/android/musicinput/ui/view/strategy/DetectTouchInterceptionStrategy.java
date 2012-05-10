package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DetectTouchInterceptionStrategy extends DummyViewGroup implements OnInterceptTouchObservable {
	private OnInterceptListener listener;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean superResult = super.onInterceptTouchEvent(ev);
		if(listener != null && superResult) {
			listener.onTouchIntercepted();
		}
		return superResult;
	}
	
	@Override
	public void setListener(OnInterceptListener listener) {
		this.listener = listener;
	}
	
	public DetectTouchInterceptionStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public DetectTouchInterceptionStrategy(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public DetectTouchInterceptionStrategy(Context context) {
		super(context);
	}
	
}
