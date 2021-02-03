package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.view.ImageView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedImageStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class ImageView_ExtendedImage extends ImageView_WithMixin {

    public ImageView_ExtendedImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public ImageView_ExtendedImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new ExtendedImageStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

}
