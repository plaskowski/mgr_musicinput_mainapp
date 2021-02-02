package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class InterceptOnScrollChangedStrategy extends View implements InterceptableOnScrollChanged {
	
	public InterceptOnScrollChangedStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(listener != null) {
			listener.onScrollChanged(l, oldl);
		}
	}
	
	private OnScrollChangedListener listener = null;

	public void setListener(OnScrollChangedListener listener) {
		this.listener = listener;
	}
}
