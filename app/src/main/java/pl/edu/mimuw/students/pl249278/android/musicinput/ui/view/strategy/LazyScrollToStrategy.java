package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.LazyScrolling;

public class LazyScrollToStrategy extends ViewGroupStrategyBase {

	public LazyScrollToStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(LazyScrolling.class);
	}

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall) {
		super.onLayout(changed, l, t, r, b, superCall);
		if(postLayoutScroll) {
			postLayoutScroll = false;
			internals().viewObject().scrollTo(scrollToX, scrollToY);
		}
	}

	private int scrollToX, scrollToY;
	private boolean postLayoutScroll = false;

	public void postLayoutScrollTo(int x, int y) {
		postLayoutScroll = true;
		scrollToX = x;
		scrollToY = y;
	}


}
