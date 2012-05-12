package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.component.ManagedReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import android.app.Activity;
import android.content.IntentFilter;

/** 
 * Encapsulated managing life cycle of single {@link FilterByRequestIdReceiver} that is bound to life cycle of the owner (Activity).
 * Receiver (by extending {@link SingleManagedReceiver}) is meant to be "single-shot".
 */
public class ManagedReceiverStrategy extends Activity {
	private SingleManagedReceiver managedReceiver = null;
	
	protected void registerManagedReceiver(SingleManagedReceiver receiver, String action) {
		unregisterManagedIfNotNull();
		registerReceiver(receiver, new IntentFilter(action));
		managedReceiver = receiver;
	}
	
	protected abstract class SingleManagedReceiver extends ManagedReceiver {
		@Override
		protected boolean unregister() {
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

