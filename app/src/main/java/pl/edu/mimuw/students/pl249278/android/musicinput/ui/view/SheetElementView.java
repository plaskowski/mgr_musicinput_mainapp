package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class SheetElementView<ElementType extends SheetElement> extends View {
//	private static LogUtils log = new LogUtils(SheetElementView.class);
	protected ElementType model;
	protected Paint paint;
	private int drawRadius;

	public SheetElementView(Context context, ElementType model) {
		super(context);
		setModel(model);
	}

	public SheetElementView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		loadPaint(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}
	
	public SheetElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadPaint(ExtendedResourcesFactory.styleResolver(context, attrs));
	}

	private void loadPaint(StyleResolver styleResolver) {
		TypedArray values = styleResolver.obtainStyledAttributes(R.styleable.SheetElementView);
		try {
			int paintId = values.getResourceId(R.styleable.SheetElementView_paint, -1);
			if(paintId != -1) {
				PaintSetup paint = ExtendedResourcesFactory.createPaintSetup(styleResolver, paintId);
				setPaint(paint.paint, paint.drawRadius);
			}
		} finally {
			values.recycle();
		}
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

	/** height of model (according to current {@link #setSheetParams(SheetVisualParams)}) + padding top and bottom */
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
	
	public Paint getPaint() {
		return paint;
	}

	public void updateDrawRadius(float drawRadius) {
		this.drawRadius = (int) Math.ceil(drawRadius);
		setPadding(0, false);
	}

	private void setPadding(int padding, boolean ignoreBackground) {
		this.setPadding(padding, padding, padding, padding, ignoreBackground);
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
	
	private static final ThreadLocal<Rect> sThreadLocal = new ThreadLocal<Rect>() {
		protected Rect initialValue() { return new Rect(); }
	};

	public void setPadding(int left, int top, int right, int bottom, boolean ignoreBackground) {
		Drawable bg = getBackground();
		if(!ignoreBackground && bg != null && bg.getPadding(sThreadLocal.get())) {
			Rect bgPadding = sThreadLocal.get();
			left += bgPadding.left;
			top += bgPadding.top;
			right += bgPadding.right;
			bottom += bgPadding.bottom;
		}
		setPadding(left, top, right, bottom);
	}
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		left += drawRadius;
		top += drawRadius;
		right += drawRadius;
		bottom += drawRadius;
		super.setPadding(left, top, right, bottom);
	}

}