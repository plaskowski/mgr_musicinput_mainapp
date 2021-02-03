package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.HorizontalScrollView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.LazyScrolling;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.OnInterceptTouchObservable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.DetectTouchInterceptionStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.LazyScrollToStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class HorizontalScrollView_ObserveInterceptTouch_LazyScrolling extends HorizontalScrollView_WithMixin
        implements OnInterceptTouchObservable, LazyScrolling {

    public HorizontalScrollView_ObserveInterceptTouch_LazyScrolling(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public HorizontalScrollView_ObserveInterceptTouch_LazyScrolling(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new LazyScrollToStrategy(
                new DetectTouchInterceptionStrategy(
                        new ViewGroupStrategyChainRoot(new Internals())));
    }

    @Override
    public void setListener(OnInterceptListener listener) {
        mixin.extractNature(DetectTouchInterceptionStrategy.class)
                .setListener(listener);
    }

    @Override
    public void postLayoutScrollTo(int x, int y) {
        mixin.extractNature(LazyScrollToStrategy.class).postLayoutScrollTo(x, y);
    }

}
