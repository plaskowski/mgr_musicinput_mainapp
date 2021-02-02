package pl.edu.mimuw.students.pl249278.android.musicinput.component;

import android.content.Intent;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;

public abstract class ManagedReceiver extends FilterByRequestIdReceiver {
	
	public ManagedReceiver() {
	}

	public ManagedReceiver(String currentRequestId) {
		super(currentRequestId);
	}

	@Override
	protected final void onSuccess(Intent response) {
		if(unregister()) {
			onSuccessReceived(response);
		}
	}
	
	@Override
	protected final void onFailure(Intent response) {
		if(unregister()) {
			onFailureReceived(response);
		}
	}
	
	protected abstract void onSuccessReceived(Intent response);
	protected abstract void onFailureReceived(Intent response);
	
	protected abstract boolean unregister();
}
