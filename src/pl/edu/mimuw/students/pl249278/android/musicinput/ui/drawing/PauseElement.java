package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.Pause;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class PauseElement extends SheetAlignedElement {
	private Pause spec;
	private SimpleSheetElement image;
	
	public PauseElement(Context ctx, ElementSpec.Pause spec) throws LoadingSvgException {
		this.spec = spec;
		this.image = new SimpleSheetElement(NotePartFactory.preparePauseImage(ctx, spec.lengthSpec().length()));
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		image.setSheetParams(params);
		super.setSheetParams(params);
	}

	@Override
	public int getHorizontalOffset(int lineIdentifier) {
		if(lineIdentifier == MIDDLE_X) {
			return measureWidth()/2;
		} else {
			return super.getHorizontalOffset(lineIdentifier);
		}
	}

	@Override
	public int getVerticalOffset(int lineIdentifier) {
		return super.getVerticalOffset(lineIdentifier);
	}

	@Override
	public ElementSpec getElementSpec() {
		return spec;
	}

	@Override
	public int measureWidth() {
		return image.measureWidth();
	}

	@Override
	public int measureHeight() {
		return image.measureHeight();
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return image.getOffsetToAnchor(anchorAbsIndex, part);
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		image.onDraw(canvas, paint);
	}
	
	public static class PauseDot extends Modifier {
		public PauseDot(Context context, SheetAlignedElement wrappedElement) throws LoadingSvgException {
			super(context, wrappedElement, NoteConstants.LINE2_ABSINDEX, ElementModifier.DOT);
		}

		@Override
		protected int elementOffsetX(int spacing) {
			return -(spacing+wrappedElement.collisionRegionRight());
		}
	}

}
