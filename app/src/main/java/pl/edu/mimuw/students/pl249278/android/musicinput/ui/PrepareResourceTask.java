package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.os.Handler;

public abstract class PrepareResourceTask<Key, Resource> implements Runnable {
	private Key key;
	private OnLoadedListener<Key, Resource> listener;
	private boolean emmitOnHandler = false;
	private Resource value;
	
	public interface OnLoadedListener<Key, Resource> {
		void onLoaded(PrepareResourceTask<Key, Resource> task, Key key, Resource value);
		Handler optionalHandler();
	}	
	
	public PrepareResourceTask(Key name, OnLoadedListener<Key, Resource> listener) {
		this.key = name;
		this.listener = listener;
	}
	
	@Override
	public void run() {
		if(!emmitOnHandler) {
			value = prepareValue(key);
			Handler handler = listener.optionalHandler();
			if(handler == null) {
				listener.onLoaded(this, key, value);
			} else {
				emmitOnHandler = true;
				handler.post(this);
				return;
			}
		} else {
			listener.onLoaded(this, key, value);
		}
	}
	
	public abstract Resource prepareValue(Key key);
}