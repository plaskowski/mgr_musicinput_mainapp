package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.Lines;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class LinedSheetElementView extends SheetElementView<Lines> {
	private SheetElement frontModel;
	private int offsetDiff;
	private int linesHorizontalPadding;
	
	public LinedSheetElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setModel(new Lines());
	}

	public LinedSheetElementView(Context context) {
		super(context, new Lines());
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		if(frontModel != null) {
			frontModel.setSheetParams(params);
			if(model != null)
				model.setForcedWidth(frontModel.measureWidth()+2*linesHorizontalPadding);
		}
		super.setSheetParams(params);
	}
	
	public void setFrontModel(SheetElement frontModel) {
		this.frontModel = frontModel;
		if(model != null) {
			SheetVisualParams sheetParams = model.getSheetParams();
			if(sheetParams != null) {
				frontModel.setSheetParams(sheetParams);
				model.setForcedWidth(frontModel.measureWidth()+2*linesHorizontalPadding);
				invalidateMeasure();
				invalidate();
			}
		}
	}
	
	@Override
	public int measureHeight() {
		if(frontModel == null || frontModel.getSheetParams() == null || model == null) {
			return super.measureHeight();
		}
		offsetDiff = frontModel.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE)
			- model.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		return Math.max(
			Math.max(offsetDiff, 0) + frontModel.measureHeight(),
			Math.max(-offsetDiff, 0) + model.measureHeight()
		) + getPaddingTop()+getPaddingBottom();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		if(offsetDiff < 0) {
			canvas.translate(0, -offsetDiff);
		}
		super.onDraw(canvas);
		canvas.translate(linesHorizontalPadding, offsetDiff);
		canvas.translate(
			getPaddingLeft(), 
			getPaddingTop()
		);
		frontModel.onDraw(canvas, paint);
		canvas.restore();
	}

	public void setLinesHorizontalPadding(int linesHorizontalPadding) {
		this.linesHorizontalPadding = linesHorizontalPadding;
	}
}
