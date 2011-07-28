package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.graphics.Point;

public abstract class AlignedElementWrapper extends SheetAlignedElement {
	protected SheetAlignedElement wrappedElement;
	protected Point elementDrawOffset = new Point();
	protected Point wrapperDrawOffset = new Point();
	protected int totalWidth, totalHeight;
	
	public AlignedElementWrapper(SheetAlignedElement wrappedElement) {
		this.wrappedElement = wrappedElement;
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		wrappedElement.setSheetParams(params);
	}

	@Override
	public int getBaseMiddleX() {
		return elementDrawOffset.x + wrappedElement.getBaseMiddleX();
	}

	@Override
	public int measureHeight() {
		return totalHeight;
	}

	@Override
	public int measureWidth() {
		return totalWidth;
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return wrappedElement.getOffsetToAnchor(anchorAbsIndex, part)
		- elementDrawOffset.y;
	}

}
