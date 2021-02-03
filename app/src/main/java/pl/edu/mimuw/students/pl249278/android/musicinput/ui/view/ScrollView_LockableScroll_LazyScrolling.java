package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.ScrollView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.LazyScrolling;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LazyScrollToStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LockScrollingStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class ScrollView_LockableScroll_LazyScrolling extends ScrollView_WithMixin
        implements ScrollingLockable, LazyScrolling {

    public ScrollView_LockableScroll_LazyScrolling(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public ScrollView_LockableScroll_LazyScrolling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new LazyScrollToStrategy(
                new LockScrollingStrategy(
                        new ViewGroupStrategyChainRoot(new Internals())));
    }

    @Override
    public void postLayoutScrollTo(int x, int y) {
        mixin.extractNature(LazyScrollToStrategy.class)
                .postLayoutScrollTo(x, y);
    }

    @Override
    public void setScrollingLocked(boolean scrollingLocked) {
        mixin.extractNature(LockScrollingStrategy.class)
                .setScrollingLocked(scrollingLocked);
    }
}
