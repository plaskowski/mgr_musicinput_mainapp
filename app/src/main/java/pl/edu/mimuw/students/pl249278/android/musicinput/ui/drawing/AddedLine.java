package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.security.InvalidParameterException;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Paint;

public class AddedLine extends AlignedElementWrapper<SheetAlignedElement> {
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
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		
		// calculate total bounds
		int startAbsIndex = NoteConstants.anchorIndex(startLine, NoteConstants.ANCHOR_TYPE_LINE);
		int linesNum = endLine-startLine+1;
		wrapperRadius = sheetParams.getLinespacingThickness()/2;
		int wrapperHeight = linesNum*sheetParams.getLineThickness()+(linesNum-1)*sheetParams.getLinespacingThickness();
		int left = wrappedElement.getHorizontalOffset(NoteHeadElement.NOTEHEAD_LEFT);
		int elOffsetX = wrapperRadius - left;
		int elOffsetY = wrappedElement.getOffsetToAnchor(startAbsIndex, AnchorPart.TOP_EDGE);
		calcDrawOffsets(
			elOffsetX,
			elOffsetY
		);
		int right = wrappedElement.getHorizontalOffset(NoteHeadElement.NOTEHEAD_RIGHT);
		calcSize(
			right - left + 2*wrapperRadius,
			wrapperHeight
		);
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		int y = wrapperDrawOffset.y;
		int xEnd = 
			elementDrawOffset.x
			+ wrappedElement.getHorizontalOffset(NoteHeadElement.NOTEHEAD_RIGHT)
			+ wrapperRadius;
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
		super.onDraw(canvas, paint);
	}

}
