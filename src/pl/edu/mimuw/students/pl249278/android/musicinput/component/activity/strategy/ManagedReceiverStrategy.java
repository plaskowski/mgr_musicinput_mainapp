package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;

/** 
 * Encapsulated managing life cycle of single {@link FilterByRequestIdReceiver} that is bound to life cycle of the owner (Activity).
 * Receiver (by extending {@link ManagedReceiver}) is meant to be "single-shot".
 */
public class ManagedReceiverStrategy extends Activity {
	private ManagedReceiver managedReceiver = null;
	
	protected void registerManagedReceiver(ManagedReceiver receiver, String action) {
		unregisterManagedIfNotNull();
		registerReceiver(receiver, new IntentFilter(action));
		managedReceiver = receiver;
	}

	protected abstract class ManagedReceiver extends FilterByRequestIdReceiver {
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
		
		private boolean unregister() {
			if(this == managedReceiver) {
				unregisterManagedIfNotNull();
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		unregisterManagedIfNotNull();
		super.onDestroy();
	}

	private void unregisterManagedIfNotNull() {
		if(managedReceiver != null) {
			unregisterReceiver(managedReceiver);
			managedReceiver = null;
		}
	}
}

