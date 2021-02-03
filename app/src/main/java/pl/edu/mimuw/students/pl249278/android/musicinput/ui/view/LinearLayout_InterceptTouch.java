package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.LinearLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableTouch;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptTouchEventStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class LinearLayout_InterceptTouch extends LinearLayout_WithMixin
        implements InterceptableTouch {

    public LinearLayout_InterceptTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public LinearLayout_InterceptTouch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new InterceptTouchEventStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

    @Override
    public void setInterceptTouchDelegate(InterceptTouchDelegate interceptTouchDelegate) {
        mixin.extractNature(InterceptTouchEventStrategy.class)
                .setInterceptTouchDelegate(interceptTouchDelegate);
    }
}
