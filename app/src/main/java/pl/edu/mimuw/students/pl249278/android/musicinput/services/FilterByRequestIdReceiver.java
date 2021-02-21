package pl.edu.mimuw.students.pl249278.android.musicinput.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.Macros;

import static pl.edu.mimuw.students.pl249278.android.common.LogUtils.commonLog;

public abstract class FilterByRequestIdReceiver extends BroadcastReceiver {
	private String currentRequestId = null;
	
	/**
	 * currentRequestId will be set to generated unique id
	 */
	public FilterByRequestIdReceiver() {
		this(null);
	}
	
	/**
	 * @param currentRequestId if null, unique id will be generated
	 */
	public FilterByRequestIdReceiver(String currentRequestId) {
		this.currentRequestId = Macros.ifNotNull(currentRequestId, getUniqueRequestID());
	}

	private String getUniqueRequestID() {
		return System.identityHashCode(this)+""+System.currentTimeMillis();
	}
	
	@Override
	public void onReceive(Context context, Intent response) {
		String id = AsyncHelper.getRequestId(response);
		if(id == null || currentRequestId == null || !id.equals(currentRequestId)) {
			commonLog.w("Received callback intent with invalid callback id = "+id+" when expected is "+currentRequestId);
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

	public String getCurrentRequestId() {
		return currentRequestId;
	}
}