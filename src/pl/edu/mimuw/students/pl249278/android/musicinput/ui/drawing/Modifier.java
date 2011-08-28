package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Modifier extends AlignedElementWrapper<SheetAlignedElement> {
	protected SimpleSheetElement modifierElement;
	
	public Modifier(Context context, SheetAlignedElement wrappedElement, int position, NoteModifier modifier) throws LoadingSvgException {
		super(wrappedElement);
		AdjustableSizeImage modifierImg = NotePartFactory.prepareModifier(context, modifier, NoteConstants.isUpsdown(position), NoteConstants.anchorType(position)); 
		modifierElement = new SimpleSheetElement(modifierImg, position);
		this.wrappedElement = wrappedElement;
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		modifierElement.setSheetParams(params);
		
		int spacing = params.getLineThickness();
		int elOffsetX = elementOffsetX(spacing);
		int wrappOY = modifierElement.getOffsetToAnchor(0, AnchorPart.TOP_EDGE);
		int elOY = wrappedElement.getOffsetToAnchor(0, AnchorPart.TOP_EDGE);
		int elOffsetY =  elOY - wrappOY;
		calcDrawOffsets(elOffsetX, elOffsetY);
		calcSize(
			modifierElement.measureWidth(),	
			modifierElement.measureHeight()
		);
	}
	
	@Override
	public int collisionRegionLeft() {
		return Math.min(
			super.collisionRegionLeft(),
			wrapperDrawOffset.x
		);
	}
	
	@Override
	public int collisionRegionRight() {
		return Math.max(
			super.collisionRegionRight(),
			wrapperDrawOffset.x + modifierElement.measureWidth()
		);
	}
	
	protected abstract int elementOffsetX(int spacing);

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		super.onDraw(canvas, paint);
		canvas.translate(wrapperDrawOffset.x, wrapperDrawOffset.y);
		modifierElement.onDraw(canvas, paint);
		canvas.translate(-wrapperDrawOffset.x, -wrapperDrawOffset.y);
	}
	
	private static abstract class NoteModifierElement extends Modifier {
		
		public NoteModifierElement(Context context,
				SheetAlignedElement wrappedElement, int position,
				NoteModifier modifier) throws LoadingSvgException {
			super(context, wrappedElement, position, modifier);
		}

		@Override
		public int getVerticalOffset(int lineIdentifier) {
			if(lineIdentifier == NoteHeadElement.AREA_NOTEHEAD_TOP) {
				return Math.min(
					wrapperDrawOffset.y,
					super.getVerticalOffset(lineIdentifier)
				);
			} else if(lineIdentifier == NoteHeadElement.AREA_NOTEHEAD_BOTTOM) {
				return Math.max(
					wrapperDrawOffset.y + modifierElement.measureHeight(),
					super.getVerticalOffset(lineIdentifier)
				);
			} else {
				return super.getVerticalOffset(lineIdentifier);
			}
		}
		
		@Override
		public int getHorizontalOffset(int lineIdentifier) {
			if(lineIdentifier == NoteHeadElement.AREA_NOTEHEAD_LEFT) {
				return Math.min(
					wrapperDrawOffset.x,
					super.getHorizontalOffset(lineIdentifier)
				);
			} else if(lineIdentifier == NoteHeadElement.AREA_NOTEHEAD_RIGHT) {
				return Math.max(
					wrapperDrawOffset.x + modifierElement.measureWidth(),
					super.getHorizontalOffset(lineIdentifier)
				);
			} else {
				return super.getHorizontalOffset(lineIdentifier);
			}
		}

	}
	
	public static class Prefix extends NoteModifierElement {
		public Prefix(Context context, SheetAlignedElement wrappedElement,
				int position, NoteModifier modifier) throws LoadingSvgException {
			super(context, wrappedElement, position, modifier);
		}

		@Override
		protected int elementOffsetX(int spacing) {
			return modifierElement.measureWidth() + spacing - wrappedElement.getHorizontalOffset(NoteHeadElement.AREA_NOTEHEAD_LEFT);
		}
	}
	public static class Suffix extends NoteModifierElement {
		public Suffix(Context context, SheetAlignedElement wrappedElement,
				int position, NoteModifier modifier) throws LoadingSvgException {
			super(context, wrappedElement, position, modifier);
		}

		@Override
		protected int elementOffsetX(int spacing) {
			return -(spacing+wrappedElement.getHorizontalOffset(NoteHeadElement.AREA_NOTEHEAD_RIGHT));
		}
	}

}
