package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ExtendedImageStrategy extends ImageView {

	public ExtendedImageStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		ExtendedResourcesFactory.loadExtendedImage(this, context, attrs);
	}
	
}
