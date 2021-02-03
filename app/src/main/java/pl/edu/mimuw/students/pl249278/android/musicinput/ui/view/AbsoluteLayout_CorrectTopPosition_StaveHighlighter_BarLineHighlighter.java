package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.AbsoluteLayout_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.BarLineHighlighter;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.StaveHighlighter;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.BarLineHighlightStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.CorrectTopPositionStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.StaveHighlightStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class AbsoluteLayout_CorrectTopPosition_StaveHighlighter_BarLineHighlighter extends AbsoluteLayout_WithMixin
        implements BarLineHighlighter, StaveHighlighter {

    public AbsoluteLayout_CorrectTopPosition_StaveHighlighter_BarLineHighlighter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initMixin(createMixin(), new ViewInflationContext(context, attrs));
    }

    public AbsoluteLayout_CorrectTopPosition_StaveHighlighter_BarLineHighlighter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
    }

    private ViewGroupStrategy createMixin() {
        return new BarLineHighlightStrategy(
                new StaveHighlightStrategy(
                    new CorrectTopPositionStrategy(
                        new ViewGroupStrategyChainRoot(new Internals()))));
    }

    @Override
    public void setHighlightedBar(SheetAlignedElementView highlightedBar) {
        mixin.extractNature(BarLineHighlightStrategy.class)
                .setHighlightedBar(highlightedBar);
    }

    @Override
    public void setParams(SheetVisualParams params) {
        mixin.extractNature(StaveHighlightStrategy.class)
                .setParams(params);
    }

    @Override
    public void setHiglightColor(int color) {
        mixin.extractNature(StaveHighlightStrategy.class)
                .setHiglightColor(color);
    }

    @Override
    public void highlightAnchor(Integer anchorAbsIndex) {
        mixin.extractNature(StaveHighlightStrategy.class)
                .highlightAnchor(anchorAbsIndex);
    }
}
