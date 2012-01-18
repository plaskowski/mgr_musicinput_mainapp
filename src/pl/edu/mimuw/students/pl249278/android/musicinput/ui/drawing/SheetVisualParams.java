package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

public interface SheetVisualParams {

	/**
	 * @param anchorAbsIndex absolute index of anchor
	 * @param part determines exact horizontal line inside anchor we measure distance to
	 * @return anchor[absIndex][part].y - [line0 (first line from 5lines)][top edge].y
	 */
	public int anchorOffset(int anchorAbsIndex, AnchorPart part);

	public int getLineThickness();

	public int getLinespacingThickness();
	
	public static enum AnchorPart {
		MIDDLE,
		BOTTOM_EDGE,
		TOP_EDGE
	}

}