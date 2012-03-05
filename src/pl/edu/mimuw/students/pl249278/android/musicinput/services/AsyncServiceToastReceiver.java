package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AsyncServiceToastReceiver extends BroadcastReceiver {
	public static final String ACTION_TOAST = "pl.edu.mimuw.students.pl249278.android.musicinput.TOAST";
	public static final String EXTRAS_ONSUCCESS_TEXT = "TOAST_success";
	public static final String EXTRAS_ONFAILURE_TEXT = "TOAST_failure";
	public static final String EXTRAS_IS_LONG = "TOAST_islong";

	@Override
	public void onReceive(Context context, Intent intent) {
		Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		String which = AsyncHelper.isSuccess(intent) ? EXTRAS_ONSUCCESS_TEXT : EXTRAS_ONFAILURE_TEXT;
		toast.setText(intent.getStringExtra(which));
		if(intent.getBooleanExtra(EXTRAS_IS_LONG, false)) {
			toast.setDuration(Toast.LENGTH_LONG);
		}
		toast.show();
	}
	
	public static PendingIntent prepare(Context ctx, String successMsg, String failureMsg, boolean isLong) {
		Intent intent = new Intent(ACTION_TOAST);
		intent.putExtra(EXTRAS_ONSUCCESS_TEXT, successMsg);
		intent.putExtra(EXTRAS_ONFAILURE_TEXT, failureMsg);
		return prepare(ctx, intent, isLong);
	}
	private static PendingIntent prepare(Context ctx, Intent intent, boolean isLong) {
		intent.putExtra(EXTRAS_IS_LONG, isLong);
		return PendingIntent.getBroadcast(ctx, 0, intent, 0);
	}

}
