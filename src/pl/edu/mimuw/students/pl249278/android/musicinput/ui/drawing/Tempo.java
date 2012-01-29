package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
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
	
	public Tempo(int upper, int lower) {
		this.upper = ""+upper;
		this.lower = ""+lower;
	}

	@Override
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		if(upper != null && lower != null) {
			calculate();
		}
	}
	
	private void setPaintTextAttributes(Paint paint) {
		paint.setTypeface(Typeface.SERIF);
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(textSize);
		paint.setAntiAlias(true);
	}

	private int upperY;
	private int lowerY;
	private float width;
	private float textSize;
	private void calculate() {
		int start = sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int horizontalSpace = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.TOP_EDGE)-start;
		
		// find such text size that upper char will fill vertically entire horizontalSpace
		textPaint.setTextSize(horizontalSpace);
		Rect bounds = new Rect();
		textPaint.getTextBounds(upper, 0, 1, bounds);
		textSize = ((float) horizontalSpace)/bounds.height() * horizontalSpace;
		textPaint.setTextSize(textSize);
		
		width = Math.max(textPaint.measureText(upper), textPaint.measureText(lower));
		upperY = start + horizontalSpace;
		textPaint.getTextBounds(lower, 0, 1, bounds);
		lowerY = sheetParams.anchorOffset(LINE2_ABSINDEX, AnchorPart.BOTTOM_EDGE) + bounds.height();
	}
	
	Paint textPaint = new Paint();
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		textPaint.reset();
		textPaint.set(paint);
		setPaintTextAttributes(textPaint);
		canvas.drawText(upper, width/2, upperY, textPaint);
		canvas.drawText(lower, width/2, lowerY, textPaint);
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
