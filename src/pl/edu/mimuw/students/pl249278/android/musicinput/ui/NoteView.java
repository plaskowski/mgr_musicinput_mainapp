package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;

public class NoteView extends SheetElementView {

	private NoteBase base;
	private NoteEnding ending;
	
	private int baseIM1Anchor;
	private int baseIM2Anchor;
	private int endingIMAnchor;
	/** in base units */
	private float baseXoffset;
	/** in base units */
	private float endingXoffset;
	/** in base units */
	private float composedWidth;
	private float ratioE2B;
	private float scaleB;
	private float scaleE;
	private PointF baseDrawOffset;
	private PointF endingDrawOffset;
	private AnchorPart baseIM1AnchorPart;

	public NoteView(Context ctx) {
		super(ctx);
	}

	public NoteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NoteView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoteView(Context context, int noteLength, int noteHeight) throws NoteDescriptionLoadingException {
		super(context);
		setNoteSpec(context, noteLength, noteHeight);
	}
	
	public void setNoteSpec(Context context, int noteLength, int noteHeight)
			throws NoteDescriptionLoadingException {
		// FIXME real logic for discovering if it's upsidedown or normal
		boolean upsdown = noteHeight <= NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
		// FIXME real logic for discovering anchors
		int baseAnchor = noteHeight;
		int endingAnchor = baseAnchor + (upsdown ? 7 : -7);
		
		// discover appropriate parts images
		this.base = NotePartFactory.getBaseImage(context, noteLength, NoteConstants.anchorType(baseAnchor), upsdown);
    	IMarker firstM = base.getImarkers().get(0), secondM = base.getImarkers().get(1);
		baseIM1Anchor = imarkerAnchor(firstM, baseAnchor);
    	baseIM1AnchorPart = part(firstM);
		baseIM2Anchor = imarkerAnchor(secondM, baseAnchor);

		this.ending = NotePartFactory.getEndingImage(context, noteLength, NoteConstants.anchorType(endingAnchor), upsdown);
		if(ending != null) {
			endingIMAnchor = imarkerAnchor(ending.imarkers.get(0), endingAnchor);
	
			ratioE2B = lineXSpan(base.getJoinLine()) / lineXSpan(ending.getJoinLine());
	    	float diff = base.getJoinLine().first.x - ending.getJoinLine().first.x * ratioE2B;
			if(diff >= 0) {
				this.baseXoffset = 0;
				this.endingXoffset = diff;
			} else {
				this.baseXoffset = -diff;
				this.endingXoffset = 0;
			}
			this.composedWidth = Math.max(baseXoffset+base.getWidth(), endingXoffset+ending.getWidth()*ratioE2B);
		} else {
			this.baseXoffset = 0;
			this.composedWidth = base.getWidth();
		}
		
		if(sheetParams != null) {
			sheetParamsCalculations();
			onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			invalidate();
		}
	}
	
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
		invalidateMeasure();
	}

	private void sheetParamsCalculations() {
		IMarker firstM = base.getImarkers().get(0), secondM = base.getImarkers().get(1);
		int baseIM1RelativeOffset = sheetParams.anchorOffset(baseIM1Anchor, baseIM1AnchorPart);
    	int baseIM2RelativeOffset = sheetParams.anchorOffset(baseIM2Anchor, part(secondM));
    	scaleB = (baseIM1RelativeOffset-baseIM2RelativeOffset)/(firstM.getLine().first.y - secondM.getLine().first.y);
    	baseDrawOffset = new PointF(
			baseXoffset * scaleB, 0
		);
    		
    	if(ending != null) {
	    	scaleE = scaleB * ratioE2B;
	    	IMarker endingIM = ending.getImarkers().get(0);
	    	int endingIMRelativeOffset = sheetParams.anchorOffset(endingIMAnchor, part(endingIM));
			endingDrawOffset = new PointF(
				endingXoffset * scaleB, 0
			);
	    	
	    	int baseTopOffset = (int) (baseIM1RelativeOffset - firstM.line.first.y * scaleB);
	    	int endingTopOffset = (int) (endingIMRelativeOffset - endingIM.line.first.y * scaleE);
	    	if(baseTopOffset > endingTopOffset) {
	    		baseDrawOffset.y = baseTopOffset - endingTopOffset;
	    	} else {
	    		endingDrawOffset.y = endingTopOffset - baseTopOffset;
	    	}
	    	endingDrawOffset.offset(getPaddingLeft(), getPaddingTop());
    	}
    	baseDrawOffset.offset(getPaddingLeft(), getPaddingTop());
	}
	
	@Override
	public int measureHeight() {
		return (int) (Math.max(
			baseDrawOffset.y + base.getHeight()*scaleB,
			ending == null ? 0 : endingDrawOffset.y + ending.getHeight()*scaleE
		)) + getPaddingBottom();
	}

	@Override
	public int measureWidth() {
		if(sheetParams == null) {
			throw new IllegalStateException();
		}
		return (int) (composedWidth*scaleB) + getPaddingLeft()+getPaddingRight();
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		int xdelta = left-getPaddingLeft();
		int ydelta = top-getPaddingTop();
		super.setPadding(left, top, right, bottom);
		if((xdelta != 0 || ydelta != 0) && sheetParams != null) {
			baseDrawOffset.offset(xdelta, ydelta);
			if(endingDrawOffset != null)
				endingDrawOffset.offset(xdelta, ydelta);
			invalidateMeasure();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(ending == null) {
			drawSvgImage(canvas, base, scaleB, baseDrawOffset, paint);
		} else {
			PointF baseJLStart = new PointF(baseDrawOffset.x, baseDrawOffset.y);
			baseJLStart.offset(base.getJoinLine().first.x*scaleB, base.getJoinLine().first.y * scaleB);
			PointF endingJLEnd = new PointF(endingDrawOffset.x, endingDrawOffset.y);
			
			// draw in appropriate order (so shadow effect would compound correctly)
			if(baseDrawOffset.y > endingDrawOffset.y) {
				drawSvgImage(canvas, ending, scaleE, endingDrawOffset, paint);
				
				endingJLEnd.offset(ending.getJoinLine().second.x*scaleE, ending.getJoinLine().second.y * scaleE);
				canvas.drawRect(
					baseJLStart.x,
					Math.min(baseJLStart.y, endingJLEnd.y)-1,
					endingJLEnd.x,
					Math.max(baseJLStart.y, endingJLEnd.y)+1,
					paint
				);
		
				drawSvgImage(canvas, base, scaleB, baseDrawOffset, paint);
			} else {
				drawSvgImage(canvas, base, scaleB, baseDrawOffset, paint);
				
				endingJLEnd.offset(ending.getJoinLine().second.x*scaleE, ending.getJoinLine().second.y * scaleE);
				canvas.drawRect(
					baseJLStart.x,
					Math.min(baseJLStart.y, endingJLEnd.y)-1,
					endingJLEnd.x,
					Math.max(baseJLStart.y, endingJLEnd.y)+1,
					paint
				);
		
				drawSvgImage(canvas, ending, scaleE, endingDrawOffset, paint);
			}
		}
	}
	
	public int getBaseMiddleX() {
		return (int) (baseDrawOffset.x + base.xMiddleMarker * scaleB);
	}
	
	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return
			sheetParams.anchorOffset(baseIM1Anchor, baseIM1AnchorPart)
			- sheetParams.anchorOffset(anchorAbsIndex, part)
			- ((int) (baseDrawOffset.y + (base.getImarkers().get(0).line.first.y*scaleB)))
		;
	}
}
	
