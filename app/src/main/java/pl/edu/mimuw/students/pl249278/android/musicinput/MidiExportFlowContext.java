package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;

public interface MidiExportFlowContext {

    <T extends FragmentActivity
            & TextInputDialog.TextInputDialogListener
            & ConfirmDialog.ConfirmDialogListener>
    T getDialogContext();

    Activity getPermissionContext();

    void showInfoDialog(int messageResId);

    /**
     * @param state request to be send to service
     */
    void onMidiExportFlowEnd(MainActivityHelper.ExportMidiRequest state);
}
