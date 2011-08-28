package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActionBar extends LinearLayout {
	
	public ActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ActionBar(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		rebuildBarBackground(context, IndicatorOrigin.BOTTOM);
	}
	
	public interface Action {
		SvgImage icon();
		void perform();
	}
	
	private List<Action> actions = new ArrayList<ActionBar.Action>();
	
	public void addToggleAction(Action action, boolean initState) {
		ImageView button = mAddAction(action, true);
		button.setSelected(initState);
	}
	public void addAction(Action action) {
		mAddAction(action, false);
	}
	public ImageView mAddAction(Action action, boolean isToggleType) {
		actions.add(action);
		ImageView button = new ImageView(getContext());
		Resources res = getContext().getResources();
		button.setBackgroundDrawable(buttonBackground(res));
		button.setImageDrawable(
			isToggleType 
			? toogleButtonDrawable(res, action.icon()) 
			: buttonDrawable(res, action.icon())
		);
		button.setOnClickListener(clickListener);
		this.addView(
			button,
			new LayoutParams(
				res.getDimensionPixelSize(R.dimen.actionbar_button_w), 
				res.getDimensionPixelSize(R.dimen.actionbar_button_h) 
			)
		);
		button.setTag(action);
		return button;
	}
	
	public void clear() {
		actions.clear();
		int count = this.getChildCount();
		for(int i = 0; i < count; i++) {
			this.getChildAt(i).setTag(null);
		}
		this.removeAllViews();
	}
	

	public static enum IndicatorOrigin {
		TOP,
		BOTTOM,
		NONE
	};
	public void setIndicator(IndicatorOrigin indicator) {
		rebuildBarBackground(getContext(), indicator);
	}
	public int measureHeight() {
		onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		return this.getMeasuredHeight();
	}
	
	public int measureWidth() {
		onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		return this.getMeasuredWidth();
	}
	
	public void measure() {
		onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Action action = (Action) v.getTag();
			action.perform();
			v.setSelected(!v.isSelected());
		}
	};
	
	// ========== DRAWABLE HELPERS ============== //
	
	public int getIndicatorEndX() {
		return getIndicatorX();
	}
	public int getIndicatorEndY() {
		switch(getIndicatorOrigin()) {
		case BOTTOM:
			return getMeasuredHeight();
		default: 
			return 0;
		}
	}
	public int getIndicatorOriginMarginLeft() {
		return ((BackgroundDrawable) getBackground()).getIndicatorOriginMargin();
	}
	public int getIndicatorOriginMarginRight() {
		return ((BackgroundDrawable) getBackground()).getIndicatorOriginMargin();
	}
	private IndicatorOrigin getIndicatorOrigin() {
		return ((BackgroundDrawable) getBackground()).getIndicatorOrigin();
	}
	public void setIndicatorOriginX(int indicatorOriginX) {
		BackgroundDrawable backgroundDrawable = (BackgroundDrawable) getBackground();
		backgroundDrawable.setIndicatorOriginX(indicatorOriginX - backgroundDrawable.getIndicatorOriginMargin());
	}
	private int getIndicatorX() {
		BackgroundDrawable backgroundDrawable = (BackgroundDrawable) getBackground();
		return backgroundDrawable.getIndicatorOriginX() + backgroundDrawable.getIndicatorOriginMargin();
	}
	private void rebuildBarBackground(Context context, IndicatorOrigin indicator) {
		BackgroundDrawable backgroundDrawable = new BackgroundDrawable(context.getResources(), indicator);
		this.setBackgroundDrawable(backgroundDrawable);
	}
	
	private static Drawable buttonBackground(Resources res) {
		StateListDrawable result = new StateListDrawable();
		result.addState(
			new int[] {android.R.attr.state_pressed}, 
			new SimpleRect(res.getColor(R.color.actionbar_button_bg_pressed), res.getDimensionPixelOffset(R.dimen.actionbar_inner_padding))
		);
		return result;
	}
	
	private static Drawable toogleButtonDrawable(Resources res, SvgImage icon) {
		StateListDrawable result = new StateListDrawable();
		result.addState(new int[] {android.R.attr.state_selected}, new ShadowAtEdgeDrawable(icon, 
			res.getColor(R.color.actionbar_button_icon_selected)
		));
		result.addState(new int[] {}, new ShadowAtEdgeDrawable(icon, 
				res.getColor(R.color.actionbar_button_icon)
		));
		return result;
	}
	
	private static Drawable buttonDrawable(Resources res, SvgImage svgIcon) {
		return new ShadowAtEdgeDrawable(svgIcon, res.getColor(R.color.actionbar_button_icon_unmutable));
	}
	
	// =========== DRAWABLES ============= //
	private static abstract class TranscluentDrawable extends Drawable {
		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}
	}
	
	private static class SimpleRect extends TranscluentDrawable {

		private int fillColor;
		private int innerMargin;

		public SimpleRect(int fillColor, int innerMargin) {
			super();
			this.fillColor = fillColor;
			this.innerMargin = innerMargin;
		}

		@Override
		public void draw(Canvas canvas) {
			int w = getBounds().width(), h = getBounds().height();
			Paint paint = new Paint();
			paint.setColor(this.fillColor);
			paint.setStyle(Style.FILL);
			canvas.drawRect(0, 0, w, h, paint);
		}
		
		@Override
		public boolean getPadding(Rect padding) {
			padding.set(innerMargin, innerMargin, innerMargin, innerMargin);
			return true;
		}
	}
	
	@SuppressWarnings("unused")
	private static class EmbossedBorder extends TranscluentDrawable {
		private int lineThickness = 4;
		private Paint paint;
		private MaskFilter maskfilter;

		public EmbossedBorder(int color, boolean neonStyle) {
			paint = new Paint();
			paint.setColor(color);
			paint.setAntiAlias(true);
			paint.setStyle(Style.FILL);
			maskfilter = neonStyle ? new BlurMaskFilter(lineThickness/3, Blur.INNER) : null;
		}

		@Override
		public void draw(Canvas canvas) {
			int drawPadding = lineThickness/2;
			int w = getBounds().width(), h = getBounds().height();
			
			Path path = new Path();
			path.addRect(
				drawPadding,
				drawPadding,
				w-drawPadding,
				h-drawPadding,
				Path.Direction.CCW
			);
			path.addRect(
				drawPadding+lineThickness,
				drawPadding+lineThickness,
				w-drawPadding-lineThickness,
				h-drawPadding-lineThickness,
				Path.Direction.CCW
			);
			path.setFillType(FillType.EVEN_ODD);
			
			if(maskfilter != null) {
				int colorBackup = paint.getColor();
				paint.setColor(Color.BLACK);
				paint.setMaskFilter(null);
				canvas.drawPath(path, paint);
				paint.setColor(colorBackup);
				paint.setMaskFilter(maskfilter);
			}
			canvas.drawPath(path, paint);
		}
		
		@Override
		public boolean getPadding(Rect padding) {
			padding.set(
				2*lineThickness,
				2*lineThickness,
				2*lineThickness,
				2*lineThickness
			);
			return true;
		}
	}

	private static class BackgroundDrawable extends TranscluentDrawable {
		
		private int lineThickness;
		private int drawPadding;
		private int tr;
		private int innerPadding;
		private Paint fillPaint;
		private Paint strokePaint;
		private BlurMaskFilter shadowFilter;
		private int borderColor;
		private IndicatorOrigin indicatorOrigin;
		private int indicatorOriginX;
	
		public BackgroundDrawable(Resources resources, IndicatorOrigin indicatorOrigin) {
			lineThickness = resources.getDimensionPixelSize(R.dimen.actionbar_border);
			tr = resources.getDimensionPixelSize(R.dimen.actionbar_pointer_height);
			innerPadding = resources.getDimensionPixelOffset(R.dimen.actionbar_inner_padding);
			drawPadding = lineThickness;
			shadowFilter = new BlurMaskFilter(lineThickness/2, Blur.NORMAL);
			borderColor = resources.getColor(R.color.actionbar_stroke);
			
			strokePaint = new Paint();
			strokePaint.setAntiAlias(true);
			strokePaint.setStyle(Style.STROKE);
			strokePaint.setStrokeCap(Cap.SQUARE);
			strokePaint.setStrokeWidth(lineThickness);
			
			fillPaint = new Paint();
			fillPaint.setColor(resources.getColor(R.color.actionbar_fill));
			
			this.indicatorOrigin = indicatorOrigin;
		}
		
		@Override
		public boolean getPadding(Rect padding) {
			int base = drawPadding + innerPadding;
			padding.set(base, base, base, base);
			switch(indicatorOrigin) {
			case TOP:
				padding.top += tr;
				break;
			case BOTTOM:
				padding.bottom += tr;
			}
			return true;
		}
	
		@Override
		public void draw(Canvas canvas) {
			int w = getBounds().width()-2*drawPadding, h = getBounds().height()-2*drawPadding;
			int indLeftW = Math.min(tr/2, indicatorOriginX);
			int indRightW = Math.min(tr/2, w - indicatorOriginX);
			
			Path path = new Path();
			path.moveTo(drawPadding, drawPadding);
			if(indicatorOrigin == IndicatorOrigin.TOP) {
				path.rMoveTo(0, tr);
				path.rLineTo(indicatorOriginX-indLeftW, 0);
				path.rLineTo(indLeftW, -tr);
				path.rLineTo(indRightW, tr);
				path.lineTo(drawPadding+w, drawPadding+tr);
			} else {
				path.rLineTo(w, 0);
			}
			if(indicatorOrigin == IndicatorOrigin.BOTTOM) {
				path.rLineTo(0, h-tr);
				path.rLineTo(-(w-indicatorOriginX-indRightW), 0);
				path.rLineTo(-indRightW, tr);
				path.rLineTo(-indLeftW, -tr);
				path.rLineTo(-(indicatorOriginX-indLeftW), 0);
			} else {
				path.lineTo(drawPadding+w, drawPadding+h);
				path.rLineTo(-w, 0);
			}
			path.close();
			
			canvas.save();
			
			// FILL
			canvas.drawPath(path, fillPaint);
			// DRAW BORDER SHADOW
			canvas.translate(lineThickness/2, lineThickness/2);
			strokePaint.setColor(0xFF000000);
			strokePaint.setMaskFilter(shadowFilter);
			canvas.drawPath(path, strokePaint);
			// DRAW BORDER
			canvas.translate(-lineThickness/2, -lineThickness/2);
			strokePaint.setMaskFilter(null);
			strokePaint.setColor(borderColor);
			canvas.drawPath(path, strokePaint);
			
			canvas.restore();
		}

		public IndicatorOrigin getIndicatorOrigin() {
			return indicatorOrigin;
		}

		private void setIndicatorOriginX(int indicatorOriginX) {
			this.indicatorOriginX = indicatorOriginX;
		}

		private int getIndicatorOriginX() {
			return indicatorOriginX;
		}
		
		private int getIndicatorOriginMargin() {
			return drawPadding;
		}

	}

	private static class ShadowAtEdgeDrawable extends Drawable {
		
		private SvgImage image;
		private int color;
		private Paint paint;
		
		public ShadowAtEdgeDrawable(SvgImage image, int color) {
			super();
			this.image = image;
			this.color = color;
			paint = new Paint();
			paint.setAntiAlias(true);
		}


		@Override
		public int getIntrinsicWidth() {
			return (int) image.getWidth();
		}
		@Override
		public int getIntrinsicHeight() {
			return (int) image.getHeight();
		}

		@Override
		public void draw(Canvas canvas) {
			float scale = getBounds().width()/image.getWidth();
			paint.setMaskFilter(null);
			paint.setColor(Color.BLACK);
			SvgRenderer.drawSvgImage(canvas, image, scale, paint);
			paint.setColor(color);
			paint.setMaskFilter(new BlurMaskFilter(2, Blur.INNER));
			SvgRenderer.drawSvgImage(canvas, image, scale, paint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
			paint.setAlpha(alpha);
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			paint.setColorFilter(cf);
		}

	}
}
