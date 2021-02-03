package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.component.ManagedReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import android.app.Activity;
import android.content.IntentFilter;

import com.google.common.base.Preconditions;

/** 
 * Encapsulated managing life cycle of single {@link FilterByRequestIdReceiver} that is bound to life cycle of the owner (Activity).
 * Receiver (by extending {@link SingleManagedReceiver}) is meant to be "single-shot".
 */
public class ManagedReceiverStrategy extends ActivityStrategyBase {
	private SingleManagedReceiver managedReceiver = null;

	public ManagedReceiverStrategy(ActivityStrategy parent) {
		super(parent);
	}

	public void registerManagedReceiver(SingleManagedReceiver receiver, String action) {
		receiver.outerObject = this;
		unregisterManagedIfNotNull();
		callbacks().registerReceiver(receiver, new IntentFilter(action));
		managedReceiver = receiver;
	}

	public static abstract class SingleManagedReceiver extends ManagedReceiver {
		private ManagedReceiverStrategy outerObject;

		public SingleManagedReceiver() {}

		@Override
		protected boolean unregister() {
			if(this == outerObject.managedReceiver) {
				outerObject.unregisterManagedIfNotNull();
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public void onDestroy(OnDestroySuperCall superCall) {
		unregisterManagedIfNotNull();
		super.onDestroy(superCall);
	}

	private void unregisterManagedIfNotNull() {
		if(managedReceiver != null) {
			callbacks().unregisterReceiver(managedReceiver);
			managedReceiver = null;
		}
	}
}

