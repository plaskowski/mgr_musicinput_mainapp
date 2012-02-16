package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;

public class ExtendedView extends android.view.View {

	public ExtendedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ExtendedResourcesFactory.loadExtendedBackground(this, context, attrs);
	}
	
}
