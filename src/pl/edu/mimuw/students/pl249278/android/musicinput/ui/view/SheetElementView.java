package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SheetElementView<ElementType extends SheetElement> extends View {
//	private static LogUtils log = new LogUtils(SheetElementView.class);
	protected ElementType model;
	protected Paint paint;

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
		if(model.getSheetParams() == null && this.model != null && this.model.getSheetParams() != null) {
			model.setSheetParams(this.model.getSheetParams());
		}
		this.model = model;
		invalidateMeasure();
		invalidate();
	}
	
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return
			model.getOffsetToAnchor(anchorAbsIndex, part)
			- getPaddingTop();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(model == null || model.getSheetParams() == null) {
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
	
	public void setSheetParams(SheetVisualParams params) {
		model.setSheetParams(params);
		invalidateMeasure();
		invalidate();
	}
	
	public void setPaint(Paint paint, float drawRadius) {
		this.paint = paint;
		invalidate();
		updateDrawRadius(drawRadius);
	}

	public void updateDrawRadius(float drawRadius) {
		setPadding((int) Math.ceil(drawRadius));
	}

	private void setPadding(int padding) {
		this.setPadding(padding, padding, padding, padding);
		invalidateMeasure();
	}
	
	public void invalidateMeasure() {
		onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		canvas.translate(
			getPaddingLeft(), 
			getPaddingTop()
		);
		model.onDraw(canvas, paint);
		canvas.restore();
	}
	
	public ElementType model() {
		return model;
	}

}