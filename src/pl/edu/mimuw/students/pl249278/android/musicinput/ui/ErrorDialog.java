package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class ErrorDialog extends DialogFragment {
	private static final String ARG_MSGID = "msg_id";
	private static final String ARG_THROWABLE = "throwable";
	private static final String ARG_LISTENER_ARG = "listener_arg";
	
	public static interface ErrorDialogListener {
		void onDismiss(ErrorDialog dialog, int arg);
	}

	public static ErrorDialog newInstance(Context ctx, int messageStringId, Throwable e, int listenerArg) {
		ErrorDialog dialog = new ErrorDialog();
		Bundle args = new Bundle();
        args.putInt(ARG_MSGID, messageStringId);
        args.putSerializable(ARG_THROWABLE, e);
        args.putInt(ARG_LISTENER_ARG, listenerArg);
		dialog.setArguments(args);
        return dialog;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		return new AlertDialog.Builder(getActivity())
		.setTitle(R.string.errordialog_title)
		.setMessage(args.getInt(ARG_MSGID))
		.setCancelable(true)
		.setNegativeButton(R.string.errordialog_button, null)
		.create();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof ErrorDialogListener) {
			((ErrorDialogListener) a).onDismiss(this, getArguments().getInt(ARG_LISTENER_ARG));
		}
		super.onDismiss(dialog);
	}
	
}
