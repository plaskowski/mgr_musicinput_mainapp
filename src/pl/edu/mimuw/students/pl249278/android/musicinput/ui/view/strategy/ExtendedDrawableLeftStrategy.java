package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

public class ExtendedDrawableLeftStrategy extends TextView {
	
	public ExtendedDrawableLeftStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		Drawable dr = ExtendedResourcesFactory.inflateExtendedDrawable(
			context, attrs,
			R.styleable.ExtendedTextDrawable,
			R.styleable.ExtendedTextDrawable_extendedDrawableLeft
		);
		if(dr != null) {
			Drawable[] drs = this.getCompoundDrawables();
			this.setCompoundDrawablesWithIntrinsicBounds(dr, drs[1], drs[2], drs[3]);
		}
	}

}
