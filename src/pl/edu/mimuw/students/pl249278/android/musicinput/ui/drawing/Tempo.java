package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

public class Tempo extends SheetElement {
	
	private static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
	private static final int LINE2_ABSINDEX = NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	
	private String upper, lower;
	private Paint fontPaint;
	
	public Tempo(int upper, int lower) {
		this.upper = ""+upper;
		this.lower = ""+lower;
		setPaint(new Paint());
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		if(upper != null && lower != null) {
			calculate();
		}
	}
	
	public void setPaint(Paint fontPaint) {
		fontPaint.setTypeface(Typeface.SERIF);
		fontPaint.setTextAlign(Align.CENTER);
		fontPaint.setTextSize(textSize);
		fontPaint.setAntiAlias(true);
		this.fontPaint = fontPaint;
	}

	private int upperY;
	private int lowerY;
	private float width;
	private float textSize;
	private void calculate() {
		int start = sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int horizontalSpace = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.TOP_EDGE)-start;
		
		// find such text size that upper char will fill vertically entire horizontalSpace
		fontPaint.setTextSize(horizontalSpace);
		Rect bounds = new Rect();
		fontPaint.getTextBounds(upper, 0, 1, bounds);
		textSize = ((float) horizontalSpace)/bounds.height() * horizontalSpace;
		fontPaint.setTextSize(textSize);
		
		width = Math.max(fontPaint.measureText(upper), fontPaint.measureText(lower));
		upperY = start + horizontalSpace;
		fontPaint.getTextBounds(lower, 0, 1, bounds);
		lowerY = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.BOTTOM_EDGE) + bounds.height();
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		canvas.drawText(upper, width/2, upperY, fontPaint);
		canvas.drawText(lower, width/2, lowerY, fontPaint);
	}

	@Override
	public int measureHeight() {
		return 
		sheetParams.anchorOffset(LINE4_ABSINDEX, AnchorPart.TOP_EDGE);
	}

	@Override
	public int measureWidth() {
		return ((int) width);
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return 
		sheetParams.anchorOffset(anchorAbsIndex, part) 
		- (sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.TOP_EDGE));
	}

}
