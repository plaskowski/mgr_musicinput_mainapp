package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.Lines;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class LinedSheetElementView extends SheetElementView<Lines> {
	private SheetElement frontModel;
	private int offsetDiff;
	private int linesHorizontalPadding;
	private int minLinesWidth = 0;
	
	public LinedSheetElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setModel(new Lines());
		setupVisualConf(ExtendedResourcesFactory.styleResolver(context, attrs));
	}
	
	public LinedSheetElementView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setupVisualConf(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}

	private void setupVisualConf(StyleResolver styleResolver) {
		TypedArray values = styleResolver.obtainStyledAttributes(R.styleable.LinedSheetElementView);
		minLinesWidth = values.getDimensionPixelOffset(R.styleable.LinedSheetElementView_minLinesWidth, minLinesWidth);
		linesHorizontalPadding = values.getDimensionPixelOffset(R.styleable.LinedSheetElementView_linesHorizontalPadding, linesHorizontalPadding);
		values.recycle();
	}

	public LinedSheetElementView(Context context) {
		super(context, new Lines());
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		if(frontModel != null) {
			frontModel.setSheetParams(params);
		}
		super.setSheetParams(params);
		recalculateSize();
	}

	private void recalculateSize() {
		if(model != null && frontModel != null && frontModel.getSheetParams() != null) {
			model.setForcedWidth(Math.max(
				frontModel.measureWidth()+2*linesHorizontalPadding,
				minLinesWidth
			));
		}
		measureHeight();
	}
	
	public void setFrontModel(SheetElement frontModel) {
		this.frontModel = frontModel;
		if(model != null) {
			SheetVisualParams sheetParams = model.getSheetParams();
			if(sheetParams != null) {
				frontModel.setSheetParams(sheetParams);
				recalculateSize();
				invalidate();
				requestLayout();
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
		canvas.translate((getWidth()-getPaddingLeft()-getPaddingRight()-frontModel.measureWidth())/2, offsetDiff);
		canvas.translate(
			getPaddingLeft(), 
			getPaddingTop()
		);
		frontModel.onDraw(canvas, paint);
		canvas.restore();
	}
	
	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return super.getOffsetToAnchor(anchorAbsIndex, part) - Math.min(offsetDiff, 0);
	}

	public void setLinesHorizontalPadding(int linesHorizontalPadding) {
		this.linesHorizontalPadding = linesHorizontalPadding;
		recalculateSize();
	}

	public void setMinLinesWidth(int minLinesWidth) {
		this.minLinesWidth = minLinesWidth;
		recalculateSize();
	}
}
