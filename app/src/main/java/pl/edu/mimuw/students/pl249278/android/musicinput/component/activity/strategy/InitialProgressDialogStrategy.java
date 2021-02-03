package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.FragmentUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ProgressDialog.ProgressDialogListener;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.google.common.base.Preconditions;

/**
 * Encapsulates showing progress dialog, which if dismissed causes finish() of current activity.
 * Progress dialog will be automatically dismissed if restart happens.
 */
public class InitialProgressDialogStrategy extends ActivityStrategyBase {
	private static final String DIALOGTAG_PROGRESS = "dialog_progress";

	public InitialProgressDialogStrategy(ActivityStrategy parent) {
		super(parent);
	}

	@Override
	public void onCustomEvent(CustomEventInterface customEvent) {
		if (customEvent instanceof ProgressDialog.ProgressDialogCanceledEvent) {
			// user dismissed "loading ..." dialog so we exit
			callbacks().finish();
		} else {
			super.onCustomEvent(customEvent);
		}
	}

	public void showProgressDialog() {
		Preconditions.checkArgument(getContext() instanceof ProgressDialogListener);
		FragmentUtils.showDialogFragment((FragmentActivity) getContext(), DIALOGTAG_PROGRESS,
				ProgressDialog.newInstance(getContext(), R.string.msg_loading_please_wait, true));
	}
	
	public void hideProgressDialog() {
		FragmentUtils.dismissDialogFragment((FragmentActivity) getContext(), DIALOGTAG_PROGRESS);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState, OnSaveInstanceStateSuperCall superCall) {
		FragmentUtils.dismissDialogFragment((FragmentActivity) getContext(), DIALOGTAG_PROGRESS);
		super.onSaveInstanceState(outState, superCall);
	}
}
