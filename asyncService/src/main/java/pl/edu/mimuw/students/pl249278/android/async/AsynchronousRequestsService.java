package pl.edu.mimuw.students.pl249278.android.async;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;

public abstract class AsynchronousRequestsService extends IntentService {
	static final LogUtils log = new LogUtils(AsynchronousRequestsService.class);

	public AsynchronousRequestsService(String name) {
		super(name);
	}

	public static class ACTIONS {
		public static String REPEAT_CALLBACK = AsynchronousRequestsService.class.getName()+".repeat_callback";
		public static String CLEAN_CALLBACK = AsynchronousRequestsService.class.getName()+".clean_callback";
		public static final String EXTRAS_CALLBACK_PENDING_INTENT = "callback";
		public static final String EXTRAS_IS_REPEATABLE = "store_callback_content";
		public static final String EXTRAS_CALLBACKID_STRING = "callback_id";
	}
	
	static String CALLBACK_INTENT_EXTRAS_TYPE = AsynchronousRequestsService.class.getName()+".callback_type";
	
	static enum CallbackType {
		BROADCAST_RECEIVER,
		SERVICE;
	}
	
	public static class CALLBACK {
		public static final String EXTRAS_IS_SUCCESS = "is_success";
		public static final String EXTRAS_ERROR_MSG = "error_msg";
		public static final String EXTRAS_CALLBACKID_STRING = "callback_id";
	}

	@Override
	protected void onHandleIntent(Intent requestIntent) {
		String action = requestIntent.getAction();
		if(ACTIONS.REPEAT_CALLBACK.equals(action)) { 
			repeatCallback(requestIntent);
		} else if(ACTIONS.CLEAN_CALLBACK.equals(action)) { 
			cleanCallback(requestIntent);
		} else {
			log.e("Unhandled action type "+action);
			onRequestError(requestIntent, "Uknown action "+action);
		}
	}

	private static Map<String, Intent> repeatableCallbacksData = new HashMap<String, Intent>();
	
	private void saveRepeatable(Intent requestIntent, Intent outData) {
		boolean repeatable = requestIntent.getBooleanExtra(ACTIONS.EXTRAS_IS_REPEATABLE, false);
		String id = requestIntent.getStringExtra(ACTIONS.EXTRAS_CALLBACKID_STRING);
		if(repeatable && id != null) {
			if(repeatableCallbacksData.containsKey(id)) {
				log.w("saving repeatable callback with non-unique id "+id);
			}
			repeatableCallbacksData.put(id, outData);
		} else if(repeatable) {
			log.w("received request with is_repeatable TRUE but no callback id");
		}
	}
	
	private void cleanCallback(Intent requestIntent) {
		String id = requestIntent.getStringExtra(ACTIONS.EXTRAS_CALLBACKID_STRING);
		if(id == null) {
			log.d("received CLEAN_CALLBACK without callback_id");
		} else if(!repeatableCallbacksData.containsKey(id)) {
			log.d("received CLEAN_CALLBACK, but no persisted data with id "+id);
		} else {
			repeatableCallbacksData.remove(id);
			log.v("CLEAN_CALLBACK, cleaned data for id "+id);
		}
	}

	private void repeatCallback(Intent requestIntent) {
		String id = requestIntent.getStringExtra(ACTIONS.EXTRAS_CALLBACKID_STRING);
		if(id == null) {
			log.w("received REPEAT_CALLBACK without callback_id");
			onRequestError(requestIntent, "Callback ID extra required for "+requestIntent.getAction());
		} else if(!repeatableCallbacksData.containsKey(id)) {
			log.w("received REPEAT_CALLBACK, but no persisted data with id "+id);
			onRequestError(requestIntent, "Data not found for ID "+id);
		} else {
			log.v("REPEAT_CALLBACK for id "+id);
			doCallback(requestIntent, repeatableCallbacksData.get(id));
		}
	}

	protected void onRequestError(Intent requestIntent, String msg) {
		Intent data = new Intent();
		data.putExtra(CALLBACK.EXTRAS_IS_SUCCESS, false);
		data.putExtra(CALLBACK.EXTRAS_ERROR_MSG, msg);
		doCallback(requestIntent, data);
	}
	
	protected void onRequestSuccess(Intent requestIntent, Intent outData) {
		outData.putExtra(CALLBACK.EXTRAS_IS_SUCCESS, true);
		doCallback(requestIntent, outData);
	}
	
	private void doCallback(Intent requestIntent, Intent outData) {
		Parcelable callback = requestIntent.getParcelableExtra(ACTIONS.EXTRAS_CALLBACK_PENDING_INTENT);
		if(callback != null && callback instanceof Intent) {
			Intent callbackIntent = (Intent) callback;
			String callbackId = requestIntent.getStringExtra(ACTIONS.EXTRAS_CALLBACKID_STRING);
			if(callbackId != null) {
				outData.putExtra(CALLBACK.EXTRAS_CALLBACKID_STRING, callbackId);
			}
			saveRepeatable(requestIntent, outData);
			CallbackType callbackType;
			String typeStringRep = callbackIntent.getStringExtra(CALLBACK_INTENT_EXTRAS_TYPE);
			try {
				callbackType = CallbackType.valueOf(typeStringRep);
			} catch (Exception e) {
				log.w("Invalid CallbackType specified in callbackIntent: "+typeStringRep);
				callbackType = CallbackType.BROADCAST_RECEIVER;
			}
			callbackIntent.fillIn(outData, 0);
			switch(callbackType) {
			case BROADCAST_RECEIVER:
				sendBroadcast(callbackIntent);
				break;
			case SERVICE:
				startService(callbackIntent);
				break;
			}
		}
	}

}
