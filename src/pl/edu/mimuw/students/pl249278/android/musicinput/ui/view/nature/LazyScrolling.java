package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

public interface LazyScrolling {
	/** Order scrollTo(0, scrollToY) just after this.onLayout pass */
	void postLayoutScrollTo(int x, int y);
}
