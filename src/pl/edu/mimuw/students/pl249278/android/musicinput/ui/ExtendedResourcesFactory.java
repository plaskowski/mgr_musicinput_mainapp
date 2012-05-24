package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

public class ExtendedResourcesFactory {
	private static LogUtils log = new LogUtils(ExtendedResourcesFactory.class);
	
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

		@Override
		public Resources getResources() {
			return ctx.getResources();
		}
	}
	

	public static void loadExtendedImage(ImageView view, Context context, AttributeSet attrs) {
		Drawable dr = inflateExtendedDrawable(
			context, attrs,
			R.styleable.ExtendedImage,
			R.styleable.ExtendedImage_extendedImage
		);
		if(dr != null) {
			view.setImageDrawable(dr);
		}
	}
	
	public static void loadExtendedBackground(View view, final Context context, final AttributeSet attrs) {
		Drawable dr = inflateExtendedDrawable(
			context, attrs,
			R.styleable.ExtendedBackground,
			R.styleable.ExtendedBackground_extendedBackground
		);
		if(dr != null) {
			view.setBackgroundDrawable(dr);
		}
	}
	
	private static SparseArray<ExtendedDrawableState> drawableConstantState = new SparseArray<ExtendedDrawableState>();
	private static Configuration lastConfiguration;
	
	private static Drawable inflateExtendedDrawable(Context ctx, AttributeSet attrsSet, int[] attrs, int attribute) {
		TypedArray styledAttributes = ctx.obtainStyledAttributes(attrsSet, attrs);
		final int styleId = styledAttributes.getResourceId(attribute, -1);
		if(styleId != -1) {
			Configuration currentConf = ctx.getResources().getConfiguration();
			if(lastConfiguration == null) {
				lastConfiguration = new Configuration(currentConf);
			} else if(!currentConf.equals(lastConfiguration)) {
				// configuration changed, we must discard any cached drawables
				drawableConstantState.clear();
				lastConfiguration.setTo(currentConf);
			}
			ExtendedDrawableState constState = drawableConstantState.get(styleId);
			if(constState != null) {
				return constState.newDrawable();
			}
			Object obj = inflateExtendedObject(ctx, attrsSet, styleId);
			if(obj instanceof Drawable) {
				return (Drawable) obj;
			} else if(obj instanceof ExtendedDrawableState) {
				constState = (ExtendedDrawableState) obj;
				drawableConstantState.put(styleId, constState);
				return constState.newDrawable();
			} else {
				throw new RuntimeException(
					"Inflating style "+styleName(styleId)+" expected Drawable object, got "
					+ obj.getClass()
				);
			}
		} else {
			return null;
		}
	}

	private static Object inflateExtendedObject(final Context ctx, final AttributeSet attrsSet, int styleId) {
		// load style
		TypedArray attrsArray = ctx.obtainStyledAttributes(attrsSet, R.styleable.StyleableClassDeclaration, 0, styleId);
		String className = attrsArray.getString(R.styleable.StyleableClassDeclaration_className);
		log.v("Inflating extended class %s", className);
		if(className == null) {
			throw new RuntimeException("No className defined in style "+styleName(styleId)+"#"+styleId);
		}
		try {
			Class<?> classObj = Class.forName(className);
			Constructor<?> constructor = classObj.getConstructor(StyleResolver.class);
			Object obj = constructor.newInstance(new SimpleResolver(styleId, attrsSet, ctx));
			return obj;
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

	private static String styleName(int styleId) {
		return ReflectionUtils.findConstName(R.style.class, "", styleId);
	}
	
	public static Shader createGradient(StyleResolver resolver, int gradientStyleId) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Gradient, gradientStyleId);
		int type = values.getResourceId(R.styleable.Gradient_gradientType, -1);
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
				styleName(gradientStyleId)
			));
		}
	}

	public static Paint createPaint(StyleResolver resolver, int paintStyleId) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Paint, paintStyleId);
		Paint result = new Paint();
		result.setAntiAlias(values.getBoolean(R.styleable.Paint_antialias, result.isAntiAlias()));
		result.setColor(values.getColor(R.styleable.Paint_color, result.getColor()));
		if(values.hasValue(R.styleable.Paint_paintStyle)) {
			result.setStyle(Style.valueOf(values.getString(R.styleable.Paint_paintStyle)));
		}
		if(values.hasValue(R.styleable.Paint_strokeWidth)) {
			result.setStrokeWidth(values.getDimension(R.styleable.Paint_strokeWidth, 0));
		}
		if(hasAll(values, paint_shadowLayer_required)) {
			result.setShadowLayer(
				values.getDimension(R.styleable.Paint_shadowLayer_radius, 0),
				values.getDimension(R.styleable.Paint_shadowLayer_dx, 0),
				values.getDimension(R.styleable.Paint_shadowLayer_dy, 0),
				values.getColor(R.styleable.Paint_shadowLayer_color, Color.BLACK)
			);
		}
		if(values.hasValue(R.styleable.Paint_pathEffect)) {
			result.setPathEffect(createPathEffect(
				resolver, 
				values.getResourceId(R.styleable.Paint_pathEffect, 0)
			));
		}
		values.recycle();
		return result;
	}
	
	private static PathEffect createPathEffect(StyleResolver resolver, int pathEffectStyleId) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.PathEffect, pathEffectStyleId);
		try {
			switch(values.getResourceId(R.styleable.PathEffect_effectType, -1)) {
			case R.id.PathEffectType_Corner:
				if(values.hasValue(R.styleable.PathEffect_cornerRadius)) {
					return new CornerPathEffect(
						values.getDimension(R.styleable.PathEffect_cornerRadius, 0)
					);
				} else {
					throw new RuntimeException("Style  "
						+ styleName(pathEffectStyleId)
						+ " doesn't specify cornerRadius attribute.");
				}
			case R.id.PathEffectType_Dash:
				if(!values.hasValue(R.styleable.PathEffect_intervals)) {
					throw new RuntimeException("Style  "
						+ styleName(pathEffectStyleId)
						+ " doesn't specify intervals attribute.");
				}
				return new DashPathEffect(
					parseFloatArray(resolver, values.getResourceId(R.styleable.PathEffect_intervals, 0)),
					values.getResourceId(R.styleable.PathEffect_phase, 0)
				);
			default:
				throw new RuntimeException("Style  "
					+ styleName(pathEffectStyleId)
					+ ".effectType points to unknown id.");
			}
		} finally {
			values.recycle();
		}
	}

	private static float[] parseFloatArray(StyleResolver resolver, int arrayResId) {
		TypedArray array = resolver.getResources().obtainTypedArray(arrayResId);
		try {
			int size = array.length();
			float[] result = new float[size];
			for(int i = 0; i < size; i++) {
				result[i] = array.getDimension(i, 0);
			}
			return result;
		} finally {
			array.recycle();
		}
	}

	/** attributes of Paint.shadowLayer that are required */
	private static final int[] paint_shadowLayer_required = new int[] {
		R.styleable.Paint_shadowLayer_radius,
		R.styleable.Paint_shadowLayer_color
	};
	
	private static boolean hasAll(TypedArray values, int[] ids) {
		boolean result = true;
		for (int i = 0; i < ids.length; i++) {
			result &= values.hasValue(ids[i]);
		}
		return result;
	}
	
	public static PaintSetup createPaintSetup(StyleResolver resolver, int paintSetupStyleId) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.PaintSetup, paintSetupStyleId);
		PaintSetup result = new PaintSetup(
			createPaint(resolver, paintSetupStyleId),
			new PointF(
				values.getDimension(R.styleable.PaintSetup_offsetX, 0),
				values.getDimension(R.styleable.PaintSetup_offsetY, 0)
			),
			values.getDimension(R.styleable.PaintSetup_drawRadius, 0)
		);
		values.recycle();
		return result;
	}

	public static List<PaintSetup> createPaintsSetup(StyleResolver resolver, int paintsSetupResId) {
		LinkedList<PaintSetup> result = new LinkedList<PaintSetup>();
		Resources resources = resolver.getResources();
		if(resources.getResourceTypeName(paintsSetupResId).equals(
			resources.getResourceTypeName(R.style.StyleResourceMockup))) {
			result.add(createPaintSetup(resolver, paintsSetupResId));
		} else {
			TypedArray values = resources.obtainTypedArray(paintsSetupResId);
			try {
			for(int i = 0; i < values.length(); i++) {
				int value = values.getResourceId(i, 0);
				if(value != 0) {
					result.add(createPaintSetup(resolver, value));
				}
			}
			} finally {
			values.recycle();
			}
		}
		return result;
	}
	
	public static int getPadding(StyleResolver resolver, int defValue) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Padding);
		int result = values.getDimensionPixelSize(R.styleable.Padding_padding, defValue);
		values.recycle();
		return result;
	}
	
	public static Rect getPadding(StyleResolver resolver, int defValue, Rect out) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.Padding);
		int padding = values.getDimensionPixelSize(R.styleable.Padding_padding, defValue);
		Rect rect = out == null ? new Rect() : out;
		rect.set(
			values.getDimensionPixelSize(R.styleable.Padding_paddingLeft, padding),
			values.getDimensionPixelSize(R.styleable.Padding_paddingTop, padding),
			values.getDimensionPixelSize(R.styleable.Padding_paddingRight, padding),
			values.getDimensionPixelSize(R.styleable.Padding_paddingBottom, padding)
		);
		values.recycle();
		return rect;
	}

	public static StyleResolver styleResolver(Context ctx, AttributeSet attrs) {
		return new SimpleResolver(0, attrs, ctx);
	}

	public static StyleResolver styleResolver(Context context, AttributeSet attrs,
			int defStyle) {
		return new SimpleResolver(defStyle, attrs, context);
	}
	
	public static StyleResolver styleResolver(Resources resources) {
		return new NoAttrSetResolver(resources);
	}

	private static class NoAttrSetResolver implements StyleResolver {
		private Resources resources;
		
		public NoAttrSetResolver(Resources resources) {
			this.resources = resources;
		}

		@Override
		public TypedArray obtainStyledAttributes(int[] attrs) {
			Theme theme = resources.newTheme();
			return theme.obtainStyledAttributes(attrs);
		}

		@Override
		public TypedArray obtainStyledAttributes(int[] attrs, int styleId) {
			Theme theme = resources.newTheme();
			return theme.obtainStyledAttributes(styleId, attrs);
		}

		@Override
		public Resources getResources() {
			return resources;
		}
		
	}
	
	public static interface ExtendedDrawableState {
		Drawable newDrawable();		
	}
}
