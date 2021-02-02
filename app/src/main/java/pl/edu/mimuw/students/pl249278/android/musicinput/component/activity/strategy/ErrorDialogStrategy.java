package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog.InfoDialogListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class ErrorDialogStrategy extends FragmentActivity implements InfoDialogListener {
	protected static final int ERRORDIALOG_CALLBACKARG_DO_FINISH = 1;
	protected static final String DIALOGTAG_ERROR = "errordialog";
	
	protected final void showErrorDialog(int messageStringId, Throwable e, boolean lazyFinish) {
		DialogFragment newFragment = InfoDialog.newInstance(
			this, R.string.errordialog_title, messageStringId, R.string.errordialog_button,
			e, lazyFinish ? ERRORDIALOG_CALLBACKARG_DO_FINISH : 0);
		FragmentUtils.showDialogFragment(this, DIALOGTAG_ERROR, newFragment);
	}
	
	protected final void showErrorDialogOnUiThread(final int messageStringId, final Throwable e, final boolean lazyFinish) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showErrorDialog(messageStringId, e, lazyFinish);
			}
		});
	}
	
	protected void lazyFinishCleanup() {
	}
	
	@Override
	public void onDismiss(InfoDialog dialog, int arg) {
		if(arg == ERRORDIALOG_CALLBACKARG_DO_FINISH) {
			lazyFinishCleanup();
			finish();
		}
	}	
}
