package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.view.View_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedBackgroundStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class View_ExtendedBackground extends View_WithMixin {

    public View_ExtendedBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public View_ExtendedBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new ExtendedBackgroundStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

}
