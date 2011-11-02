package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import java.util.Collection;

import pl.edu.mimuw.students.pl249278.android.common.ContextUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Action;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware.IndicatorOrigin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable.CompoundDrawable;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class QuickActionsView extends LinearLayout {
	private int spacing, buttonStyleId, toggleButtonStyleId;
	private Collection<PaintSetup> iconPaintSetup;
	private static final int UNDEFINED = -1;

 	public QuickActionsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public QuickActionsView(Context context) {
		super(context);
		init(context, null);
	}
	
	private void init(Context ctx, AttributeSet attrs) {
		if(attrs != null) {
			ExtendedResourcesFactory.loadExtendedBackground(this, ctx, attrs);
			TypedArray styledAttributes = ctx.obtainStyledAttributes(attrs,R.styleable.QuickActions);
			spacing = styledAttributes.getDimensionPixelOffset(R.styleable.QuickActions_itemSpacing, 0);
			buttonStyleId = styledAttributes.getResourceId(R.styleable.QuickActions_buttonStyle, UNDEFINED);
			toggleButtonStyleId = styledAttributes.getResourceId(R.styleable.QuickActions_toggleButtonStyle, UNDEFINED);
			int paintsSetupId = styledAttributes.getResourceId(R.styleable.QuickActions_buttonIconPaintSetup, UNDEFINED);
			if(paintsSetupId != UNDEFINED) {
				iconPaintSetup = ExtendedResourcesFactory.createPaintsSetup(ctx, attrs, paintsSetupId);
			}
		}
	}
	
	@Override
	public void addView(View child, int index,
			android.view.ViewGroup.LayoutParams params) {
		if(params instanceof MarginLayoutParams && getChildCount() > 0) {
			if(index != 0) {
				((MarginLayoutParams) params).leftMargin = spacing;
			} else {
				((MarginLayoutParams) params).rightMargin = spacing;
			}
		}
		super.addView(child, index, params);
	}

	public void setModel(Collection<Action> model) {
		// clean
		int count = this.getChildCount();
		for(int i = 0; i < count; i++) {
			View child = this.getChildAt(i);
			child.setTag(null);
			child.setOnClickListener(null);
		}
		this.removeAllViews();
		if(model != null) {
			// create new buttons
			int index = 0;
			for(Action action: model) {
				Boolean state = action.getState();
				final int styleId = state != null ? toggleButtonStyleId : buttonStyleId;
				ContextUtils.getLayoutInflater(getContext()).inflate(styleId, this);
				ImageView button = (ImageView) getChildAt(index++);
				CompoundDrawable compoundDrawable = new SvgCompoundDrawable(action.icon());
				if(iconPaintSetup != null) for(PaintSetup ps: iconPaintSetup) {
					compoundDrawable.addPaintSetup(ps);
				}
				button.setImageDrawable(compoundDrawable);
				
				button.setOnClickListener(clickListener);
				button.setTag(action);
				if(state != null)
					button.setSelected(state);
			}
		}
	}
	
	private static class SvgCompoundDrawable extends CompoundDrawable {
		private SvgImage svgImage;
		
		private SvgCompoundDrawable(SvgImage svgImage) {
			this.svgImage = svgImage;
		}

		@Override
		protected void draw(Canvas canvas, Paint paint, float width, float height) {
			float Xscale = width/svgImage.getWidth();
			float Yscale = height/svgImage.getHeight();
			if(Xscale == Yscale) {
				SvgRenderer.drawSvgImage(canvas, svgImage, Xscale, paint);
			} else {
				canvas.save();
				if(Xscale < Yscale) {
					canvas.translate(0, (height-Xscale*svgImage.getHeight())/2f);
				} else {
					canvas.translate((width-Yscale*svgImage.getWidth())/2f, 0);
				}
				SvgRenderer.drawSvgImage(canvas, svgImage, Math.min(Xscale, Yscale), paint);
				canvas.restore();
			}
		}
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Action action = (Action) v.getTag();
			action.perform();
			v.setSelected(!v.isSelected());
		}
	};
	
	public void setIndicatorOrigin(IndicatorOrigin origin) {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			((IndicatorAware) bg).setIndicatorOrigin(origin);
			setBackgroundDrawable(getBackground());
		}
	}
	public void setOriginX(int indicatorOriginX) {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			((IndicatorAware) bg).setOriginX(indicatorOriginX);
		}
	}
	public void getOriginPostionMargin(Rect margins) {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			((IndicatorAware) bg).getOriginPostionMargin(margins);
		} else {
			margins.set(0, 0, 0, 0);
		}
		margins.top = margins.bottom = 0;
	}
	public int getIndicatorEndY() {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			Rect margins = new Rect();
			((IndicatorAware) bg).getOriginPostionMargin(margins);
			switch(((IndicatorAware) bg).getIndicatorOrigin()) {
			case BOTTOM:
				return getMeasuredHeight()-margins.bottom;
			case TOP:
				return margins.top;
			}
		}
		return 0;
	}
	
	//=== helper methods ===//
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

}
