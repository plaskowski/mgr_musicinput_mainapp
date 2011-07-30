package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import android.graphics.Point;
import android.util.Pair;

public abstract class SheetAlignedElement extends SheetElement {
	public abstract int getHeadMiddleX();
	public int collisionRegionLeft() {
		return 0;
	}
	public int collisionReginRight() {
		return measureWidth();
	}
	public NoteSpec getNoteSpec() {
		throw new UnsupportedOperationException();
	}
}
