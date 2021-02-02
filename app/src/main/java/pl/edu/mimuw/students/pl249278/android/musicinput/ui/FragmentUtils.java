package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class FragmentUtils {
	public static void showDialogFragment(FragmentActivity ctx, String dialogTag, DialogFragment newFragment) {
		dismissDialogFragment(ctx, dialogTag);
	    newFragment.show(ctx.getSupportFragmentManager(), dialogTag);
	}
	
	public static void dismissDialogFragment(FragmentActivity ctx, String dialogTag) {
		DialogFragment prev = (DialogFragment) ctx.getSupportFragmentManager().findFragmentByTag(dialogTag);
	    if (prev != null) {
	        prev.dismiss();
	    }
	}
}
