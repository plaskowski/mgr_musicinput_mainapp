package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.FakePause;
import android.graphics.Canvas;
import android.graphics.Paint;

public class FakePauseElement extends SheetAlignedElement {
	ElementSpec.FakePause spec;
	
	public FakePauseElement(FakePause spec) {
		this.spec = spec;
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
	}
	
	@Override
	public int measureWidth() {
		return 0;
	}
	
	@Override
	public int measureHeight() {
		return 0;
	}
	
	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return 0;
	}
	
	@Override
	public int getVerticalOffset(int lineIdentifier) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getHorizontalOffset(int lineIdentifier) {
		if(lineIdentifier == MIDDLE_X) {
			return 0;
		} else {
			return super.getHorizontalOffset(lineIdentifier);
		}
	}
	
	@Override
	public ElementSpec getElementSpec() {
		return spec;
	}
}