package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

public abstract class SheetAlignedElement extends SheetElement {
	public abstract int getMiddleX();
	public int collisionRegionLeft() {
		return 0;
	}
	public int collisionRegionRight() {
		return measureWidth();
	}
	public abstract ElementSpec getElementSpec();
}
