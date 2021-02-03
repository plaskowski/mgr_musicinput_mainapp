package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.FrameLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptScaleGestureStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class FrameLayout_InterceptScale extends FrameLayout_WithMixin
        implements InterceptsScaleGesture {

    public FrameLayout_InterceptScale(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public FrameLayout_InterceptScale(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new InterceptScaleGestureStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

    @Override
    public void setOnScaleListener(OnScaleListener onScaleListener) {
        mixin.extractNature(InterceptScaleGestureStrategy.class)
                .setOnScaleListener(onScaleListener);
    }
}
