package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/** a stub class, lets a strategy call super.onLayout() */
class DummyViewGroup extends ViewGroup {

	public DummyViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		throw new RuntimeException("Should not be reached");
	}

	public DummyViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		throw new RuntimeException("Should not be reached");
	}

	public DummyViewGroup(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		throw new RuntimeException("Should not be reached");
	}
	
}