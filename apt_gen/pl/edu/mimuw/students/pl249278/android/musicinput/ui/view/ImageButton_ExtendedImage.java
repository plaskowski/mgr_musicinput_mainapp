package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;


import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ImageButton_ExtendedImage extends
		ImageButton_ExtendedImage_internals.ExtendedImageStrategy {
	public ImageButton_ExtendedImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}

interface ImageButton_ExtendedImage_internals {
	/**
	 * @GeneratedAt Wed Feb 22 11:57:39 CET 2012
	 * @GeneratedFrom pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedImageStrategy
	 */
	class ExtendedImageStrategy extends android.widget.ImageButton {
		public ExtendedImageStrategy(Context context, AttributeSet attrs) {
			super(context, attrs);
			ExtendedResourcesFactory.loadExtendedImage(this, context, attrs);
		}
	}
}