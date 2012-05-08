package pl.edu.mimuw.students.pl249278.android.musicinput;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig.DisplayMode;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.IncrementModel;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.IntegerSpinnerController;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.IntegerSpinner.IntegerSpinnerModel;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class VisualPreferencesActivity extends Activity {
	public static final String START_EXTRAS_VISCONF = "visconf";
	public static final String RESULT_EXTRAS_VISCONF = "visconf";
	private IntegerSpinnerController lower;
	private IntegerSpinnerController upper;
	private ScoreVisualizationConfig model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null) {
			model = savedInstanceState.getParcelable(START_EXTRAS_VISCONF);
		} else {
			model = getIntent().getParcelableExtra(START_EXTRAS_VISCONF);
		}
		if(model == null) {
			throw new RuntimeException("No required VisualConfig object provided.");
		}
		setContentView(R.layout.vispreferences);		
		Spinner spinner = (Spinner) findViewById(R.id.displaymode_spinner);
		spinner.setAdapter(new EnumAdapter<DisplayMode>(this, android.R.layout.simple_spinner_item, android.R.layout.simple_spinner_dropdown_item, 
			modeLabelsMapping));
		spinner.setSelection(model.getDisplayMode().ordinal());
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long enumOridinal) {
				model.setDisplayMode(DisplayMode.values()[(int) enumOridinal]);
				updateActivityResult();
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}			
		});
		upper = new IncrDecrController(
			new IncrementModel(1).setValue(-model.getMinSpaceAnchor() - 1).setMinValue(0), 
			findViewById(R.id.VISPREFS_spinner_upper));
		int maxlineNo = model.getMaxSpaceAnchor() - 4;
		lower = new IncrDecrController(
			new IncrementModel(1).setValue(maxlineNo).setMinValue(0), 
			findViewById(R.id.VISPREFS_spinner_lower));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(START_EXTRAS_VISCONF, model);
		super.onSaveInstanceState(outState);
	}
	
	private void updateActivityResult() {
		if(upper != null && lower != null) {
			model.setMinSpaceAnchor(- upper.getValue() - 1);
			model.setMaxSpaceAnchor(lower.getValue() + 4);
		}
		// displayMode is automatically updated in model
		Intent data = new Intent();
		data.putExtra(RESULT_EXTRAS_VISCONF, model);
		setResult(RESULT_OK, data);
	}
	
	private class IncrDecrController extends IntegerSpinnerController {
		private TextView textView;

		public IncrDecrController(IntegerSpinnerModel model, View wrapper) {
			super(model, wrapper, R.id.button_more, R.id.button_less);
			textView = (TextView) wrapper.findViewById(R.id.label);
			updateViews();
		}
		
		@Override
		protected void updateViews() {
			super.updateViews();
			if(textView != null && model != null) {
				textView.setText(Integer.toString(getValue()));
			}
			updateActivityResult();
		}
	}
	
	private static EnumMap<DisplayMode, Integer> modeLabelsMapping = new EnumMap<DisplayMode, Integer>(DisplayMode.class);
	static {
		modeLabelsMapping.put(DisplayMode.NORMAL, R.string.spinner_label_normal_voice);
		modeLabelsMapping.put(DisplayMode.UPPER_VOICE, R.string.spinner_label_upper_voice);
		modeLabelsMapping.put(DisplayMode.LOWER_VOICE, R.string.spinner_label_lower_voice);
	}
	
	private static class EnumAdapter<T extends Enum<T>> extends ArrayAdapter<IndexedString<T>> {

		public EnumAdapter(Context context, int textViewResourceId, int dropdownResource,
				EnumMap<T, Integer> mapping) {
			super(context, textViewResourceId, prepare(context, mapping));
			setDropDownViewResource(dropdownResource);
		}
		
		@Override
		public long getItemId(int position) {
			return getItem(position).first.ordinal();
		}
		
		private static <T extends Enum<T>> List<IndexedString<T>> prepare(Context context, EnumMap<T, Integer> mapping) {
			List<IndexedString<T>> result = new ArrayList<IndexedString<T>>(mapping.size());
			for (T index : mapping.keySet()) {
				Integer resId = mapping.get(index);
				if(resId != null) {
					result.add(new IndexedString<T>(index, context.getString(resId)));
				}
			}
			return result;
		}

	}
	
	private static class IndexedString<T> extends Pair<T, String> {
		public IndexedString(T first, String second) {
			super(first, second);
		}
		
		@Override
		public String toString() {
			return second.toString();
		}
	}
}
