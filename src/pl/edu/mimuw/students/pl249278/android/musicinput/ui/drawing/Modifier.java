package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Modifier extends AlignedElementWrapper {
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
		elementDrawOffset.set(
			elOffsetX >= 0 ? elOffsetX : 0,
			elOffsetY >= 0 ? elOffsetY : 0
		);
		wrapperDrawOffset.set(
			elementDrawOffset.x - elOffsetX, 
			elementDrawOffset.y - elOffsetY
		);
		totalWidth = Math.max(
			elementDrawOffset.x+wrappedElement.measureWidth(), 
			wrapperDrawOffset.x+modifierElement.measureWidth()
		);
		totalHeight = Math.max(
			elementDrawOffset.y+wrappedElement.measureHeight(),
			wrapperDrawOffset.y+modifierElement.measureHeight()
		);
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		// TODO remove debug
//		Paint paint2 = new Paint();
//		paint2.setColor(0xFF00FF00);
//		paint2.setAlpha(100);
//		canvas.drawRect(0,0, measureWidth(), measureHeight(), paint2);
		canvas.translate(elementDrawOffset.x, elementDrawOffset.y);
//		paint2.setColor(0xFFFF0000);
//		paint2.setAlpha(100);
//		canvas.drawRect(0,0, wrappedElement.measureWidth(), wrappedElement.measureHeight(), paint2);
		wrappedElement.onDraw(canvas, paint);
		canvas.translate(wrapperDrawOffset.x - elementDrawOffset.x, wrapperDrawOffset.y - elementDrawOffset.y);
		modifierElement.onDraw(canvas, paint);
	}

}
