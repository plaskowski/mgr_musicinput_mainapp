package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog.ProgressDialogListener;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

/**
 * Encapsulates showing progress dialog, which if dismissed causes finish() of current activity.
 * Progress dialog will be automatically dismissed if restart happens.
 */
public class InitialProgressDialogStrategy extends FragmentActivity implements ProgressDialogListener {
	private static final String DIALOGTAG_PROGRESS = "dialog_progress";
	
	@Override
	public void onCancel(ProgressDialog dialog) {
		// user dismissed "loading ..." dialog so we exit
		finish();
	}
	
	public void showProgressDialog() {
		FragmentUtils.showDialogFragment(this, DIALOGTAG_PROGRESS, 
				ProgressDialog.newInstance(this, R.string.msg_loading_please_wait, true));
	}
	
	public void hideProgressDialog() {
		FragmentUtils.dismissDialogFragment(this, DIALOGTAG_PROGRESS);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		FragmentUtils.dismissDialogFragment(this, DIALOGTAG_PROGRESS);
		super.onSaveInstanceState(outState);
	}
}
