package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.MotionEvent;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable.OnInterceptListener;

public class DetectTouchInterceptionStrategy extends ViewGroupStrategyBase {

	private OnInterceptListener listener;

	public DetectTouchInterceptionStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(OnInterceptTouchObservable.class);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev, OnInterceptTouchEventSuperCall superCall) {
		boolean superResult = super.onInterceptTouchEvent(ev, superCall);
		if(listener != null && superResult) {
			listener.onTouchIntercepted();
		}
		return superResult;
	}
	
	public void setListener(OnInterceptListener listener) {
		this.listener = listener;
	}
	
}
