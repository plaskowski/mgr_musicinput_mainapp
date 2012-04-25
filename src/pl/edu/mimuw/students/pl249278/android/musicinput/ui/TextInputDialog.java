package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TextInputDialog extends DialogFragment {
	private static final LogUtils log = new LogUtils(TextInputDialog.class);
	private static final String ARG_VALUE_ID = "value_id";
	private static final String ARG_LISTENER_ARG = "listener_arg";
	private static final String ARG_TITLE = "title";
	private static final String ARG_POSITIVE_LABEL = "okid";
	private static final String ARG_NEGATIVE_LABEL = "cancelid";
	private static final String ARG_INITIAL_VALUE = "value";
	
	public static interface TextInputDialogListener {
		void onValueEntered(TextInputDialog dialog, int valueId, long listenerArg, String value);
		void onDismiss(TextInputDialog dialog, int valueId, long listenerArg);
	}

	public static TextInputDialog newInstance(Context ctx, int valueId, long listenerArg, 
		String title, int positiveLabelId, int negativeLabelId, String initialValue) {
		TextInputDialog dialog = new TextInputDialog();
		Bundle args = new Bundle();
        args.putInt(ARG_VALUE_ID, valueId);
        args.putLong(ARG_LISTENER_ARG, listenerArg);
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_POSITIVE_LABEL, positiveLabelId);
        args.putInt(ARG_NEGATIVE_LABEL, negativeLabelId);
        args.putString(ARG_INITIAL_VALUE, initialValue);
		dialog.setArguments(args);
        return dialog;
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context ctx = getActivity();
		Bundle args = getArguments();
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(args.getString(ARG_TITLE))
         .setCancelable(true)
         .setPositiveButton(ctx.getString(args.getInt(ARG_POSITIVE_LABEL)), new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
					Activity act = getActivity();
					if(act == null || !(act instanceof TextInputDialogListener)) {
						log.w("[OK] received when no activity implementing result interface is bound: %s", act);
					} else {
						Bundle args = getArguments();
						((TextInputDialogListener) act).onValueEntered(
							TextInputDialog.this, 
							args.getInt(ARG_VALUE_ID), args.getLong(ARG_LISTENER_ARG), 
							((TextView) getDialog().findViewById(R.id.valueField)).getText().toString()
						);
					}
			   }
         })
		.setNegativeButton(ctx.getString(args.getInt(ARG_NEGATIVE_LABEL)), null);
		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View promptLayout = inflater.inflate(R.layout.prompt_string, null);
		builder.setView(promptLayout);
		final AlertDialog alert = builder.create();
		TextView field = (TextView) promptLayout.findViewById(R.id.valueField);
		field.setText(args.getString(ARG_INITIAL_VALUE));
		return alert;
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof TextInputDialogListener) {
			((TextInputDialogListener) a).onDismiss(this, getArguments().getInt(ARG_VALUE_ID), getArguments().getLong(ARG_LISTENER_ARG));
		}
		super.onDismiss(dialog);
	}
	
}
