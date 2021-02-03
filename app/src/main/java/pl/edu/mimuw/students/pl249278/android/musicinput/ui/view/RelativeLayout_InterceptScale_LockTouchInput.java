package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.RelativeLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.TouchInputLockable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptScaleGestureStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LockTouchInputStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class RelativeLayout_InterceptScale_LockTouchInput extends RelativeLayout_WithMixin
        implements InterceptsScaleGesture, TouchInputLockable {

    public RelativeLayout_InterceptScale_LockTouchInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public RelativeLayout_InterceptScale_LockTouchInput(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new LockTouchInputStrategy(
                new InterceptScaleGestureStrategy(
                        new ViewGroupStrategyChainRoot(new Internals())));
    }

    @Override
    public void setOnScaleListener(OnScaleListener onScaleListener) {
        mixin.extractNature(InterceptScaleGestureStrategy.class)
                .setOnScaleListener(onScaleListener);
    }

    @Override
    public void setTouchInputLocked(boolean setLocked) {
        mixin.extractNature(LockTouchInputStrategy.class)
                .setTouchInputLocked(setLocked);
    }
}
