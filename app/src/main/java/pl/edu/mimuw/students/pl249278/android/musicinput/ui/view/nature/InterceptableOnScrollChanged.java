package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

public interface InterceptableOnScrollChanged {
	
	public static interface OnScrollChangedListener {
		void onScrollChanged(int l, int oldl);
	}

	public void setListener(OnScrollChangedListener listener);
}
