package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.NoteDescriptionLoadingException;
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

	private float Xcorrection;

	public NoteStemAndFlag(Context context, NoteHeadElement wrappedElement) throws NoteDescriptionLoadingException {
		super(wrappedElement);
		parseNoteSpec(context);
	}
	
	private void parseNoteSpec(Context context) throws NoteDescriptionLoadingException {
		NormalNote noteSpec = (ElementSpec.NormalNote) getElementSpec();
		
		boolean upsdown = noteSpec.getOrientation() == NoteConstants.ORIENT_DOWN;
		int endingAnchor = NoteConstants.stemEnd(noteSpec.noteSpec(), noteSpec.getOrientation());
		
		// discover appropriate parts images
		this.ending = NotePartFactory.getEndingImage(context, noteSpec.lengthSpec().length(), NoteConstants.anchorType(endingAnchor), upsdown);
		endingIMAnchor = imarkerAnchor(ending.getIMarker(), endingAnchor);
	}
	
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
	}

	private void sheetParamsCalculations() {
		scaleE = wrappedElement.joinLineExactWidth() / lineXSpan(ending.getJoinLine());
		
		float endJLpreciseX = ending.getJoinLine().first.x * scaleE;
		int endJLx = (int) endJLpreciseX;
		float headJLpreciseX = wrappedElement.joinLineLeft();
		int headJLX = (int) headJLpreciseX;
		
		Xcorrection = (endJLx - endJLpreciseX) - (headJLX - headJLpreciseX);

		int headRelativeXOffset = endJLx - headJLX; 
		int headYOffset = wrappedElement.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		int endingYOffset = sheetParams.anchorOffset(endingIMAnchor, part(ending.getIMarker()))
			- (int) (ending.getIMarker().getLine().first.y * scaleE);
		int headRelativeYOffset = headYOffset - endingYOffset;
		
		calcDrawOffsets(
			headRelativeXOffset,
			headRelativeYOffset
		);
		calcSize(
			(int) Math.ceil(ending.getWidth()*scaleE),
			(int) Math.ceil(ending.getHeight()*scaleE)
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
			wrapperDrawOffset.x + (int) Math.ceil(ending.getWidth()*scaleE)
		);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		PointF headJLStart = new PointF(elementDrawOffset.x, elementDrawOffset.y);
		headJLStart.offset(
			wrappedElement.joinLineLeft(), 
			wrappedElement.joinLineY()
		);
		PointF endingJLEnd = new PointF(wrapperDrawOffset.x, wrapperDrawOffset.y);
		endingJLEnd.offset(ending.getJoinLine().second.x*scaleE, ending.getJoinLine().second.y * scaleE);
		
		// draw in appropriate order (so shadow effect would compound correctly)
		if(elementDrawOffset.y > wrapperDrawOffset.y) {
			drawEndingImage(canvas, paint);
			drawStem(canvas, paint, headJLStart, endingJLEnd);
			super.onDraw(canvas, paint);
		} else {
			super.onDraw(canvas, paint);
			drawStem(canvas, paint, headJLStart, endingJLEnd);
			drawEndingImage(canvas, paint);
		}
	}

	private void drawStem(Canvas canvas, Paint paint, PointF headJLStart,
			PointF endingJLEnd) {
		canvas.drawRect(
			headJLStart.x,
			Math.min(headJLStart.y, endingJLEnd.y)-1,
			endingJLEnd.x + Xcorrection,
			Math.max(headJLStart.y, endingJLEnd.y)+1,
			paint
		);
	}

	private void drawEndingImage(Canvas canvas, Paint paint) {
		canvas.translate(wrapperDrawOffset.x + Xcorrection, wrapperDrawOffset.y);
		SvgRenderer.drawSvgImage(canvas, ending, scaleE, paint);
		canvas.translate(-(wrapperDrawOffset.x + Xcorrection), -wrapperDrawOffset.y);
	}

}
