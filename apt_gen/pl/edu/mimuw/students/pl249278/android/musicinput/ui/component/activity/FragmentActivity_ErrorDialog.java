package pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ErrorDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ErrorDialog.ErrorDialogListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class FragmentActivity_ErrorDialog extends
		FragmentActivity_ErrorDialog_internals.ErrorDialogStrategy {
}

interface FragmentActivity_ErrorDialog_internals {
	/**
	 * @GeneratedAt Wed Mar 14 15:04:27 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ErrorDialogStrategy
	 */
	class ErrorDialogStrategy extends android.support.v4.app.FragmentActivity
			implements ErrorDialogListener {
		private static final int ERRORDIALOG_CALLBACK_DO_FINISH = 1;
		private static final String DIALOGTAG_ERROR = "errordialog";

		protected final void showErrorDialog(int messageStringId, Throwable e,
				boolean lazyFinish) {
			DialogFragment prev = (DialogFragment) getSupportFragmentManager()
					.findFragmentByTag(DIALOGTAG_ERROR);
			if (prev != null) {
				prev.dismiss();
			}
			DialogFragment newFragment = ErrorDialog.newInstance(this,
					messageStringId, e,
					lazyFinish ? ERRORDIALOG_CALLBACK_DO_FINISH : 0);
			newFragment.show(getSupportFragmentManager(), DIALOGTAG_ERROR);
		}

		protected void lazyFinishCleanup() {
		}

		@Override
		public final void onDismiss(ErrorDialog dialog, int arg) {
			if (arg == ERRORDIALOG_CALLBACK_DO_FINISH) {
				lazyFinishCleanup();
				finish();
			}
		}
	}
}