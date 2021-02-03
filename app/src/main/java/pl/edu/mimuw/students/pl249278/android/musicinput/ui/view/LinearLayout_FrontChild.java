package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.LinearLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.DrawingChildOnTop;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.BringToFrontStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class LinearLayout_FrontChild extends LinearLayout_WithMixin
        implements DrawingChildOnTop {

    public LinearLayout_FrontChild(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public LinearLayout_FrontChild(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new BringToFrontStrategy(
                new ViewGroupStrategyChainRoot(new Internals()));
    }

    @Override
    public void setFrontChildView(View frontChildView) {
        mixin.extractNature(BringToFrontStrategy.class)
                .setFrontChildView(frontChildView);
    }
}
