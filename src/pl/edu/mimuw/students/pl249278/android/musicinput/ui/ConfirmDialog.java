package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class ConfirmDialog extends DialogFragment {
	private static final String ARG_MSGID = "msg_id";
	private static final String ARG_POSITIVELABEL_ID = "oklabel";
	private static final String ARG_NEGATIVELABEL_ID = "cancellabel";
	private static final String ARG_DIALOG_ID = "dialog_id";
	private static final String ARG_CALLBACK_PARAM = "callback_param";
	private static final String ARG_MSG_PARAMS = "msg_string_params";
	
	public static interface ConfirmDialogListener {
		void onConfirm(ConfirmDialog dialog, int dialogId, long callbackParam);
		void onCancel(ConfirmDialog dialog, int dialogId, long callbackParam);		
	}

	public static ConfirmDialog newInstance(Context ctx, int dialogId, long callbackParam, int msgId, String[] msgParams, int positiveLabelId, int negativeLabelId) {
		ConfirmDialog dialog = new ConfirmDialog();
		Bundle args = new Bundle();
		args.putInt(ARG_DIALOG_ID, dialogId);
		args.putLong(ARG_CALLBACK_PARAM, callbackParam);
		args.putInt(ARG_MSGID, msgId);
		args.putInt(ARG_POSITIVELABEL_ID, positiveLabelId);
		args.putInt(ARG_NEGATIVELABEL_ID, negativeLabelId);
		args.putStringArray(ARG_MSG_PARAMS, msgParams);
		dialog.setArguments(args);
        return dialog;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    Bundle args = getArguments();
	    String msg = getString(args.getInt(ARG_MSGID), 
    		ifNotNull((Object[]) args.getStringArray(ARG_MSG_PARAMS), new Object[0]));
		builder
	    .setMessage(msg)
		.setCancelable(false)
		.setPositiveButton(getString(args.getInt(ARG_POSITIVELABEL_ID)),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FragmentActivity a = getActivity();
					if(a != null && a instanceof ConfirmDialogListener) {
						((ConfirmDialogListener) a).onConfirm(
							ConfirmDialog.this, getArguments().getInt(ARG_DIALOG_ID), 
							getArguments().getLong(ARG_CALLBACK_PARAM));
					}
				}
			})
		.setNegativeButton(getString(args.getInt(ARG_NEGATIVELABEL_ID)), null);
	    return builder.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof ConfirmDialogListener) {
			((ConfirmDialogListener) a).onCancel(this, getArguments().getInt(ARG_DIALOG_ID), 
				getArguments().getLong(ARG_CALLBACK_PARAM));
		}
		super.onCancel(dialog);
	}
	
}
