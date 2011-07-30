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

public class Modifier extends AlignedElementWrapper<SheetAlignedElement> {
	private SimpleSheetElement modifierElement;
	
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
		int elOffsetX = modifierElement.measureWidth() + spacing - wrappedElement.collisionRegionLeft();
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
	public void onDraw(Canvas canvas, Paint paint) {
		super.onDraw(canvas, paint);
		canvas.translate(wrapperDrawOffset.x, wrapperDrawOffset.y);
		modifierElement.onDraw(canvas, paint);
	}

}
