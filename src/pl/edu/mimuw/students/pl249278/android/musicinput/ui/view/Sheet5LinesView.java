package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.View;

// TODO should I extract logic to SheetElement object?
public class Sheet5LinesView extends View {

	private static final int LINESPACE3_ABSINDEX = NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINESPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	
	public Sheet5LinesView(Context context) {
		super(context);
	}

	public Sheet5LinesView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Sheet5LinesView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


	SheetVisualParams params;
	private int lineThickness;
	private int totalVerticalSpan;
	private int notesAreaLeftPadding;
	private Integer highlightedAnchor = null;
	private Paint normalPaint = new Paint();
	private Paint lineHighlightedPaint = new Paint();
	private GradientDrawable linespaceHighlighted;
	
	public void setParams(SheetVisualParams params) {
		this.params = params;
		lineThickness = params.getLineThickness();
		totalVerticalSpan = params.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int shadowThickness = params.getLineThickness()/2;
		lineHighlightedPaint.setShadowLayer(shadowThickness, 0, params.getLineThickness()/4, Color.BLACK);
		setPadding(0, shadowThickness, 0, shadowThickness);
		setMeasuredDimension(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		invalidate();
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
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int paddingTop = getPaddingTop();
//		LogUtils.info("Sheet5LinesView::onDraw (canvas.size: %dx%d)", canvas.getWidth(), canvas.getHeight());
		for(int i = 0; i < 5; i++) {
			int anchorIndex = NoteConstants.anchorIndex(i, NoteConstants.ANCHOR_TYPE_LINE);
			canvas.drawRect(
				notesAreaLeftPadding, 
				paddingTop + params.anchorOffset(anchorIndex, AnchorPart.TOP_EDGE),
				getWidth(), 
				paddingTop + params.anchorOffset(anchorIndex, AnchorPart.BOTTOM_EDGE),
				highlightedAnchor != null && highlightedAnchor == anchorIndex ? lineHighlightedPaint : normalPaint
			);
		}
		if(highlightedAnchor != null 
		&& NoteConstants.anchorType(highlightedAnchor) == NoteConstants.ANCHOR_TYPE_LINESPACE
		&& highlightedAnchor >= LINESPACE0_ABSINDEX
		&& highlightedAnchor <= LINESPACE3_ABSINDEX) {
			linespaceHighlighted.setBounds(
				notesAreaLeftPadding, 
				paddingTop + params.anchorOffset(highlightedAnchor, AnchorPart.TOP_EDGE),
				getWidth(), 
				paddingTop + params.anchorOffset(highlightedAnchor, AnchorPart.BOTTOM_EDGE)
			);
			linespaceHighlighted.draw(canvas);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(params == null) {
			LogUtils.info("Sheet5LinesView::onMeasure when sheet params not yet set.");
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		
		int width = 100;
		switch(MeasureSpec.getMode(widthMeasureSpec)) {
		case MeasureSpec.AT_MOST:
		case MeasureSpec.EXACTLY:
			width = MeasureSpec.getSize(widthMeasureSpec);
			break;
		}
		setMeasuredDimension(width, totalVerticalSpan+getPaddingTop()+getPaddingBottom());
	}

	public int getMinPadding() {
		return 2*lineThickness;
	}

	public void setNotesAreaLeftPadding(int paddingLeft) {
		this.notesAreaLeftPadding = paddingLeft;
	}
}
