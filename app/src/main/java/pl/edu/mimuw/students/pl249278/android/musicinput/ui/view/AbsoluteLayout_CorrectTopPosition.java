package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.AbsoluteLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.CorrectTopPositionStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class AbsoluteLayout_CorrectTopPosition extends AbsoluteLayout_WithMixin {

    public AbsoluteLayout_CorrectTopPosition(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public AbsoluteLayout_CorrectTopPosition(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new CorrectTopPositionStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

}
