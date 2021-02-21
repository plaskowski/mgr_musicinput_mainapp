package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import androidx.fragment.app.FragmentActivity;

import java.io.File;

import javax.annotation.Nullable;

import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ExportMidiRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.google.common.base.MoreObjects.firstNonNull;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogListener.DialogAction.BUTTON_POSITIVE;

public class MidiExportFlow {
    private static final int INPUTDIALOG_CALLBACKARG_MIDIFILE = 101;
    private static final int PERMISSION_REQUEST_WRITE_MIDI = 101;
    private static final int CONFIRMDIALOG_CALLBACKARG_GRANT_PERMISSION = 100;
    private static final int CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE = 101;
    private static final String DIALOGTAG_EXPORT_MIDI = "dialog_export_midi";
    private static final String DIALOGTAG_CONFIRM_OVERWRITE = "dialog_overwrite_file";
    private static final String DIALOGTAG_CONFIRM_GRANT_PERMISSION = "dialog_grant_permission";
    private static final String STATE_SCORE_ID = "score_id";
    private static final String STATE_SCORE_TITLE = "score_title";
    private static final String STATE_EXPORT_REQUEST = "export_request";
    public static final String WRITE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private final MidiExportFlowContext context;
    private final long scoreId;
    @Nullable
    private final String scoreTitle;
    private ExportMidiRequest exportMidiRequest;

    public MidiExportFlow(MidiExportFlowContext context, Score score) {
        this(context, score.getId(), score.getTitle());
    }

    private MidiExportFlow(MidiExportFlowContext context, long scoreId, @Nullable String scoreTitle) {
        this.context = context;
        this.scoreId = scoreId;
        this.scoreTitle = scoreTitle;
    }

    static MidiExportFlow start(MidiExportFlowContext context, Score score) {
        MidiExportFlow result = new MidiExportFlow(context, score);
        result.showExportMidiDialog();
        return result;
    }

    public static MidiExportFlow restore(MidiExportFlowContext context, Bundle savedStateBundle) {
        MidiExportFlow instance = new MidiExportFlow(context,
                savedStateBundle.getLong(STATE_SCORE_ID),
                savedStateBundle.getString(STATE_SCORE_TITLE));
        instance.exportMidiRequest = savedStateBundle.getParcelable(STATE_EXPORT_REQUEST);
        return instance;
    }

    public Bundle saveState() {
        Bundle result = new Bundle();
        result.putLong(STATE_SCORE_ID, scoreId);
        result.putString(STATE_SCORE_TITLE, scoreTitle);
        result.putParcelable(STATE_EXPORT_REQUEST, exportMidiRequest);
        return result;
    }

    void showExportMidiDialog() {
        FragmentActivity dialogContext = context.getDialogContext();
        String scoreTitle = firstNonNull(this.scoreTitle, dialogContext.getString(android.R.string.untitled));
        String initValue = scoreTitle.replaceAll("[^A-Za-z \\(\\)0-9]", "_") + ".midi";
        TextInputDialog.newInstance(dialogContext,
                INPUTDIALOG_CALLBACKARG_MIDIFILE, scoreId,
                R.string.popup_title_export_as_midi, dialogContext.getString(R.string.popup_msg_exportmidi),
                android.R.string.ok, android.R.string.cancel, initValue
        ).show(dialogContext.getSupportFragmentManager(), DIALOGTAG_EXPORT_MIDI);
    }

    public void onValueEntered(int valueId, long listenerArg, String value) {
        if (valueId == INPUTDIALOG_CALLBACKARG_MIDIFILE) {
            this.exportMidiRequest = new ExportMidiRequest(scoreId, value);
            checkPermissionAndExport();
        }
    }

    private void checkPermissionAndExport() {
        Activity permissionContext = context.getPermissionContext();
        if (permissionContext.checkSelfPermission(WRITE_PERMISSION) == PERMISSION_GRANTED) {
            exportToMidi();
            return;
        }
        if (permissionContext.shouldShowRequestPermissionRationale(WRITE_PERMISSION)) {
            showPermissionRationaleDialog();
        } else {
            requestWritePermission();
        }
    }

    private void showPermissionRationaleDialog() {
        FragmentActivity dialogContext = context.getDialogContext();
        new ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_GRANT_PERMISSION)
                .setMsg(R.string.dialog_midi_export_explain_permission, new String[] {})
                .setPositiveNegative(R.string.yes, R.string.no)
                .showNew(dialogContext.getSupportFragmentManager(), DIALOGTAG_CONFIRM_GRANT_PERMISSION);
    }

    private void requestWritePermission() {
        context.getPermissionContext().requestPermissions(new String[]{WRITE_PERMISSION}, PERMISSION_REQUEST_WRITE_MIDI);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_MIDI && grantResults[0] == PERMISSION_GRANTED) {
            exportToMidi();
        }
    }

    private void exportToMidi() {
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            context.showInfoDialog(R.string.errormsg_external_storage_not_present);
        } else {
            File dir = WorkerService.getExportDir();
            File destFile = new File(dir, exportMidiRequest.filename);
            if(destFile.exists()) {
                showConfirmOverwriteDialog();
            } else {
                sendExportMidiRequest();
            }
        }
    }

    private void showConfirmOverwriteDialog() {
        FragmentActivity dialogContext = context.getDialogContext();
        new ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE)
                .setMsg(R.string.popup_msg_file_already_exists, new String[] {exportMidiRequest.filename})
                .setButtons(R.string.overwrite, R.string.change, android.R.string.cancel)
                .showNew(dialogContext.getSupportFragmentManager(), DIALOGTAG_CONFIRM_OVERWRITE);
    }

    public void onDialogResult(ConfirmDialog.ConfirmDialogClosedEvent event) {
        if (event.getDialogId() == CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE) {
            switch (event.getAction()) {
                case BUTTON_POSITIVE:
                    // user chose to overwrite existing MIDI file
                    sendExportMidiRequest();
                    break;
                case BUTTON_NEUTRAL:
                    showExportMidiDialog();
                    break;
            }
        } else if (event.getDialogId() == CONFIRMDIALOG_CALLBACKARG_GRANT_PERMISSION) {
            if (event.getAction() == BUTTON_POSITIVE) {
                requestWritePermission();
            }
        }
    }

    private void sendExportMidiRequest() {
        context.onMidiExportFlowEnd(exportMidiRequest);
    }
}
