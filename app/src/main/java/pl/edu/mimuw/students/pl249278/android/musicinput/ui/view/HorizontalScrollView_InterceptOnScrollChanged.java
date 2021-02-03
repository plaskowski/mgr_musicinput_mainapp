package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.HorizontalScrollView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.InterceptOnScrollChangedStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class HorizontalScrollView_InterceptOnScrollChanged extends HorizontalScrollView_WithMixin
        implements InterceptableOnScrollChanged {

    public HorizontalScrollView_InterceptOnScrollChanged(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public HorizontalScrollView_InterceptOnScrollChanged(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new InterceptOnScrollChangedStrategy(new ViewGroupStrategyChainRoot(new Internals()));
    }

    @Override
    public void setListener(OnScrollChangedListener listener) {
        mixin.extractNature(InterceptOnScrollChangedStrategy.class)
                .setListener(listener);
    }
}
