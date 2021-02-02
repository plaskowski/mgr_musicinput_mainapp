package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;

public interface NoteValueWidget {

	public void setOnValueChangedListener(
			OnValueChanged<Integer> onValueChangedListener);

	public int getCurrentValue();
	
	public void setupNoteViews(SheetParams globalParams, int initialCurrentValue) throws CreationException;
	
	/** Setup views, set scale to 1 */
	public void setupNoteViews(SheetParams globalParams) throws CreationException;
	
	
	public static interface OnValueChanged<ValueType> {
		public void onValueChanged(ValueType newValue, ValueType oldValue);
	}
}
