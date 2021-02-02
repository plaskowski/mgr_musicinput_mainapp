package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;

public class ExtendedBackgroundStrategy extends android.view.View {

	public ExtendedBackgroundStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		ExtendedResourcesFactory.loadExtendedBackground(this, context, attrs);
	}
	
}
