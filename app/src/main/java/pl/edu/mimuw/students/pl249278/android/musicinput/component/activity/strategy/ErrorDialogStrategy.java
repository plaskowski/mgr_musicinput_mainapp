package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InfoDialog.InfoDialogListener;

public class ErrorDialogStrategy extends ActivityStrategyBase {
	public static final int ERRORDIALOG_CALLBACKARG_DO_FINISH = 1;
	protected static final String DIALOGTAG_ERROR = "errordialog";

	public ErrorDialogStrategy(ActivityStrategy parent) {
		super(parent);
	}

	public final void showErrorDialog(int messageStringId, Throwable e, boolean lazyFinish) {
		DialogFragment newFragment = InfoDialog.newInstance(
			getContext(), R.string.errordialog_title, messageStringId, R.string.errordialog_button,
			e, lazyFinish ? ERRORDIALOG_CALLBACKARG_DO_FINISH : 0);
		Preconditions.checkState(getContext() instanceof InfoDialogListener);
		FragmentUtils.showDialogFragment((FragmentActivity) getContext(), DIALOGTAG_ERROR, newFragment);
	}

	public final void showErrorDialogOnUiThread(final int messageStringId, final Throwable e, final boolean lazyFinish) {
		callbacks().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showErrorDialog(messageStringId, e, lazyFinish);
			}
		});
	}
	
	protected void lazyFinishCleanup() {}

	@Override
	public void onCustomEvent(CustomEventInterface customEvent) {
		if(customEvent instanceof InfoDialog.InfoDialogDismissalEvent
				&& ((InfoDialog.InfoDialogDismissalEvent) customEvent).getArg() == ERRORDIALOG_CALLBACKARG_DO_FINISH) {
			lazyFinishCleanup();
			callbacks().finish();
		} else {
			super.onCustomEvent(customEvent);
		}
	}

}
