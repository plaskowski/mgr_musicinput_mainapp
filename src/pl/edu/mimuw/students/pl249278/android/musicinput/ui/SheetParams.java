package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

public class SheetParams {

	private int lineThickness;
	private int linespacingThickness;
	
	public SheetParams(int lineThickness, int linespacingThickness) {
		super();
		this.lineThickness = lineThickness;
		this.linespacingThickness = linespacingThickness;
	}

	public static enum AnchorPart {
		MIDDLE,
		BOTTOM_EDGE
	}

	public int anchorOffset(int anchorAbsIndex, AnchorPart part) {
		//TODO inline
		int anchorSize = anchorAbsIndex%2 == 0 ? lineThickness : linespacingThickness;
		int linesBefore = (anchorAbsIndex+1)/2;
		int spacesBefore = anchorAbsIndex/2;
		int linesBeforeSize = linesBefore*lineThickness;
		int spacesBeforeSize = spacesBefore*linespacingThickness;
		int baseDiff = linesBeforeSize + spacesBeforeSize;
		int partDiff = part == AnchorPart.BOTTOM_EDGE ? anchorSize : anchorSize/2;
		return baseDiff + partDiff;
	}	
}
