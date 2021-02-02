package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ParcelablePrimitives.ParcelableString;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_EmptyConfirmDialogListener;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Parcelable;
import android.util.Log;

public class TipDialogStrategy extends FragmentActivity_EmptyConfirmDialogListener implements ConfirmDialogListener {
	protected static final int CONFIRMDIALOG_CALLBACKARG_TIP = 1;
	protected static final String DIALOGTAG_TIP = "tipdialog";
	private static final String REGISTRY_FILE = "tips_registry";
	
	/**
	 * Shows Dialog with given tip, allowing user to permanently dismiss 
	 * so next call of this method with same tipTag won't show anything.
	 * @param tipTag identifier of tip, should be unique in application context
	 * @return if dialog was shown
	 */
	protected boolean showTipDialog(String tipTag, int msgId, String[] msgParams) {
		SharedPreferences prefs = getSharedPreferences(REGISTRY_FILE, Context.MODE_PRIVATE);
		if(prefs.getBoolean(tipTag, false)) {
			// user already dismissed dialog permanently in the past
			return false;
		} else {
			new ConfirmDialog.ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_TIP)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setState(new ParcelableString(tipTag))
			.setTitle(R.string.tipdialog_title)
			.setMsg(msgId, msgParams)
			.setPositiveNeutral(android.R.string.ok, R.string.label_dismiss_tip)
			.showNew(getSupportFragmentManager(), DIALOGTAG_TIP);
			return true;
		}
	}
	
	@Override
	public void onDialogResult(ConfirmDialog dialog, int dialogId,
			DialogAction action, Parcelable state) {
		if(dialogId == CONFIRMDIALOG_CALLBACKARG_TIP) {
			if(action == DialogAction.BUTTON_NEUTRAL) {
				// save that user dismissed dialog permanently
				SharedPreferences prefs = getSharedPreferences(REGISTRY_FILE, Context.MODE_PRIVATE);
				Editor editor = prefs.edit();
				String tipTag = ((ParcelableString) state).value;
				editor.putBoolean(tipTag, true);
				editor.commit();
				Log.v(LogUtils.COMMON_TAG, "Permanently dismissed tip: "+tipTag);
			}
		} else {
			super.onDialogResult(dialog, dialogId, action, state);
		}
	}
	
}