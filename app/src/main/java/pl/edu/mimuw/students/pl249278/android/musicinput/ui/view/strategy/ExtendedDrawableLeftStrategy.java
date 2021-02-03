package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class ExtendedDrawableLeftStrategy extends ViewGroupStrategyBase {

    public ExtendedDrawableLeftStrategy(ViewGroupStrategy parent) {
        super(parent);
    }

    @Override
    public void initStrategy(ViewInflationContext viewInflationContext) {
        super.initStrategy(viewInflationContext);
        Drawable dr = ExtendedResourcesFactory.inflateExtendedDrawable(
                viewInflationContext,
                R.styleable.ExtendedTextDrawable,
                R.styleable.ExtendedTextDrawable_extendedDrawableLeft
        );
        if (dr != null) {
            TextView textView = (TextView) internals().viewObject();
            Drawable[] drs = textView.getCompoundDrawables();
            textView.setCompoundDrawablesWithIntrinsicBounds(dr, drs[1], drs[2], drs[3]);
        }
    }

}
