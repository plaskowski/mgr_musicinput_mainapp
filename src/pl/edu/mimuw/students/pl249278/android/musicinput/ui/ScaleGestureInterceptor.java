package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.FrameLayout;

public class ScaleGestureInterceptor extends FrameLayout {
	protected static final float MIN_SCALE_STEP = 0.05f;

	static LogUtils log = new LogUtils(ScaleGestureInterceptor.class);

	public ScaleGestureInterceptor(Context context) {
		super(context);
	}

	public ScaleGestureInterceptor(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScaleGestureInterceptor(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public interface OnScaleListener {
		void onScale(float scaleFactor, PointF focusPoint);
		void onScaleEnd();
	}
	private OnScaleListener onScaleListener = null;
	
	ScaleGestureDetector detector = new ScaleGestureDetector(getContext(), new OnScaleGestureListener() {
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector det) {
			logSG("onScaleBegin()", det);
			dispatchCancel = true;
			return true;
		}
		
		PointF point = new PointF();
		@Override
		public boolean onScale(ScaleGestureDetector arg0) {
			boolean handled = Math.abs(arg0.getScaleFactor()-1f) > MIN_SCALE_STEP;
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
			log.i(
				"%s detector state: (scaleF: %f, focusPoint: %fx%f)",
				label,
				det.getScaleFactor(),
				det.getFocusX(),
				det.getFocusY()
			);
		}
	});
	
	private boolean dispatchCancel = false;
	private boolean dispatchDown = false;
	private boolean touchInputLocked = false;
	
	public void setTouchInputLocked(boolean setLocked) {
		if(touchInputLocked == setLocked) return;
		dispatchCancel = setLocked;
		dispatchDown = !setLocked;
		touchInputLocked = setLocked;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(!touchInputLocked) {
			detector.onTouchEvent(ev);
		}
		if(dispatchCancel) {
			int action = ev.getAction();
			ev.setAction(MotionEvent.ACTION_CANCEL);
			super.dispatchTouchEvent(ev);
			ev.setAction(action);
			dispatchCancel = false;
		}
		if(dispatchDown) {
			switch(ev.getActionMasked()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				// touch input ended, no finger on screen
				logEv("dispatchDown := false, ignoring", ev);
				dispatchDown = false;
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
				// we ignore this and wait for other event
				logEv("dispatchDown = true, ignoring", ev);
				break;
			default:
				/* 
				 * event that is continuation of touch input 
				 * we fake ACTION_DOWN
				 * to let descendants handle this input 
				 */
				ev.setAction(MotionEvent.ACTION_DOWN);
				logEv("dispatchDown := false, dispatch faked", ev);
				super.dispatchTouchEvent(ev);
				dispatchDown = false;
			}
			return true;
		}
		if(touchInputLocked || detector.isInProgress()) {
			return true;
		}
		logEv("dispatchEvent() normal route ", ev);
		return super.dispatchTouchEvent(ev);
	}

	private static void logEv(String descr, MotionEvent ev) {
		log.i(
			"%s action: %s, index: %d, pos: %fx%f",
			descr,
			ReflectionUtils.findConst(MotionEvent.class, "ACTION_", ev.getActionMasked()),
			ev.getActionIndex(),
			ev.getX(),
			ev.getY()
		);
	}

	public void setOnScaleListener(OnScaleListener onScaleListener) {
		this.onScaleListener = onScaleListener;
	}
}
