package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory.ExtendedDrawableState;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;

public class RectDrawable extends Drawable {
	private ConstantState constantState;
	private RectF mBounds = new RectF();
	private Path mPath = new Path();
	
	private RectDrawable(ConstantState constantState) {
		this.constantState = constantState;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = getBounds();
		mBounds.set(bounds);
		mPath.reset();
		inset(mBounds, constantState.inset);
		Paint strokePaint = constantState.strokePaint;
		if(strokePaint != null && strokePaint.getStyle() == Style.STROKE) {
			float strokeHalf = strokePaint.getStrokeWidth()/2f;
			mBounds.inset(strokeHalf, strokeHalf);
		}
		if(constantState.radii != null) {
			mPath.addRoundRect(mBounds, constantState.radii, Direction.CW);		
		} else {
			mPath.addRect(mBounds, Direction.CW);
		}
		if(constantState.fillPaint != null) {
			apply(constantState.fillPaint, constantState.fillColorStateList);
			canvas.drawPath(mPath, constantState.fillPaint);
		}
		if(strokePaint != null) {
			apply(strokePaint, constantState.strokeColorStateList);
			canvas.drawPath(mPath, strokePaint);
		}
	}

	private static void inset(RectF bounds, Rect inset) {
		bounds.left += inset.left;
		bounds.top += inset.top;
		bounds.right -= inset.right;
		bounds.bottom -= inset.bottom;
	}
	
	@Override
	public boolean getPadding(Rect result) {
		int padding = constantState.padding;
		Rect inset = constantState.inset;
		result.set(
			padding + inset.left, 
			padding + inset.top,
			padding + inset.right,
			padding + inset.bottom
		);
		return padding != 0;
	}

	private void apply(Paint paint, ColorStateList colorStateList) {
		if(colorStateList != null) {
			paint.setColor(colorStateList.getColorForState(getState(), 0));
		}
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
	
	@Override
	public boolean isStateful() {
		return constantState.strokeColorStateList != null 
		|| constantState.fillColorStateList != null
		|| super.isStateful();
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		boolean superResult = super.setState(stateSet);
		if(isStateful()) {
			invalidateSelf();
			return true;
		} else {
			return superResult;
		}
	}

	public static class ConstantState implements ExtendedDrawableState {
		Paint strokePaint, fillPaint;
		ColorStateList strokeColorStateList, fillColorStateList;
		float[] radii;
		int padding;
		Rect inset = new Rect();
		
		public ConstantState(StyleResolver resolver) {
			TypedArray values = resolver.obtainStyledAttributes(R.styleable.Padding);
			padding = values.getDimensionPixelSize(R.styleable.Padding_padding, 0);
			values.recycle();
			values = resolver.obtainStyledAttributes(R.styleable.Inset);
			inset.set(
				values.getDimensionPixelSize(R.styleable.Inset_insetLeft, 0),
				values.getDimensionPixelSize(R.styleable.Inset_insetTop, 0),
				values.getDimensionPixelSize(R.styleable.Inset_insetRight, 0),
				values.getDimensionPixelSize(R.styleable.Inset_insetBottom, 0)
			);
			values.recycle();
			values = resolver.obtainStyledAttributes(R.styleable.RectDrawable);
			int styleId;
			if((styleId = values.getResourceId(R.styleable.RectDrawable_fillPaint, -1)) != -1) {
				fillPaint = ExtendedResourcesFactory.createPaint(resolver, styleId);
			}
			if((styleId = values.getResourceId(R.styleable.RectDrawable_strokePaint, -1)) != -1) {
				strokePaint = ExtendedResourcesFactory.createPaint(resolver, styleId);
				// round up stroke width to full pixels
				strokePaint.setStrokeWidth(FloatMath.ceil(strokePaint.getStrokeWidth()));
			}
			fillColorStateList = values.getColorStateList(R.styleable.RectDrawable_fillColorStateList);
			strokeColorStateList = values.getColorStateList(R.styleable.RectDrawable_strokeColorStateList);
			values.recycle();
			values = resolver.obtainStyledAttributes(R.styleable.Corners);
			int topLeft = 0, topRight = 0, bottomLeft = 0, bottomRight = 0;
			boolean hasRoundedCorners = false;
			int corner;
			if((corner = values.getDimensionPixelSize(R.styleable.Corners_cornerTopLeft, 0)) > 0) {
				topLeft = corner;
				hasRoundedCorners = true;
			}
			if((corner = values.getDimensionPixelSize(R.styleable.Corners_cornerTopRight, 0)) > 0) {
				topRight = corner;
				hasRoundedCorners = true;
			}
			if((corner = values.getDimensionPixelSize(R.styleable.Corners_cornerBottomLeft, 0)) > 0) {
				bottomLeft = corner;
				hasRoundedCorners = true;
			}
			if((corner = values.getDimensionPixelSize(R.styleable.Corners_cornerBottomRight, 0)) > 0) {
				bottomRight = corner;
				hasRoundedCorners = true;
			}
			if(hasRoundedCorners) {
				radii = new float[] {
					topLeft, topLeft,
					topRight, topRight,
					bottomRight, bottomRight,
					bottomLeft, bottomLeft
				};
			}
			values.recycle();
		}

		@Override
		public Drawable newDrawable() {
			return new RectDrawable(this);
		}
		
	}
}
