package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener.DialogAction;
import android.os.Parcelable;

/** Dummy class that provides empty implementation for {@link ConfirmDialogListener} */
class EmptyConfirmDialogListener {
	public void onDialogResult(ConfirmDialog dialog, int dialogId,
			DialogAction action, Parcelable state) {
	}
}