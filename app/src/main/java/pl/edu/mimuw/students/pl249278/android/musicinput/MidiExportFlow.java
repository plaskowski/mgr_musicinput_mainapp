package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;

import androidx.fragment.app.FragmentActivity;

import java.io.File;

import javax.annotation.Nullable;

import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivityHelper.ExportMidiRequest;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.WorkerService;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ConfirmDialog.ConfirmDialogBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TextInputDialog;

import static com.google.common.base.MoreObjects.firstNonNull;

public class MidiExportFlow {
    private static final int INPUTDIALOG_CALLBACKARG_MIDIFILE = 101;
    private static final int PERMISSION_REQUEST_WRITE_MIDI = 101;
    private static final int CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE = 101;
    private static final String DIALOGTAG_EXPORT_MIDI = "dialog_export_midi";
    private static final String DIALOGTAG_CONFIRM_OVERWRITE = "dialog_overwrite_file";
    private static final String STATE_SCORE_ID = "score_id";
    private static final String STATE_SCORE_TITLE = "score_title";

    private final MidiExportFlowContext context;
    private final long scoreId;
    @Nullable
    private final String scoreTitle;

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
        return new MidiExportFlow(context,
                savedStateBundle.getLong(STATE_SCORE_ID),
                savedStateBundle.getString(STATE_SCORE_TITLE));
    }

    public Bundle saveState() {
        Bundle result = new Bundle();
        result.putLong(STATE_SCORE_ID, scoreId);
        result.putString(STATE_SCORE_TITLE, scoreTitle);
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
            exportToMidi(listenerArg, value);
        }
    }

    private void exportToMidi(long listenerArg, String value) {
        if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            context.showInfoDialog(R.string.errormsg_external_storage_not_present);
        } else {
            File dir = WorkerService.getExportDir();
            File destFile = new File(dir, value);
            if(destFile.exists()) {
                FragmentActivity dialogContext = context.getDialogContext();
                new ConfirmDialogBuilder(CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE)
                        .setState(new ExportMidiRequest(listenerArg, value))
                        .setMsg(R.string.popup_msg_file_already_exists, new String[] { value })
                        .setButtons(R.string.overwrite, R.string.change, android.R.string.cancel)
                        .showNew(dialogContext.getSupportFragmentManager(), DIALOGTAG_CONFIRM_OVERWRITE);
            } else {
                sendExportMidiRequest(new ExportMidiRequest(listenerArg, value));
            }
        }
    }

    public void onDialogResult(ConfirmDialog.ConfirmDialogClosedEvent event) {
        Parcelable state = event.getState();
        if (event.getDialogId() == CONFIRMDIALOG_CALLBACKARG_MIDIFILE_OVERWRITE) {
            switch (event.getAction()) {
                case BUTTON_POSITIVE:
                    // user chose to overwrite existing MIDI file
                    sendExportMidiRequest((ExportMidiRequest) state);
                    break;
                case BUTTON_NEUTRAL:
                    showExportMidiDialog();
                    break;
            }
        }
    }

    private void sendExportMidiRequest(ExportMidiRequest state) {
        context.onMidiExportFlowEnd(state);
    }

}
