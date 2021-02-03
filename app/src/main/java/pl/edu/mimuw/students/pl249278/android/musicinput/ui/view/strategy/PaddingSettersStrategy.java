package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Rect;

public class PaddingSettersStrategy extends ViewGroupStrategyBase {

	private Rect lastCall;

	public PaddingSettersStrategy(ViewGroupStrategy parent) {
		super(parent);
	}

	@Override
	public void setPadding(int left, int top, int right, int bottom, SetPaddingSuperCall superCall) {
		if(lastCall == null) { lastCall = new Rect(); }
		lastCall.set(left, top, right, bottom);
		super.setPadding(left, top, right, bottom, superCall);
	}
	
	public void setPaddingLeft(int left) {
		lastCall.left = left;
		updatePadding();
	}
	
	public void setPaddingTop(int top) {
		lastCall.top = top;
		updatePadding();
	}
	
	public void setPaddingRight(int right) {
		lastCall.right = right;
		updatePadding();
	}
	
	public void setPaddingBottom(int bottom) {
		lastCall.bottom = bottom;
		updatePadding();
	}

	private void updatePadding() {
		internals().super_setPadding(lastCall.left, lastCall.top, lastCall.right, lastCall.bottom);
	}

}
