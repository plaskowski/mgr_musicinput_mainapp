package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;

import java.util.Collection;

import pl.edu.mimuw.students.pl249278.android.common.ContextUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Action;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware.IndicatorOrigin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable.CompoundDrawable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable.SvgCompoundDrawable;

import static android.view.View.MeasureSpec.makeMeasureSpec;

public class QuickActionsView extends GridLayout {
	private int buttonStyleId, toggleButtonStyleId;
	private Collection<PaintSetup> iconPaintSetup, inactivePaintSetup;
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
			ViewInflationContext viewInflationContext = new ViewInflationContext(ctx, attrs);
			ExtendedResourcesFactory.loadExtendedBackground(this, viewInflationContext);
			TypedArray styledAttributes = ctx.obtainStyledAttributes(attrs,R.styleable.QuickActions);
			buttonStyleId = styledAttributes.getResourceId(R.styleable.QuickActions_buttonStyle, UNDEFINED);
			toggleButtonStyleId = styledAttributes.getResourceId(R.styleable.QuickActions_toggleButtonStyle, UNDEFINED);
			int paintsSetupId = styledAttributes.getResourceId(R.styleable.QuickActions_buttonIconPaintSetup, UNDEFINED);
			if(paintsSetupId != UNDEFINED) {
				iconPaintSetup = ExtendedResourcesFactory.createPaintsSetup(
					ExtendedResourcesFactory.styleResolver(viewInflationContext),
					paintsSetupId
				);
			}
			paintsSetupId = styledAttributes.getResourceId(R.styleable.QuickActions_inactiveButtonIconPaintSetup, UNDEFINED);
			if(paintsSetupId != UNDEFINED) {
				inactivePaintSetup = ExtendedResourcesFactory.createPaintsSetup(
					ExtendedResourcesFactory.styleResolver(viewInflationContext),
					paintsSetupId
				);
			} else {
				inactivePaintSetup = iconPaintSetup;
			}
		}
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
				boolean isActive = action.isActive();
				final int styleId = state != null ? toggleButtonStyleId : buttonStyleId;
				ContextUtils.getLayoutInflater(getContext()).inflate(styleId, this);
				ImageView button = (ImageView) getChildAt(index++);
				CompoundDrawable compoundDrawable = new SvgCompoundDrawable(action.icon());
				Collection<PaintSetup> iconPaintSetup = isActive ? this.iconPaintSetup : this.inactivePaintSetup;
				if(iconPaintSetup != null) for(PaintSetup ps: iconPaintSetup) {
					compoundDrawable.addPaintSetup(ps);
				}
				button.setImageDrawable(compoundDrawable);
				button.setEnabled(isActive);
				button.setOnClickListener(clickListener);
				if(isActive) {
					button.setTag(action);
				}
				if(state != null)
					button.setSelected(state);
			}
		}
	}
	
	private OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Action action = (Action) v.getTag();
			if(action != null) {
				action.perform();
				v.setSelected(!v.isSelected());
			}
		}
	};
	
	public void setIndicatorOrigin(IndicatorOrigin origin) {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			((IndicatorAware) bg).setIndicatorOrigin(origin);
			setBackgroundDrawable(getBackground());
		}
	}
	
	public IndicatorOrigin getIndicatorOrigin() {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			return ((IndicatorAware) bg).getIndicatorOrigin();
		} else {
			return IndicatorOrigin.NONE;
		}
	}
	
	public void setOriginX(int indicatorOriginX) {
		Drawable bg = getBackground();
		if(bg != null && bg instanceof IndicatorAware) {
			((IndicatorAware) bg).setOriginX(indicatorOriginX);
			setBackgroundDrawable(bg);
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
	/**
	 * Tries to pack all buttons in given rows number in given width, if to wide tries with more rows.
	 */
	public void measure(int maxWidth, int minRows, Point out) {
		int unsp = makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int total = getChildCount();
		for(int rows = Math.max(minRows, 1); rows <= total; rows++) {
			setColumnCount((total+rows-1)/rows);
			onMeasure(unsp, unsp);
			if(getMeasuredWidth() <= maxWidth) {
				break;
			}
		}
		out.set(getMeasuredWidth(), getMeasuredHeight());
	}

}
