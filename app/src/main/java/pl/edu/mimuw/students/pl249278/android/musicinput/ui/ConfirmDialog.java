package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener.DialogAction;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ConfirmDialog extends DialogFragment {
	private static final String ARG_TITLEID = "title_id";
	private static final String ARG_MSGID = "msg_id";
	private static final String ARG_POSITIVELABEL_ID = "oklabel";
	private static final String ARG_NEGATIVELABEL_ID = "cancellabel";
	private static final String ARG_NEUTRALLABEL_ID = "neutrallabel";
	private static final String ARG_DIALOG_ID = "dialog_id";
	private static final String ARG_MSG_PARAMS = "msg_string_params";
	private static final String ARG_STATE = "state";
	private static final String ARG_ICON_ID = "iconId";
	
	public static class ConfirmDialogBuilder {
		private Bundle args = new Bundle();
		
		public static ConfirmDialogBuilder init(int dialogId) {
			return new ConfirmDialogBuilder(dialogId);
		}
		public ConfirmDialogBuilder(int dialogId) {
			args.putInt(ARG_DIALOG_ID, dialogId);
		}
		public ConfirmDialogBuilder setState(Parcelable state) {
			args.putParcelable(ARG_STATE, state);
			return this;
		}
		public ConfirmDialogBuilder setTitle(int titleId) {
			args.putInt(ARG_TITLEID, titleId);
			return this;
		}
		public ConfirmDialogBuilder setMsgId(int msgId) {
			args.putInt(ARG_MSGID, msgId);
			return this;
		}
		public ConfirmDialogBuilder setMsg(int msgId, String msgParams[]) {
			setMsgId(msgId);
			args.putStringArray(ARG_MSG_PARAMS, msgParams);
			return this;
		}
		public ConfirmDialogBuilder setPositiveNegative(int positiveLabelId, int negativeLabelId) {
			args.putInt(ARG_POSITIVELABEL_ID, positiveLabelId);
			args.putInt(ARG_NEGATIVELABEL_ID, negativeLabelId);
			return this;
		}
		public ConfirmDialogBuilder setPositiveNeutral(int positiveLabelId, int neutralLabelId) {
			args.putInt(ARG_POSITIVELABEL_ID, positiveLabelId);
			args.putInt(ARG_NEUTRALLABEL_ID, neutralLabelId);
			return this;
		}
		public ConfirmDialogBuilder setButtons(int positiveLabelId, int neutralLabelId, int negativeLabelId) {
			this.setPositiveNegative(positiveLabelId, negativeLabelId);
			args.putInt(ARG_NEUTRALLABEL_ID, neutralLabelId);
			return this;
		}
		public ConfirmDialogBuilder setIcon(int iconId) {
			args.putInt(ARG_ICON_ID, iconId);
			return this;
		}
		public void showNew(FragmentManager manager, String tag) {
			ConfirmDialog dialog = new ConfirmDialog();
			dialog.setArguments(args);
	        dialog.show(manager, tag);
	    }
	}
	
	public static interface ConfirmDialogListener {
		public static enum DialogAction {
			BUTTON_POSITIVE,
			BUTTON_NEUTRAL,
			BUTTON_NEGATIVE,
			CANCEL
		}
		
		void onDialogResult(ConfirmDialog dialog, int dialogId, DialogAction action, Parcelable state);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    Bundle args = getArguments();
	    int msgId = args.getInt(ARG_MSGID, -1);
	    if(msgId != -1) {
			builder.setMessage(getString(msgId, 
	    		ifNotNull((Object[]) args.getStringArray(ARG_MSG_PARAMS), new Object[0])));
	    }
	    if(args.getInt(ARG_TITLEID) > 0) {
	    	builder.setTitle(args.getInt(ARG_TITLEID));
	    }
	    if(args.getInt(ARG_ICON_ID) > 0) {
	    	builder.setIcon(args.getInt(ARG_ICON_ID));
	    }
		builder
		.setCancelable(false)
		.setPositiveButton(getString(args.getInt(ARG_POSITIVELABEL_ID)),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FragmentActivity a = getActivity();
					if(a != null && a instanceof ConfirmDialogListener) {
						((ConfirmDialogListener) a).onDialogResult(
							ConfirmDialog.this, getArguments().getInt(ARG_DIALOG_ID), 
							DialogAction.BUTTON_POSITIVE,
							getArguments().getParcelable(ARG_STATE));
					}
				}
		});
		if(args.getInt(ARG_NEGATIVELABEL_ID) > 0) {
			builder.setNegativeButton(getString(args.getInt(ARG_NEGATIVELABEL_ID)), 
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FragmentActivity a = getActivity();
						if(a != null && a instanceof ConfirmDialogListener) {
							((ConfirmDialogListener) a).onDialogResult(
								ConfirmDialog.this, getArguments().getInt(ARG_DIALOG_ID), 
								DialogAction.BUTTON_NEGATIVE,
								getArguments().getParcelable(ARG_STATE));
						}
					}
			});
		}
		if(args.getInt(ARG_NEUTRALLABEL_ID) > 0) {
			builder.setNeutralButton(args.getInt(ARG_NEUTRALLABEL_ID), 
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					FragmentActivity a = getActivity();
					if(a != null && a instanceof ConfirmDialogListener) {
						((ConfirmDialogListener) a).onDialogResult(
							ConfirmDialog.this, getArguments().getInt(ARG_DIALOG_ID), 
							DialogAction.BUTTON_NEUTRAL,
							getArguments().getParcelable(ARG_STATE));
					}
				}
			});
		}
	    return builder.create();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		FragmentActivity a = getActivity();
		if(a != null && a instanceof ConfirmDialogListener) {
			((ConfirmDialogListener) a).onDialogResult(this, getArguments().getInt(ARG_DIALOG_ID), 
				DialogAction.CANCEL,
				getArguments().getParcelable(ARG_STATE));
		}
		super.onCancel(dialog);
	}
	
}
