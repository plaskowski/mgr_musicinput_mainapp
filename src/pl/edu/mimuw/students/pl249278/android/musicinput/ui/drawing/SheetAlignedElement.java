package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import android.util.SparseIntArray;

public abstract class SheetAlignedElement extends SheetElement {
	private static SparseIntArray optionalHorizontalLines = new SparseIntArray();
	
	/**
	 * @return lineIdentifier for code flow
	 */
	protected static int registerHorizontalLineAsOptional(int lineIdentifier) {
		optionalHorizontalLines.put(lineIdentifier, 1);
		return lineIdentifier;
	}
	
	public static final int HLINE_UNSPECIFIED = Integer.MAX_VALUE;
	
	/**
	 * offset of specific vertical line relative to left edge of this element
	 * @param lineIdentifier specify exact line
	 */
	public int getHorizontalOffset(int lineIdentifier) {
		if(optionalHorizontalLines.get(lineIdentifier, 0) == 1) {
			return HLINE_UNSPECIFIED;
		} else {
			throw new UnsupportedOperationException();
		}
	}
	/**
	 * offset of specific horizontal line relative to top edge of this element
	 * @param lineIdentifier specify exact line
	 */
	public int getVerticalOffset(int lineIdentifier) {
		throw new UnsupportedOperationException();
	}
	
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
	
	private Object tag = null;
	public void setTag(Object tag) {
		this.tag = tag;
	}
	public Object getTag() {
		return tag;
	}
	
}
