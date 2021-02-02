package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.util.ArrayList;

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
import android.graphics.Rect;
import android.util.FloatMath;

public class NoteStemAndFlag extends AlignedElementWrapper<NoteHeadElement> {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils("Note");

	public static int HLINE_STEM_MIDDLE = registerHorizontalLineAsOptional(registerIndex());
	
	private NoteEnding ending;
	private int endingIMAnchor;
	private float scaleE;

	private float Xcorrection;

	private boolean noFlag;

	public NoteStemAndFlag(Context context, NoteHeadElement wrappedElement, boolean noFlag) throws NoteDescriptionLoadingException {
		super(wrappedElement);
		this.noFlag = noFlag;
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
		int endingYOffset = sheetParams.anchorOffset(endingIMAnchor, part(ending.getIMarker()));
		if(!noFlag) {
			endingYOffset -= (int) (ending.getIMarker().getLine().first.y * scaleE);
		}
		int headRelativeYOffset = headYOffset - endingYOffset;
		if(noFlag) {
			headRelativeYOffset -= Math.signum(headRelativeYOffset) * sheetParams.getLineThickness()*3;
		}
		
		calcDrawOffsets(
			headRelativeXOffset,
			headRelativeYOffset
		);
		if(noFlag) {
			calcSize(0, 0);
		} else {
			calcSize(
				(int) Math.ceil(ending.getWidth()*scaleE),
				(int) Math.ceil(ending.getHeight()*scaleE)
			);
		}
		
		mHeadJLStart.set(elementDrawOffset.x, elementDrawOffset.y);
		mHeadJLStart.offset(
			wrappedElement.joinLineLeft(), 
			wrappedElement.joinLineY()
		);
		mEndingJLEnd.set(wrapperDrawOffset.x, wrapperDrawOffset.y);
		mEndingJLEnd.offset(
			ending.getJoinLine().second.x*scaleE, 
			noFlag ? 0 : ending.getJoinLine().second.y * scaleE
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
	
	private PointF mHeadJLStart = new PointF(), mEndingJLEnd = new PointF();
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		// draw in appropriate order (so shadow effect would compound correctly)
		if(elementDrawOffset.y > wrapperDrawOffset.y) {
			drawEndingImage(canvas, paint);
			drawStem(canvas, paint);
			super.onDraw(canvas, paint);
		} else {
			super.onDraw(canvas, paint);
			drawStem(canvas, paint);
			drawEndingImage(canvas, paint);
		}
	}
	
	@Override
	public void getCollisionRegions(ArrayList<Rect> areas,
			ArrayList<Rect> rectsPool) {
		super.getCollisionRegions(areas, rectsPool);
		if(!noFlag) {
			Rect endingRect = obtain(rectsPool);
			endingRect.set(0, 0, (int) FloatMath.ceil(ending.getWidth()*scaleE), (int) FloatMath.ceil(ending.getHeight()*scaleE));
			endingRect.offset(wrapperDrawOffset.x, wrapperDrawOffset.y);
			areas.add(endingRect);
		}
		Rect stemRect = obtain(rectsPool);
		stemRect.set(
			(int) mHeadJLStart.x,
			(int) Math.min(mHeadJLStart.y, mEndingJLEnd.y)-1,
			ceil(mEndingJLEnd.x + Xcorrection),
			ceil(Math.max(mHeadJLStart.y, mEndingJLEnd.y)+1)
		);
		areas.add(stemRect);
	}
	
	@Override
	public int getHorizontalOffset(int lineIdentifier) {
		if(lineIdentifier == HLINE_STEM_MIDDLE) {
			return (((int) mHeadJLStart.x) + ceil(mEndingJLEnd.x + Xcorrection))/2;			
		} else {
			return super.getHorizontalOffset(lineIdentifier);
		}
	}
	
	private static int ceil(float value) {
		return (int) FloatMath.ceil(value);
	}

	private void drawStem(Canvas canvas, Paint paint) {
		canvas.drawRect(
			mHeadJLStart.x,
			Math.min(mHeadJLStart.y, mEndingJLEnd.y)-1,
			mEndingJLEnd.x + Xcorrection,
			Math.max(mHeadJLStart.y, mEndingJLEnd.y)+1,
			paint
		);
	}

	private void drawEndingImage(Canvas canvas, Paint paint) {
		if(noFlag)
			return;
		canvas.translate(wrapperDrawOffset.x + Xcorrection, wrapperDrawOffset.y);
		SvgRenderer.drawSvgImage(canvas, ending, scaleE, paint);
		canvas.translate(-(wrapperDrawOffset.x + Xcorrection), -wrapperDrawOffset.y);
	}

}
