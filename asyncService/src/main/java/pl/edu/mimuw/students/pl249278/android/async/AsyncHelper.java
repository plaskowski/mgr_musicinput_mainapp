package pl.edu.mimuw.students.pl249278.android.async;

import pl.edu.mimuw.students.pl249278.android.async.AsynchronousRequestsService.CALLBACK;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

public class AsyncHelper {
	private static <T extends AsynchronousRequestsService> Intent prepareServiceIntent(Context ctx, Class<T> serviceClass, String serviceAction) {
		Intent i = new Intent(ctx, serviceClass);
		i.setAction(serviceAction);
		return i;
	}
	
	public static <T extends AsynchronousRequestsService> Intent prepareServiceIntent(Context ctx, Class<T> serviceClass, String serviceAction, String callbackId, Intent callbackIntent, boolean isRepeatable) {
		Intent i = prepareServiceIntent(ctx, serviceClass, serviceAction);
		putCallbackID(i, callbackId);
		setupCallback(ctx, i, callbackIntent, isRepeatable);
		return i;
	}

	private static void putCallbackID(Intent i, String callbackId) {
		i.putExtra(
			AsynchronousRequestsService.ACTIONS.EXTRAS_CALLBACKID_STRING, 
			callbackId
		);
	}
	
	public static <T extends AsynchronousRequestsService> Intent prepareCleanCallbackIntent(Context ctx, Class<T> serviceClass, String callbackToClearId) {
		Intent result = AsyncHelper.prepareServiceIntent(
			ctx, 
			serviceClass,
			AsynchronousRequestsService.ACTIONS.CLEAN_CALLBACK
		);
		putCallbackID(result, callbackToClearId);
		return result;
	}
	
	public static <T extends AsynchronousRequestsService> Intent prepareRepeatCallbackIntent(Context ctx, Class<T> serviceClass, String callbackId, Intent callbackIntent) {
		return prepareServiceIntent(
			ctx,
			serviceClass,
			AsynchronousRequestsService.ACTIONS.REPEAT_CALLBACK, 
			callbackId,
			callbackIntent,
			false
		);
	}
	
	private static <T extends AsynchronousRequestsService> void setupCallback(Context ctx, Intent serviceIntent, Intent callbackIntent, boolean isRepeatable) {
		serviceIntent.putExtra(AsynchronousRequestsService.ACTIONS.EXTRAS_CALLBACK_PENDING_INTENT, callbackIntent);
		if(isRepeatable) {
			serviceIntent.putExtra(AsynchronousRequestsService.ACTIONS.EXTRAS_IS_REPEATABLE, true);
		}
	}
	
	public static String getRequestId(Intent response) {
		return response.getStringExtra(CALLBACK.EXTRAS_CALLBACKID_STRING);
	}
	
	public static boolean isSuccess(Intent response) {
		return response.getBooleanExtra(CALLBACK.EXTRAS_IS_SUCCESS, false);
	}

	public static String getError(Intent response) {
		return response.getStringExtra(CALLBACK.EXTRAS_ERROR_MSG);
	}
	
	public static Intent getBroadcastCallback(String callbackAction) {
		Intent result = new Intent(callbackAction);
		result.putExtra(AsynchronousRequestsService.CALLBACK_INTENT_EXTRAS_TYPE, AsynchronousRequestsService.CallbackType.BROADCAST_RECEIVER.name());
		return result;
	}
	
	public static <ServiceType extends Service> Intent getServiceCallback(Context packageCtx, Class<ServiceType> serviceClass) {
		Intent result = new Intent(packageCtx, serviceClass);
		result.putExtra(AsynchronousRequestsService.CALLBACK_INTENT_EXTRAS_TYPE, AsynchronousRequestsService.CallbackType.SERVICE.name());
		return result;
	}
	
}
