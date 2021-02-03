package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged.OnScrollChangedListener;

public class InterceptOnScrollChangedStrategy extends ViewGroupStrategyBase {

	private OnScrollChangedListener listener = null;

	public InterceptOnScrollChangedStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(InterceptableOnScrollChanged.class);
	}

	@Override
	public void onScrollChanged(int l, int t, int oldl, int oldt, OnScrollChangedSuperCall superCall) {
		super.onScrollChanged(l, t, oldl, oldt, superCall);
		if(listener != null) {
			listener.onScrollChanged(l, oldl);
		}
	}
	
	public void setListener(OnScrollChangedListener listener) {
		this.listener = listener;
	}

}
