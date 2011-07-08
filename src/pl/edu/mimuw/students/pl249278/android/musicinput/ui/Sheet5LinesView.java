package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Sheet5LinesView extends View {

	private static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
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


	SheetParams params;
	private int lineThickness;
	private int totalVerticalSpan;
	private int notesAreaLeftPadding;
	public void setParams(SheetParams params) {
		this.params = params;
		lineThickness = params.getLineThickness();
		totalVerticalSpan = params.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		LogUtils.info("Sheet5LinesView::onDraw (canvas.size: %dx%d)", canvas.getWidth(), canvas.getHeight());
		Paint paint = new Paint();
		for(int i = 0; i < 5; i++) {
			int anchorIndex = NoteConstants.anchorIndex(i, NoteConstants.ANCHOR_TYPE_LINE);
			canvas.drawRect(
				notesAreaLeftPadding-lineThickness, params.anchorOffset(anchorIndex, AnchorPart.TOP_EDGE),
				getWidth(), params.anchorOffset(anchorIndex, AnchorPart.BOTTOM_EDGE),
				paint
			);
		}
		int line0top = params.anchorOffset(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		int line4bottom = params.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		canvas.drawRect(
			notesAreaLeftPadding-lineThickness, line0top,
			notesAreaLeftPadding, line4bottom,
			paint
		);
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
//		LogUtils.info("Sheet5LinesView::onMeasure(%s %d, %s %d) reports size %dx%d", 
//			ReflectionUtils.findConst(MeasureSpec.class, "", MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.getSize(widthMeasureSpec),
//			ReflectionUtils.findConst(MeasureSpec.class, "", MeasureSpec.getMode(heightMeasureSpec)), MeasureSpec.getSize(heightMeasureSpec),
//			width, totalVerticalSpan
//		);
		setMeasuredDimension(width, totalVerticalSpan);
	}

	public int getMinPadding() {
		return 2*lineThickness;
	}

	public void setNotesAreaLeftPadding(int paddingLeft) {
		this.notesAreaLeftPadding = paddingLeft;
	}
}
