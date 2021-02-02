package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class ProgressDialog extends DialogFragment {
	private static final String ARG_MSG_ID = "msgId";
	private static final String ARG_CANCELABLE = "cancelable";
	
	public static interface ProgressDialogListener {
		void onCancel(ProgressDialog dialog);
	}

	public static ProgressDialog newInstance(Context ctx, int msgId, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog();
		Bundle args = new Bundle();
        args.putInt(ARG_MSG_ID, msgId);
        args.putBoolean(ARG_CANCELABLE, cancelable);
		dialog.setArguments(args);
        return dialog;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		android.app.ProgressDialog dialog = new android.app.ProgressDialog(getActivity());
		dialog.setMessage(getResources().getString(args.getInt(ARG_MSG_ID)));
		dialog.setIndeterminate(true);
		dialog.setCancelable(args.getBoolean(ARG_CANCELABLE));
		return dialog;
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof ProgressDialogListener) {
			((ProgressDialogListener) a).onCancel(this);
		}
		super.onCancel(dialog);
	}
	
}
