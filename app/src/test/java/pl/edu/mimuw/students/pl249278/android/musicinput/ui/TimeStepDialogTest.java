package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class TimeStepDialogTest {
    @Test
    public void recreateDialog_restoresSelectedContainerAndSpinnerValues() throws Exception {
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class).setup().get();
        TimeStepDialog firstDialog = TimeStepDialog.newInstance(activity, null);
        firstDialog.show(activity.getSupportFragmentManager(), "time-step");
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(activity.getMainLooper()).idle();

        Dialog firstPlatformDialog = firstDialog.getDialog();
        firstPlatformDialog.findViewById(R.id.EDIT_dialog_timestep_custom).performClick();
        firstPlatformDialog.findViewById(R.id.EDIT_dialog_timestep_spinnertop)
                .findViewById(R.id.numberspinner_button_more)
                .performClick();
        firstPlatformDialog.findViewById(R.id.EDIT_dialog_timestep_spinnertop)
                .findViewById(R.id.numberspinner_button_more)
                .performClick();
        firstPlatformDialog.findViewById(R.id.EDIT_dialog_timestep_spinnerbottom)
                .findViewById(R.id.numberspinner_button_more)
                .performClick();
        firstPlatformDialog.findViewById(R.id.EDIT_dialog_timestep_spinnerbottom)
                .findViewById(R.id.numberspinner_button_more)
                .performClick();

        Bundle savedState = new Bundle();
        firstDialog.onSaveInstanceState(savedState);
        firstDialog.dismiss();
        activity.getSupportFragmentManager().executePendingTransactions();

        TimeStepDialog restoredDialog = TimeStepDialog.newInstance(activity, TimeStep.commonTime);
        restoredDialog.restoreInstanceStateForTest(savedState);
        restoredDialog.show(activity.getSupportFragmentManager(), "time-step-restored");
        activity.getSupportFragmentManager().executePendingTransactions();
        Shadows.shadowOf(activity.getMainLooper()).idle();

        Dialog restoredPlatformDialog = restoredDialog.getDialog();
        assertTrue(TimeStepDialog.isContainerSelectedForTest(
                activity,
                restoredPlatformDialog,
                R.id.EDIT_dialog_timestep_custom));
        assertEquals(3, TimeStepDialog.getSpinnerValueForTest(
                restoredPlatformDialog,
                R.id.EDIT_dialog_timestep_spinnertop));
        assertEquals(4, TimeStepDialog.getSpinnerValueForTest(
                restoredPlatformDialog,
                R.id.EDIT_dialog_timestep_spinnerbottom));
    }
}
