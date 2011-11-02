package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ExtendedResourcesFactory {
	
	private static final class SimpleResolver implements StyleResolver {
		private final int styleId;
		private final AttributeSet attrs;
		private final Context ctx;

		private SimpleResolver(int styleId, AttributeSet attrs, Context ctx) {
			this.styleId = styleId;
			this.attrs = attrs;
			this.ctx = ctx;
		}

		@Override
		public TypedArray obtainStyledAttributes(int[] styleAttributes) {
			return ctx.obtainStyledAttributes(attrs, styleAttributes, 0, styleId);
		}

		@Override
		public TypedArray obtainStyledAttributes(int[] styleAttributes, int styleId) {
			return ctx.obtainStyledAttributes(attrs, styleAttributes, 0, styleId);
		}
	}

	public static void loadExtendedBackground(View view, final Context ctx, final AttributeSet attrs) {
		TypedArray styledAttributes = ctx.obtainStyledAttributes(attrs,R.styleable.ExtendedBackground);
		final int styleId = styledAttributes.getResourceId(R.styleable.ExtendedBackground_extendedBackground, -1);
		if(styleId != -1) {
			// load style
			TypedArray attrsArray = ctx.obtainStyledAttributes(attrs, R.styleable.ExtendedBackgroundDeclaration, 0, styleId);
			String drClass = attrsArray.getString(R.styleable.ExtendedBackgroundDeclaration_drawableClass);
			LogUtils.info("drawableClass %s", drClass);
			if(drClass == null) {
				throw new RuntimeException("No drawableClass defined in style "+ReflectionUtils.findConstName(R.style.class, "", styleId));
			}
			try {
				Class<?> classObj = Class.forName(drClass);
				Constructor<?> constructor = classObj.getConstructor(StyleResolver.class);
				Object obj = constructor.newInstance(new SimpleResolver(styleId, attrs, ctx));
				if (obj instanceof Drawable) {
					view.setBackgroundDrawable((Drawable) obj);
				} else {
					throw new RuntimeException("Class pointed by drawableClass in style "+ReflectionUtils.findConstName(R.style.class, "", styleId)+" doesn't inherit from Drawable.");
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static Shader createGradient(StyleResolver resolver, int gradientStyleId) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Gradient, gradientStyleId);
		int type = values.getResourceId(R.styleable.Gradient_type, -1);
		switch(type) {
		case R.id.GradientType_Linear:
			return new LinearGradient(
				values.getFloat(R.styleable.Gradient_X0, 0),
				values.getFloat(R.styleable.Gradient_Y0, 0),
				values.getFloat(R.styleable.Gradient_X1, 0),
				values.getFloat(R.styleable.Gradient_Y1, 0),
				values.getColor(R.styleable.Gradient_color0, Color.BLACK),
				values.getColor(R.styleable.Gradient_color1, Color.BLACK),
			    Shader.TileMode.CLAMP
			);
		default:
			throw new RuntimeException(String.format(
				"Uknown gradient type %d in gradient style %s",
				type,
				ReflectionUtils.findConstName(R.style.class, "", gradientStyleId)
			));
		}
	}

	public static Paint createPaint(Context ctx, AttributeSet attrsSet, int paintStyleId) {
		TypedArray values = ctx.obtainStyledAttributes(attrsSet, R.styleable.Paint, 0, paintStyleId);
		Paint result = new Paint();
		result.setAntiAlias(values.getBoolean(R.styleable.Paint_antialias, result.isAntiAlias()));
		result.setColor(values.getColor(R.styleable.Paint_color, result.getColor()));
		if(values.hasValue(R.styleable.Paint_paintStyle)) {
			result.setStyle(Style.valueOf(values.getString(R.styleable.Paint_paintStyle)));
		}
		if(values.hasValue(R.styleable.Paint_strokeWidth)) {
			result.setStrokeWidth(values.getDimension(R.styleable.Paint_strokeWidth, 0));
		}
		values.recycle();
		return result;
	}
	
	private static PaintSetup createPaintSetup(Context ctx, AttributeSet attrs, int paintSetupStyleId) {
		TypedArray values = ctx.obtainStyledAttributes(attrs, R.styleable.PaintSetup, 0, paintSetupStyleId);
		PaintSetup result = new PaintSetup(
			createPaint(ctx, attrs, paintSetupStyleId),
			new PointF(
				values.getDimension(R.styleable.PaintSetup_offsetX, 0),
				values.getDimension(R.styleable.PaintSetup_offsetY, 0)
			),
			values.getDimension(R.styleable.PaintSetup_drawRadius, 0)
		);
		values.recycle();
		return result;
	}

	public static Collection<PaintSetup> createPaintsSetup(Context ctx, AttributeSet attrs, int resId) {
		LinkedList<PaintSetup> result = new LinkedList<PaintSetup>();
		TypedArray values = ctx.getResources().obtainTypedArray(resId);
		for(int i = 0; i < values.length(); i++) {
			int value = values.getResourceId(i, 0);
			if(value != 0) {
				result.add(createPaintSetup(ctx, attrs, value));
			}
		}
		values.recycle();
		return result;
	}
	
}
