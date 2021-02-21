package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.CustomEventInterface;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner;

public class PlayPreferencesDialog extends DialogFragment {

    private static final String ARG_PLAY_CONF = "play_conf";

    public static void showNew(FragmentManager manager, PlayingConfiguration playConf) {
        PlayPreferencesDialog dialog = new PlayPreferencesDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLAY_CONF, new PlayingConfiguration(playConf));
        dialog.setArguments(args);
        dialog.show(manager, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        PlayingConfiguration playConf = getArguments().getParcelable(ARG_PLAY_CONF);
        View menuPanel = LayoutInflater.from(getContext()).inflate(R.layout.playscreen_menu, null);
        menuPanel.setTag(R.id.PLAY_tempoField, new TempoFieldController(menuPanel, playConf));
        CompoundButton introCheckbox = menuPanel.findViewById(R.id.PLAY_checkbox_intro);
        CompoundButton metronomeCheckbox = menuPanel.findViewById(R.id.PLAY_checkbox_metronome);
        introCheckbox.setChecked(playConf.isPrependEmptyBar());
        introCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> playConf.setPrependEmptyBar(isChecked));
        metronomeCheckbox.setChecked(playConf.isPlayMetronome());
        metronomeCheckbox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> playConf.setPlayMetronome(isChecked));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.configure)
                .setView(menuPanel)
                .setCancelable(true)
                .setNeutralButton(android.R.string.cancel, (dialog, id) -> {})
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    FragmentActivity a = getActivity();
                    if (a != null) {
                        ((PlayPreferencesDialogListener) a).onDialogResult(
                                new PlayPreferencesDialogClosedEvent(playConf));
                    }
                })
                .create();
    }

    public interface PlayPreferencesDialogListener {
        void onDialogResult(PlayPreferencesDialogClosedEvent event);
    }

    public static class PlayPreferencesDialogClosedEvent implements CustomEventInterface {
        private final PlayingConfiguration playingConfiguration;

        public PlayPreferencesDialogClosedEvent(PlayingConfiguration playingConfiguration) {
            this.playingConfiguration = playingConfiguration;
        }

        public PlayingConfiguration getPlayingConfiguration() {
            return playingConfiguration;
        }
    }

    private class TempoFieldController extends IntegerSpinner.IntegerSpinnerController implements TextWatcher {
        private EditText field;
        private int prevValue;
        private final PlayingConfiguration playingConfiguration;

        public TempoFieldController(View wrapper, PlayingConfiguration playingConfiguration) {
            super(
                    new IntegerSpinner.IncrementModel(10)
                            .setMinValue(getResources().getInteger(R.integer.minPlayTempoBPM))
                            .setValue(playingConfiguration.getTempo()),
                    wrapper,
                    R.id.button_plus_ten,
                    R.id.button_minus_ten
            );
            field = (EditText) wrapper.findViewById(R.id.PLAY_tempoField);
            this.playingConfiguration = playingConfiguration;
            field.addTextChangedListener(this);
            prevValue = playingConfiguration.getTempo();
            updateViews();
        }

        @Override
        protected void updateViews() {
            super.updateViews();
            if(field != null) {
                field.setText(Integer.toString(getValue()));
            }
            onValueChanged();
        }

        private void onValueChanged() {
            if(model.getValue() != prevValue) {
                prevValue = model.getValue();
                if (playingConfiguration != null) {
                    playingConfiguration.setTempo(model.getValue());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                int newValue = Integer.parseInt(s.toString());
                model.setValue(newValue);
                super.updateViews();
                onValueChanged();
            } catch(NumberFormatException e) {
            }
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    }

}
