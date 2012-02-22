package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.TouchInputLockable;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LockTouchInputStrategy extends View implements TouchInputLockable {
	protected static LogUtils log = new LogUtils(LockTouchInputStrategy.class);

	public LockTouchInputStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private boolean dispatchCancel = false;
	private boolean dispatchDown = false;
	private boolean touchInputLocked = false;
	
	@Override
	public void setTouchInputLocked(boolean setLocked) {
		if(touchInputLocked == setLocked) return;
		dispatchCancel = setLocked;
		dispatchDown = !setLocked;
		touchInputLocked = setLocked;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
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
			case MotionEvent.ACTION_POINTER_UP:
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
		if(touchInputLocked) {
			return true;
		}
//		logEv("dispatchEvent() normal route ", ev);
		return super.dispatchTouchEvent(ev);
	}

	private static void logEv(String descr, MotionEvent ev) {
		log.v(
			"%s action: %s, index: %d, pos: %fx%f",
			descr,
			ReflectionUtils.findConstName(MotionEvent.class, "ACTION_", ev.getActionMasked()),
			ev.getActionIndex(),
			ev.getX(),
			ev.getY()
		);
	}
}
