package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Thread to execute ordered tasks sequentially on it.
 * Queue tasks by {@link Handler#post(Runnable)} on {@link #handler}.
 * Started in constructor.
 * {@link #postQuit()} must be called when not needed anymore to quit thread.
 */
public class WorkerThread extends HandlerThread {
	private Handler handler = null;
	private boolean startCalled = false;
	
	public WorkerThread(String name) {
		super(name);
	}
	
	public void postQuit() {
		startIfNeccesary();
		handler.removeCallbacksAndMessages(null);
		handler.post(new Runnable() {
			@Override
			public void run() {
				Looper.myLooper().quit();
			}
		});
	}

	public void post(Runnable task) {
		startIfNeccesary();
		this.handler.post(task);
	}

	private void startIfNeccesary() {
		if(!startCalled) {
			start();
			startCalled = true;
			handler = new Handler(getLooper());
		}
	}
}
