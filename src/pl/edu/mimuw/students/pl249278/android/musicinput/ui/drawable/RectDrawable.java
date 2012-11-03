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
		Rect padding = constantState.padding;
		Rect inset = constantState.inset;
		result.set(
			padding.left + inset.left, 
			padding.top + inset.top,
			padding.right + inset.right,
			padding.bottom + inset.bottom
		);
		return result.left != 0 && result.right != 0 && result.top != 0 && result.bottom != 0;
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
		Rect padding;
		Rect inset = new Rect();
		
		public ConstantState(StyleResolver resolver) {
			padding = ExtendedResourcesFactory.getPadding(resolver, 0, null);
			TypedArray values = resolver.obtainStyledAttributes(R.styleable.Inset);
			int insetCommon = values.getDimensionPixelSize(R.styleable.Inset_inset, 0);
			inset.set(
				values.getDimensionPixelSize(R.styleable.Inset_insetLeft, insetCommon),
				values.getDimensionPixelSize(R.styleable.Inset_insetTop, insetCommon),
				values.getDimensionPixelSize(R.styleable.Inset_insetRight, insetCommon),
				values.getDimensionPixelSize(R.styleable.Inset_insetBottom, insetCommon)
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
