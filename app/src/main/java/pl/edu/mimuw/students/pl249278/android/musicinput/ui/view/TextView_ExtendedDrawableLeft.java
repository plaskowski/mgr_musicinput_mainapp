package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.view.TextView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedDrawableLeftStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class TextView_ExtendedDrawableLeft extends TextView_WithMixin {

    public TextView_ExtendedDrawableLeft(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public TextView_ExtendedDrawableLeft(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new ExtendedDrawableLeftStrategy(new ViewGroupStrategyChainRoot(new Internals()));
    }
}
