package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import android.graphics.Point;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LazyTouchListener implements OnTouchListener, Runnable {
	private static LogUtils log = new LogUtils(LazyTouchListener.class);
	private static final int INVALID_POINTER = -1;
	private static Handler uiHandler;
	private int downPointerId = INVALID_POINTER;
	private Point downCoords = new Point(), lastCoords = new Point();
	private DelayedDownTouchListener touchDelegate;
	private boolean isDelegateActive = false;
	private int delay;
	
	public LazyTouchListener(int delay, DelayedDownTouchListener touchDelagate) {
		this.touchDelegate = touchDelagate;
		this.delay = delay;
		if(uiHandler == null) {
			uiHandler = new Handler();
		}
	}
	
	public static interface DelayedDownTouchListener extends OnTouchListener {
		
		public boolean tryActionDown(Point coords);

		public void actionDown(int downPointerId, Point downCoords,
				Point lastMoveCoords);
	}
	
	public void run() {
		if(downPointerId != INVALID_POINTER && touchDelegate.tryActionDown(downCoords)) {
			isDelegateActive = true;
			touchDelegate.actionDown(downPointerId, downCoords, lastCoords);
			downPointerId = INVALID_POINTER;
		}
	}	
	
	Point temp = new Point(), rebuildRange = new Point();
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int actionMasked = event.getActionMasked();
		if(isDelegateActive && actionMasked != MotionEvent.ACTION_DOWN) {
			return touchDelegate.onTouch(v, event);
		}
		switch(actionMasked) {
		case MotionEvent.ACTION_DOWN:
			isDelegateActive = false;
			copyCoords(event, event.getActionIndex(), downCoords);
			if(touchDelegate.tryActionDown(downCoords)) {
				copyCoords(event, event.getActionIndex(), lastCoords);
				downPointerId = pointerId(event);
				uiHandler.postDelayed(this, delay);
				return true;
			} else {
				return false;
			}
		case MotionEvent.ACTION_MOVE:
			if(pointerId(event) == downPointerId) {
				copyCoords(event, event.getActionIndex(), lastCoords);
				return true;
			} else {
				return false;
			}
		case MotionEvent.ACTION_POINTER_UP:
			if(pointerId(event) == downPointerId) {
				cancelDelayedRun();
				return true;
			} else {
				return false;
			}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(downPointerId != INVALID_POINTER) {
				cancelDelayedRun();
				return true;
			} else {
				return false;
			}
		default:
			return false;
		}
	}

	private static int pointerId(MotionEvent event) {
		return event.getPointerId(event.getActionIndex());
	}

	private static void copyCoords(MotionEvent event, int pointerIndex, Point point) {
		point.set((int) event.getX(pointerIndex), (int) event.getY(pointerIndex));
	}

	private void cancelDelayedRun() {
		if(downPointerId != INVALID_POINTER) {
			uiHandler.removeCallbacks(this);
			log.v("::cancel() before delayed ACTION_DOWN emitted.");
		}
		downPointerId = INVALID_POINTER;
	}

}
