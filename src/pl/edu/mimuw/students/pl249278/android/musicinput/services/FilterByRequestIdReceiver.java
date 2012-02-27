package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public abstract class FilterByRequestIdReceiver extends BroadcastReceiver {
	private static final String TAG = LogUtils.COMMON_TAG;
	private String currentRequestId = null;
	
	public FilterByRequestIdReceiver(String currentRequestId) {
		this.currentRequestId = currentRequestId;
	}

	public String getUniqueRequestID(boolean generateNew) {
		if(generateNew) {
			currentRequestId = System.identityHashCode(this)+""+System.currentTimeMillis();
		}
		return currentRequestId;
	}
	
	@Override
	public void onReceive(Context context, Intent response) {
		String id = AsyncHelper.getRequestId(response);
		if(id == null || currentRequestId == null || !id.equals(currentRequestId)) {
			Log.w(TAG, "Received callback intent with invalid callback id = "+id+" when expected is "+currentRequestId);
			return;
		}
		if(AsyncHelper.isSuccess(response)) {
			onSuccess(response);
		} else {
			onFailure(response);
		}
	}
	
	protected abstract void onFailure(Intent response);
	protected abstract void onSuccess(Intent response);
}