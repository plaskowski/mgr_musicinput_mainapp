package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewGroup;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.TouchInputLockable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RelativeLayout_InterceptScale_LockTouchInput
		extends
		RelativeLayout_InterceptScale_LockTouchInput_internals.LockTouchInputStrategy {
	public RelativeLayout_InterceptScale_LockTouchInput(Context context,
			AttributeSet attrs) {
		super(context, attrs);
	}
}

interface RelativeLayout_InterceptScale_LockTouchInput_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LockTouchInputStrategy
	 */
	class LockTouchInputStrategy
			extends
			RelativeLayout_InterceptScale_LockTouchInput_internals.InterceptScaleGestureStrategy
			implements TouchInputLockable {
		protected static LogUtils log = new LogUtils(
				LockTouchInputStrategy.class);

		public LockTouchInputStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		private boolean dispatchCancel = false;
		private boolean dispatchDown = false;
		private boolean touchInputLocked = false;

		@Override
		public void setTouchInputLocked(boolean setLocked) {
			if (touchInputLocked == setLocked)
				return;
			dispatchCancel = setLocked;
			dispatchDown = !setLocked;
			touchInputLocked = setLocked;
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			if (dispatchCancel) {
				int action = ev.getAction();
				ev.setAction(MotionEvent.ACTION_CANCEL);
				super.dispatchTouchEvent(ev);
				ev.setAction(action);
				dispatchCancel = false;
			}
			if (dispatchDown) {
				switch (ev.getActionMasked()) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					logEv("dispatchDown := false, ignoring", ev);
					dispatchDown = false;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					logEv("dispatchDown = true, ignoring", ev);
					break;
				default:
					ev.setAction(MotionEvent.ACTION_DOWN);
					logEv("dispatchDown := false, dispatch faked", ev);
					super.dispatchTouchEvent(ev);
					dispatchDown = false;
				}
				return true;
			}
			if (touchInputLocked) {
				return true;
			}
			return super.dispatchTouchEvent(ev);
		}

		private static void logEv(String descr, MotionEvent ev) {
			log.v("%s action: %s, index: %d, pos: %fx%f", descr,
					ReflectionUtils.findConstName(MotionEvent.class, "ACTION_",
							ev.getActionMasked()), ev.getActionIndex(), ev
							.getX(), ev.getY());
		}
	}

	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptScaleGestureStrategy
	 */
	abstract class InterceptScaleGestureStrategy extends
			android.widget.RelativeLayout implements InterceptsScaleGesture {
		protected static final float MIN_SCALE_STEP = 0.05f;
		protected static final float MAX_SCALE_STEP = 0.5f;
		protected static LogUtils log = new LogUtils(
				InterceptScaleGestureStrategy.class);

		public InterceptScaleGestureStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		private InterceptsScaleGesture.OnScaleListener onScaleListener = null;
		ScaleGestureDetector detector = new ScaleGestureDetector(getContext(),
				new OnScaleGestureListener() {
					@Override
					public boolean onScaleBegin(ScaleGestureDetector det) {
						logSG("onScaleBegin()", det);
						superDispatchCancelEvent();
						if (onScaleListener != null) {
							onScaleListener.onScaleBegin();
						}
						return true;
					}

					PointF point = new PointF();

					@Override
					public boolean onScale(ScaleGestureDetector arg0) {
						float step = Math.abs(arg0.getScaleFactor() - 1f);
						boolean handled = step > MIN_SCALE_STEP
								&& step < MAX_SCALE_STEP;
						if (handled && onScaleListener != null) {
							logSG("onScale()", arg0);
							point.set(arg0.getFocusX(), arg0.getFocusY());
							onScaleListener.onScale(arg0.getScaleFactor(),
									point);
						}
						return handled;
					}

					@Override
					public void onScaleEnd(ScaleGestureDetector arg0) {
						logSG("onScaleEnd()", arg0);
						if (onScaleListener != null) {
							onScaleListener.onScaleEnd();
						}
					}

					private void logSG(String label, ScaleGestureDetector det) {
						log.v("%s detector state: (scaleF: %f, focusPoint: %fx%f)",
								label, det.getScaleFactor(), det.getFocusX(),
								det.getFocusY());
					}
				});

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			detector.onTouchEvent(ev);
			if (detector.isInProgress()) {
				return true;
			} else {
				return super.dispatchTouchEvent(ev);
			}
		}

		protected void superDispatchCancelEvent() {
			MotionEvent ev = MotionEvent.obtain(0, 0,
					MotionEvent.ACTION_CANCEL, 0, 0, 0);
			super.dispatchTouchEvent(ev);
		}

		@Override
		public void setOnScaleListener(
				InterceptsScaleGesture.OnScaleListener onScaleListener) {
			this.onScaleListener = onScaleListener;
		}
	}
}