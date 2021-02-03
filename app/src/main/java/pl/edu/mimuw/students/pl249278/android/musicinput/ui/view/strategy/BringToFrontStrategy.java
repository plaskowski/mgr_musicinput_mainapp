package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.View;
import android.view.ViewGroup;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.DrawingChildOnTop;

public class BringToFrontStrategy extends ViewGroupStrategyBase {
	private View frontChildView;
	private int index;

	public BringToFrontStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ViewGroup.class);
		checkThatViewImplements(DrawingChildOnTop.class);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);
		internals().setChildrenDrawingOrderEnabled(true);
	}

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall) {
		// find index of choosen view
		index = ViewUtils.indexOf((ViewGroup) internals().viewObject(), frontChildView);
		super.onLayout(changed, l, t, r, b, superCall);
	}

	@Override
	public int getChildDrawingOrder(int childCount, int i, GetChildDrawingOrderSuperCall superCall) {
		if(index == -1) {
			return super.getChildDrawingOrder(childCount, i, superCall);
		} else if(i < index) {
			return i;
		} else if(i < childCount - 1) {
			return i + 1;
		} else {
			return index;
		}
	}

	public void setFrontChildView(View frontChildView) {
		this.frontChildView = frontChildView;
	}

}
