package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.NoteValueSpinner;

public interface NoteValueWidget {

	int INITIAL_VALUE_UNDEFINED = NoteValueSpinner.INITIAL_VALUE_UNDEFINED;

	public void setOnValueChangedListener(
			OnValueChanged<Integer> onValueChangedListener);

	public int getCurrentValue();

	/** Setup views, set scale to 1 */
	public void setupNoteViews(SheetParams globalParams, int initialCurrentValue) throws CreationException;

	public static interface OnValueChanged<ValueType> {
		public void onValueChanged(ValueType newValue, ValueType oldValue);
	}
}
