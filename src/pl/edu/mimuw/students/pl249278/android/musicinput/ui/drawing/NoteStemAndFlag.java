package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteEnding;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class NoteStemAndFlag extends AlignedElementWrapper<NoteHeadElement> {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils("Note");

	private NoteEnding ending;
	private int endingIMAnchor;
	private float scaleE;

	public NoteStemAndFlag(Context context, NoteHeadElement wrappedElement) throws NoteDescriptionLoadingException {
		super(wrappedElement);
		parseNoteSpec(context);
	}
	
	private void parseNoteSpec(Context context) throws NoteDescriptionLoadingException {
		NoteSpec noteSpec = ((ElementSpec.NormalNote) getElementSpec()).spec;
		
		boolean upsdown = noteSpec.getOrientation() == NoteSpec.ORIENT_DOWN;
		int endingAnchor = NoteConstants.stemEnd(noteSpec);
		
		// discover appropriate parts images
		this.ending = NotePartFactory.getEndingImage(context, noteSpec.length(), NoteConstants.anchorType(endingAnchor), upsdown);
		endingIMAnchor = imarkerAnchor(ending.getIMarker(), endingAnchor);
	}
	
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
	}

	private void sheetParamsCalculations() {
		scaleE = wrappedElement.joinLineExactWidth() / lineXSpan(ending.getJoinLine());
		int endJLx = (int) (ending.getJoinLine().first.x * scaleE); 
		
		int headRelativeXOffset = endJLx - wrappedElement.joinLineX(); 
		int headYOffset = wrappedElement.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		int endingYOffset = sheetParams.anchorOffset(endingIMAnchor, part(ending.getIMarker()))
			- (int) (ending.getIMarker().getLine().first.y * scaleE);
		int headRelativeYOffset = headYOffset - endingYOffset;
		
		calcDrawOffsets(
			headRelativeXOffset,
			headRelativeYOffset
		);
		calcSize(
			(int) (ending.getWidth()*scaleE),
			(int) (ending.getHeight()*scaleE)
		);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		PointF headJLStart = new PointF(elementDrawOffset.x, elementDrawOffset.y);
		headJLStart.offset(wrappedElement.joinLineX(), wrappedElement.joinLineY());
		PointF endingJLEnd = new PointF(wrapperDrawOffset.x, wrapperDrawOffset.y);
		endingJLEnd.offset(ending.getJoinLine().second.x*scaleE, ending.getJoinLine().second.y * scaleE);
		
		// draw in appropriate order (so shadow effect would compound correctly)
		if(elementDrawOffset.y > wrapperDrawOffset.y) {
			canvas.translate(wrapperDrawOffset.x, wrapperDrawOffset.y);
			SvgRenderer.drawSvgImage(canvas, ending, scaleE, paint);
			canvas.translate(-wrapperDrawOffset.x, -wrapperDrawOffset.y);
			
			canvas.drawRect(
				headJLStart.x,
				Math.min(headJLStart.y, endingJLEnd.y)-1,
				endingJLEnd.x,
				Math.max(headJLStart.y, endingJLEnd.y)+1,
				paint
			);
	
			super.onDraw(canvas, paint);
		} else {
			super.onDraw(canvas, paint);
			
			canvas.drawRect(
				headJLStart.x,
				Math.min(headJLStart.y, endingJLEnd.y)-1,
				endingJLEnd.x,
				Math.max(headJLStart.y, endingJLEnd.y)+1,
				paint
			);
	
			canvas.translate(wrapperDrawOffset.x, wrapperDrawOffset.y);
			SvgRenderer.drawSvgImage(canvas, ending, scaleE, paint);
			canvas.translate(-wrapperDrawOffset.x, -wrapperDrawOffset.y);
		}
	}

}
