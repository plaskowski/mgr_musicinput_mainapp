package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;

public enum ElementModifier {
	DOT(null),
	FLAT(NoteModifier.FLAT),
	SHARP(NoteModifier.SHARP),
	NATURAL(NoteModifier.NATURAL);
	
	NoteModifier origin;

	private ElementModifier(NoteModifier origin) {
		this.origin = origin;
	}
	
	public static ElementModifier map(NoteModifier modifier) {
		ElementModifier[] values = ElementModifier_values;
		for(int i = 0; i < values.length; i++) {
			if(modifier.equals(values[i].origin))
				return values[i];
		}
		throw new RuntimeException();
	}
	
	private static ElementModifier[] ElementModifier_values = ElementModifier.values();
}
