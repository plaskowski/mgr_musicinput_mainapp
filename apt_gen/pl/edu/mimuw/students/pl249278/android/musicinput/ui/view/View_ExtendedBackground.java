package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;

public class View_ExtendedBackground extends
		View_ExtendedBackground_internals.ExtendedBackgroundStrategy {
	public View_ExtendedBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}

interface View_ExtendedBackground_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedBackgroundStrategy
	 */
	class ExtendedBackgroundStrategy extends android.view.View {
		public ExtendedBackgroundStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
			ExtendedResourcesFactory.loadExtendedBackground(this, context,
					attrs);
		}
	}
}