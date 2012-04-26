package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ErrorDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ErrorDialog.ErrorDialogListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

public class ErrorDialogStrategy extends FragmentActivity implements ErrorDialogListener {
	protected static final int ERRORDIALOG_CALLBACKARG_DO_FINISH = 1;
	protected static final String DIALOGTAG_ERROR = "errordialog";
	
	protected final void showErrorDialog(int messageStringId, Throwable e, boolean lazyFinish) {
		DialogFragment newFragment = ErrorDialog.newInstance(
			this, messageStringId, e, lazyFinish ? ERRORDIALOG_CALLBACKARG_DO_FINISH : 0);
		FragmentUtils.showDialogFragment(this, DIALOGTAG_ERROR, newFragment);
	}
	
	protected final void showErrorDialog(int messageStringId, int callbackArg) {
		DialogFragment newFragment = ErrorDialog.newInstance(this, messageStringId, null, callbackArg);
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
	public void onDismiss(ErrorDialog dialog, int arg) {
		if(arg == ERRORDIALOG_CALLBACKARG_DO_FINISH) {
			lazyFinishCleanup();
			finish();
		}
	}	
}
