package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.DrawingChildOnTop;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class BringToFrontStrategy extends DummyViewGroup implements DrawingChildOnTop {
	private View frontChildView;
	private int index;

	public BringToFrontStrategy(Context context) {
		super(context);
		setChildrenDrawingOrderEnabled(true);
	}

	public BringToFrontStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		setChildrenDrawingOrderEnabled(true);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// find index of choosen view
		index = ViewUtils.indexOf(this, frontChildView);
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		if(index == -1) {
			return super.getChildDrawingOrder(childCount, i);
		} else if(i < index) {
			return i;
		} else if(i < childCount - 1) {
			return i + 1;
		} else {
			return index;
		}
	}

	@Override
	public View getFrontChildView() {
		return frontChildView;
	}

	@Override
	public void setFrontChildView(View frontChildView) {
		this.frontChildView = frontChildView;
	}

}
