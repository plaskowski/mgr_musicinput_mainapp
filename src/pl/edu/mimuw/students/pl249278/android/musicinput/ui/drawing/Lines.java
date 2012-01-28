package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

public class Lines extends SheetElement {

	private static final int LINESPACE3_ABSINDEX = NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINESPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	
	private int totalVerticalSpan;
	private int notesAreaLeftPadding;
	private Integer highlightedAnchor = null;
	private Paint lineHighlightedPaint = new Paint();
	private GradientDrawable linespaceHighlighted;
	private int forcedWidth = 0;
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		totalVerticalSpan = params.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int shadowThickness = params.getLineThickness()/2;
		lineHighlightedPaint.setShadowLayer(shadowThickness, 0, params.getLineThickness()/4, Color.BLACK);
		super.setSheetParams(params);
	}
	
	public void setHiglightColor(int color) {
		lineHighlightedPaint.setColor(color);
		linespaceHighlighted = new GradientDrawable(
			Orientation.TOP_BOTTOM,
			new int[] { color, Color.WHITE, Color.WHITE, color }
		);
	}
	
	/**
	 * @param anchorAbsIndex null to turn off previous highlight
	 */
	public void highlightAnchor(Integer anchorAbsIndex) {
		this.highlightedAnchor = anchorAbsIndex;
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		for(int i = 0; i < 5; i++) {
			int anchorIndex = NoteConstants.anchorIndex(i, NoteConstants.ANCHOR_TYPE_LINE);
			canvas.drawRect(
				notesAreaLeftPadding, 
				sheetParams.anchorOffset(anchorIndex, AnchorPart.TOP_EDGE),
				forcedWidth,
				sheetParams.anchorOffset(anchorIndex, AnchorPart.BOTTOM_EDGE),
				highlightedAnchor != null && highlightedAnchor == anchorIndex ? lineHighlightedPaint : paint
			);
		}
		if(highlightedAnchor != null 
		&& NoteConstants.anchorType(highlightedAnchor) == NoteConstants.ANCHOR_TYPE_LINESPACE
		&& highlightedAnchor >= LINESPACE0_ABSINDEX
		&& highlightedAnchor <= LINESPACE3_ABSINDEX) {
			linespaceHighlighted.setBounds(
				notesAreaLeftPadding, 
				sheetParams.anchorOffset(highlightedAnchor, AnchorPart.TOP_EDGE),
				forcedWidth,
				sheetParams.anchorOffset(highlightedAnchor, AnchorPart.BOTTOM_EDGE)
			);
			linespaceHighlighted.draw(canvas);
		}
	}
	
	public void setNotesAreaLeftPadding(int paddingLeft) {
		this.notesAreaLeftPadding = paddingLeft;
	}

	@Override
	public int measureWidth() {
		return forcedWidth;
	}

	@Override
	public int measureHeight() {
		return totalVerticalSpan;
	}
	
	/** Lines doesn't have any width by it's own, so it requires to have width set from outside */
	public void setForcedWidth(int forcedWidth) {
		this.forcedWidth = forcedWidth;
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return sheetParams.anchorOffset(anchorAbsIndex, part);
	}
}
