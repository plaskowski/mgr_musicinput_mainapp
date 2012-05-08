package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class PaddingSettersStrategy extends View {

	public PaddingSettersStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PaddingSettersStrategy(Context context) {
		super(context);
	}
	
	private Rect lastCall;
	
	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		if(lastCall == null) { lastCall = new Rect(); }
		lastCall.set(left, top, right, bottom);
		super.setPadding(left, top, right, bottom);
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
		super.setPadding(lastCall.left, lastCall.top, lastCall.right, lastCall.bottom);
	}

}
