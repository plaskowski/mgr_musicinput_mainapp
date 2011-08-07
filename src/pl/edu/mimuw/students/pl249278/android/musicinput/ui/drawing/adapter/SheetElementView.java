package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SheetElementView<ElementType extends SheetElement> extends View {
//	private static LogUtils log = new LogUtils(SheetElementView.class);
	protected ElementType model;
	private Paint paint;

	public SheetElementView(Context context, ElementType model) {
		super(context);
		this.model = model;
	}

	public SheetElementView(Context context) {
		super(context);
	}
	
	public SheetElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SheetElementView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setModel(ElementType model) {
		SheetParams sheetParams = this.model == null ? null : this.model.getSheetParams();
		this.model = model;
		if(sheetParams != null) {
			this.model.setSheetParams(sheetParams);
		}
		if(this.model.getSheetParams() != null) 
			invalidateMeasure();
	}
	
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return
			model.getOffsetToAnchor(anchorAbsIndex, part)
			- getPaddingTop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(model == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		setMeasuredDimension(
			measureWidth(), 
			measureHeight()
		);
	}	

	public int measureHeight() {
		return model.measureHeight()+getPaddingTop()+getPaddingBottom();
	}

	public int measureWidth() {
		return model.measureWidth()+getPaddingLeft()+getPaddingRight();
	}
	
	public void setSheetParams(SheetParams params) {
		model.setSheetParams(params);
		invalidateMeasure();
	}
	
	public void setPaint(Paint paint) {
		this.paint = paint;
		invalidate();
	}

	public void setPadding(int padding) {
		this.setPadding(padding, padding, padding, padding);
		invalidateMeasure();
	}
	
	public void invalidateMeasure() {
		onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.translate(
			getPaddingLeft(), 
			getPaddingTop()
		);
		model.onDraw(canvas, paint);
	}
	
	public ElementType model() {
		return model;
	}

}