package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

public abstract class SheetAlignedElement extends SheetElement {
	/**
	 * offset of specific vertical line relative to left edge of this element
	 * @param lineIdentifier specify exact line
	 */
	public abstract int getHorizontalOffset(int lineIdentifier);
	/**
	 * offset of specific horizontal line relative to top edge of this element
	 * @param lineIdentifier specify exact line
	 */
	public abstract int getVerticalOffset(int lineIdentifier);
	
	public float getMetaValue(int valueIndentifier, int param) {
		throw new UnsupportedOperationException();
	}
	
	public int collisionRegionLeft() {
		return 0;
	}
	public int collisionRegionRight() {
		return measureWidth();
	}
	
	public abstract ElementSpec getElementSpec();
	
	private static int sequence = 0;
	protected synchronized static int registerIndex() {
		return sequence++;
	}
	public final int getMiddleX() {
		return getHorizontalOffset(MIDDLE_X);
	}
	public static int MIDDLE_X = registerIndex();
}
