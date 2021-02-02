package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.PaddingSettersStrategy;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class Sheet5LinesView extends PaddingSettersStrategy {

	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	
	public Sheet5LinesView(Context context) {
		super(context);
	}

	public Sheet5LinesView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	SheetVisualParams params;
	private int totalVerticalSpan;
	private Integer highlightedAnchor = null;
	private Paint normalPaint = new Paint();
	private Paint lineHighlightedPaint = new Paint();
	
	public void setParams(SheetVisualParams params, int topPaddingMin, int bottomPaddingMin) {
		this.params = params;
		totalVerticalSpan = params.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int shadowThickness = params.getLineThickness()/2;
		lineHighlightedPaint.setShadowLayer(shadowThickness, 0, params.getLineThickness()/4, Color.BLACK);
		setPaddingTop(Math.max(shadowThickness, topPaddingMin));
		setPaddingBottom(Math.max(shadowThickness, bottomPaddingMin));
		setMeasuredDimension(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		requestLayout();
		invalidate();
		LogUtils.debug("5::setParams(..., topM: %d, bottomM: %d) vSpan %d vPadding %d, %d",
			topPaddingMin, bottomPaddingMin, totalVerticalSpan, getPaddingTop(), getPaddingBottom()
		);
	}
	
	public void setHiglightColor(int color) {
		lineHighlightedPaint.setColor(color);
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
				getPaddingLeft(), 
				paddingTop + params.anchorOffset(anchorIndex, AnchorPart.TOP_EDGE),
				getWidth() - getPaddingRight(), 
				paddingTop + params.anchorOffset(anchorIndex, AnchorPart.BOTTOM_EDGE),
				highlightedAnchor != null && highlightedAnchor == anchorIndex ? lineHighlightedPaint : normalPaint
			);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(params == null) {
			LogUtils.log(Log.VERBOSE, LogUtils.COMMON_TAG, "Sheet5LinesView::onMeasure when sheet params not yet set.");
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
}
