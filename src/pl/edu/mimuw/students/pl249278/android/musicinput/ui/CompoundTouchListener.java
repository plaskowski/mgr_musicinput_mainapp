package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class CompoundTouchListener implements OnTouchListener {
	
	private OnTouchListener[] listeners;
	private OnTouchListener current;
	public CompoundTouchListener(OnTouchListener... listeners) {
		this.listeners = listeners;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			current = null;
			for(int i = 0; i < listeners.length; i++) {
				if(listeners[i].onTouch(v, event)) {
					current = listeners[i];
					return true;
				}
			}
		} else if(current != null) {
			return current.onTouch(v, event);
		}
		return false;
	}

}
