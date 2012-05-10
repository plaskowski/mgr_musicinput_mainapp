package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.LazyScrolling;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class LazyScrollToStrategy extends View implements LazyScrolling {
	
	public LazyScrollToStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public LazyScrollToStrategy(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public LazyScrollToStrategy(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(postLayoutScroll) {
			postLayoutScroll = false;
			scrollTo(scrollToX, scrollToY);
		}
	}
	
	private int scrollToX, scrollToY;
	private boolean postLayoutScroll = false;

	@Override
	public void postLayoutScrollTo(int x, int y) {
		postLayoutScroll = true;
		scrollToX = x;
		scrollToY = y;
	}


}
