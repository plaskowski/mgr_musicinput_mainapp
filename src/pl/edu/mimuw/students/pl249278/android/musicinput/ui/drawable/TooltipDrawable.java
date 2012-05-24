package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import pl.edu.mimuw.students.pl249278.android.common.PaintBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;

public class TooltipDrawable extends CompoundDrawable implements IndicatorAware {
	private int innerPadding;
	private TooltipShape shape;

	public TooltipDrawable(StyleResolver resolver) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Padding);
		innerPadding = values.getDimensionPixelSize(R.styleable.Padding_padding, 0);
		values.recycle();
		
		TypedArray styledAttributes = resolver.obtainStyledAttributes(R.styleable.TooltipStyle);
		
		shape = new TooltipShape(styledAttributes.getDimensionPixelSize(R.styleable.TooltipStyle_indicatorSize, 0));
		
		PaintBuilder paintB = PaintBuilder.init().antialias(true);
		int cornerR = styledAttributes.getDimensionPixelSize(R.styleable.TooltipStyle_outlineCornerRadius, 0);
		if(cornerR != 0) {
			paintB.pathEffect(new CornerPathEffect(cornerR));
		}
		
		int gradientFillId = styledAttributes.getResourceId(R.styleable.TooltipStyle_fill, -1);
		if(gradientFillId != -1) {
			Shader sh = ExtendedResourcesFactory.createGradient(resolver, gradientFillId);
			addPaintSetup(
				paintB.clone().style(Style.FILL)
				.shader(sh)
				.build(),
				0, 0,
				0
			);
		}
		
		float borderTh = styledAttributes.getDimension(R.styleable.TooltipStyle_borderThickness, 0);
		if(borderTh != 0) {
			addPaintSetup(
				paintB.clone().style(Style.STROKE)
				.color(styledAttributes.getColor(R.styleable.TooltipStyle_borderColor, Color.BLACK))
				.strokeWidth(borderTh)
				.build(),
				0, 0, 
				borderTh/2f
			);
		}
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		super.getPadding(padding);
		
		Rect temp = new Rect();
		shape.getPadding(temp);
		padding.left += temp.left;
		padding.right += temp.right;
		padding.top += temp.top;
		padding.bottom += temp.bottom;
		
		padding.offset(innerPadding, innerPadding);
		
		return true;
	}

	@Override
	protected void draw(Canvas canvas, Paint paint, float width, float height) {
		shape.draw(canvas, paint, width, height);
	}

	
	@Override
	public void setIndicatorOrigin(IndicatorOrigin origin) {
		shape.setIndicatorOrigin(origin);
	}

	@Override
	public void setOriginX(int indicatorOriginX) {
		shape.setIndicatorOriginX(indicatorOriginX);
		
	}


	@Override
	public void getOriginPostionMargin(Rect margins) {
		super.getPadding(margins);
	}

	@Override
	public IndicatorOrigin getIndicatorOrigin() {
		return shape.getIndicatorOrigin();
	}
	
}
