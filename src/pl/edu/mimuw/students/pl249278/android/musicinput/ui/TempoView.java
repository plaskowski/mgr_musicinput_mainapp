package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

public class TempoView extends SheetElementView {
	
	private static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
	private static final int LINE2_ABSINDEX = NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);

	public TempoView(Context context) {
		super(context);
	}
	
	private String upper, lower;
	
	public void setLetters(char upper, char lower) {
		this.upper = ""+upper;
		this.lower = ""+lower;
		if(sheetParams != null) {
			calculate();
			invalidateMeasure();
			invalidate();
		}
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		if(upper != null && lower != null) {
			calculate();
			invalidateMeasure();
			invalidate();
		}
	}
	
	@Override
	public void setPaint(Paint paint) {
		paint.setTypeface(Typeface.SERIF);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(textSize);
		super.setPaint(paint);
	}

	private int upperY;
	private int lowerY;
	private float width;
	private float textSize;
	private void calculate() {
		int start = sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int horizontalSpace = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.TOP_EDGE)-start;
		
		// find such text size that upper char will fill vertically entire horizontalSpace
		paint.setTextSize(horizontalSpace);
		Rect bounds = new Rect();
		paint.getTextBounds(upper, 0, 1, bounds);
		textSize = ((float) horizontalSpace)/bounds.height() * horizontalSpace;
		paint.setTextSize(textSize);
		
		width = Math.max(paint.measureText(upper), paint.measureText(lower));
		upperY = start + horizontalSpace;
		paint.getTextBounds(lower, 0, 1, bounds);
		lowerY = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.BOTTOM_EDGE) + bounds.height();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvas.drawText(upper, getPaddingLeft()+width/2, getPaddingTop()+upperY, paint);
		canvas.drawText(lower, getPaddingLeft()+width/2, getPaddingTop()+lowerY, paint);
	}

	@Override
	public int measureHeight() {
		return 
		getPaddingTop() + 
		sheetParams.anchorOffset(LINE4_ABSINDEX, AnchorPart.TOP_EDGE) +
		getPaddingBottom(); 
	}

	@Override
	public int measureWidth() {
		return 
		getPaddingLeft() +
		((int) width) +
		getPaddingRight();
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return 
		sheetParams.anchorOffset(anchorAbsIndex, part)
		- (sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.TOP_EDGE)-getPaddingTop());
	}

}
