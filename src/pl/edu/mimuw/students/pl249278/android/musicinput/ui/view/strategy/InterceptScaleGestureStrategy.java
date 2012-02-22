package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewGroup;

public abstract class InterceptScaleGestureStrategy extends ViewGroup implements InterceptsScaleGesture {
	protected static final float MIN_SCALE_STEP = 0.05f;
	protected static final float MAX_SCALE_STEP = 0.5f;
	protected static LogUtils log = new LogUtils(InterceptScaleGestureStrategy.class);

	public InterceptScaleGestureStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private InterceptsScaleGesture.OnScaleListener onScaleListener = null;
	
	ScaleGestureDetector detector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector det) {
			logSG("onScaleBegin()", det);
			superDispatchCancelEvent();
			if(onScaleListener != null) {
				onScaleListener.onScaleBegin();
			}
			return true;
		}
		
		PointF point = new PointF();
		@Override
		public boolean onScale(ScaleGestureDetector arg0) {
			float step = Math.abs(arg0.getScaleFactor()-1f);
			boolean handled = step > MIN_SCALE_STEP && step < MAX_SCALE_STEP;
			if(handled && onScaleListener != null) {
				logSG("onScale()", arg0);
				point.set(arg0.getFocusX(), arg0.getFocusY());
				onScaleListener.onScale(arg0.getScaleFactor(), point);
			}
			return handled;
		}
		
		@Override
		public void onScaleEnd(ScaleGestureDetector arg0) {
			logSG("onScaleEnd()", arg0);
			if(onScaleListener != null) {
				onScaleListener.onScaleEnd();
			}
		}
		
		private void logSG(String label, ScaleGestureDetector det) {
			log.v(
				"%s detector state: (scaleF: %f, focusPoint: %fx%f)",
				label,
				det.getScaleFactor(),
				det.getFocusX(),
				det.getFocusY()
			);
		}
	});
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		detector.onTouchEvent(ev);
		if(detector.isInProgress()) {
			return true;
		} else {
			return super.dispatchTouchEvent(ev);
		}
	}
	
	protected void superDispatchCancelEvent() {
		MotionEvent ev =  MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
		super.dispatchTouchEvent(ev);
	}

	@Override
	public void setOnScaleListener(InterceptsScaleGesture.OnScaleListener onScaleListener) {
		this.onScaleListener = onScaleListener;
	}
}
