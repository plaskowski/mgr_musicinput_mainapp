package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.security.InvalidParameterException;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Paint;

public class AddedLine extends AlignedElementWrapper {
	private int startLine, endLine;
	private int wrapperRadius;
	
	public AddedLine(SheetAlignedElement wrappedElement, int lineIndex) {
		super(wrappedElement);
		if(lineIndex < 0) {
			startLine = lineIndex;
			endLine = -1;
		} else if(lineIndex > 4) {
			startLine = 5;
			endLine = lineIndex;
		} else {
			throw new InvalidParameterException();
		}
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		
		// calculate total bounds
		int startAbsIndex = NoteConstants.anchorIndex(startLine, NoteConstants.ANCHOR_TYPE_LINE);
		int linesNum = endLine-startLine+1;
		wrapperRadius = sheetParams.getLineThickness()*2;
		int wrapperHeight = linesNum*sheetParams.getLineThickness()+(linesNum-1)*sheetParams.getLinespacingThickness();
		int elOffsetX = wrapperRadius - wrappedElement.collisionRegionLeft();
		int elOffsetY = wrappedElement.getOffsetToAnchor(startAbsIndex, AnchorPart.TOP_EDGE);
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
			elementDrawOffset.x+wrappedElement.collisionReginRight()+wrapperRadius
		);
		totalHeight = Math.max(
			elementDrawOffset.y+wrappedElement.measureHeight(),
			wrapperDrawOffset.y+wrapperHeight
		);
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		int y = wrapperDrawOffset.y;
		int xEnd = elementDrawOffset.x+wrappedElement.collisionReginRight()+wrapperRadius;
		for(int i = startLine; i <= endLine; i++) {
			canvas.drawRect(
				wrapperDrawOffset.x,
				y,
				xEnd,
				y+sheetParams.getLineThickness(),
				paint
			);
			y += sheetParams.getLineThickness()+sheetParams.getLinespacingThickness();
		}
		canvas.translate(elementDrawOffset.x, elementDrawOffset.y);
		// TODO remove debugging drawing
//		Paint paint2 = new Paint();
//		paint2.setColor(Color.RED);
//		canvas.drawRect(0, 0, wrappedElement.measureWidth(), wrappedElement.measureHeight(),
//			paint2
//		);
		wrappedElement.onDraw(canvas, paint);
	}

}
