package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

public interface OnInterceptTouchObservable {
	public interface OnInterceptListener {
		/**
		 * Called when observed ViewGroup "decided" to intercept a MotionEvent stream
		 * (by returning true from ViewGroup.onInterceptTouchEvent())
		 */
		public void onTouchIntercepted();
	}
	
	public void setListener(OnInterceptListener listener);
}
