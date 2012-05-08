package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.Macros;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

public class InfoDialog extends DialogFragment {
	private static final String[] EMPTY_MSG_ARGS = new String[0];
	
	private static final String ARG_MSGID = "msg_id";
	private static final String ARG_TITLEID = "TITLE_id";
	private static final String ARG_THROWABLE = "throwable";
	private static final String ARG_LISTENER_ARG = "listener_arg";
	private static final String ARG_MSG_ARGS = "msg_args";
	private static final String ARG_BUTTONLABEL_ID = "buttonLabelId";
	
	public static interface InfoDialogListener {
		void onDismiss(InfoDialog dialog, int arg);
	}

	public static InfoDialog newInstance(Context ctx, int titleId, int messageStringId, int buttonLabelId, Throwable e, int listenerArg) {
		Bundle args = new Bundle();
		args.putInt(ARG_TITLEID, titleId);
		args.putInt(ARG_MSGID, messageStringId);
		args.putStringArray(ARG_MSG_ARGS, null);
        args.putInt(ARG_BUTTONLABEL_ID, buttonLabelId);
        args.putSerializable(ARG_THROWABLE, e);
		args.putInt(ARG_LISTENER_ARG, listenerArg);
		InfoDialog dialog = new InfoDialog();
		dialog.setArguments(args);
		return dialog;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		View view = getActivity().getLayoutInflater().inflate(R.layout.popup_longinfo, null);
		String msg = getString(args.getInt(ARG_MSGID), (Object[]) Macros.ifNotNull(args.getStringArray(ARG_MSG_ARGS), EMPTY_MSG_ARGS));
		((TextView) view.findViewById(android.R.id.message)).setText(msg);
		AlertDialog builder = new AlertDialog.Builder(getActivity())
		.setView(view)
		.setCancelable(true)
		.setNegativeButton(args.getInt(ARG_BUTTONLABEL_ID, android.R.string.ok), null)
		.create();
		if(args.containsKey(ARG_TITLEID)) {
			builder.setTitle(args.getInt(ARG_TITLEID, R.string.errordialog_title));
		}
		return builder;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof InfoDialogListener) {
			((InfoDialogListener) a).onDismiss(this, getArguments().getInt(ARG_LISTENER_ARG));
		}
		super.onDismiss(dialog);
	}
	
}
