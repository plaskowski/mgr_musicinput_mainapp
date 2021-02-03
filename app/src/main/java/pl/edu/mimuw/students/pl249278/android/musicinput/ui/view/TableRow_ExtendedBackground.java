package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.TableRow_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ExtendedBackgroundStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class TableRow_ExtendedBackground extends TableRow_WithMixin {

    public TableRow_ExtendedBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    private ExtendedBackgroundStrategy createMixin() {
        return new ExtendedBackgroundStrategy(new ViewGroupStrategyChainRoot(new Internals()));
    }
}
