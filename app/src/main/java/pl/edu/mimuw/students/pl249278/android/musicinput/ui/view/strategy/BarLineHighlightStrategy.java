package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.TimeDivider;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.BarLineHighlighter;

public class BarLineHighlightStrategy extends ViewGroupStrategyBase {
	
	private SheetAlignedElementView highlightedBar;
	private Drawable leftDrawable, rightDrawable;

	public BarLineHighlightStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(BarLineHighlighter.class);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);
		int color = viewInflationContext.context.getResources().getColor(R.color.highlightColor);
		int whiteTrans = Color.argb(0, 255, 255, 255);
		int[] colors = new int[] { color, whiteTrans, whiteTrans, whiteTrans };
		leftDrawable = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
		rightDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, colors);
	}

	public void setHighlightedBar(SheetAlignedElementView highlightedBar) {
		this.highlightedBar = highlightedBar;
		internals().viewObject().invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas, OnDrawSuperCall superCall) {
		super.onDraw(canvas, superCall);
		if(this.highlightedBar == null)
			return;
		SheetAlignedElement model = this.highlightedBar.model();
		SheetVisualParams params = model.getSheetParams();
		if(params == null)
			return;
		int leftEdge = model.getHorizontalOffset(TimeDivider.HLINE_TIMEBAR_LEFTEDGE);
		int rightEdge = model.getHorizontalOffset(TimeDivider.HLINE_TIMEBAR_RIGHTEDGE);
		int spacing = rightEdge - leftEdge;
		int shadowWidth = params.getLinespacingThickness();
		int xStart = highlightedBar.getLeft() + highlightedBar.getPaddingLeft() + leftEdge;
		leftDrawable.setBounds(xStart - shadowWidth, 0, xStart, internals().viewObject().getHeight());
		leftDrawable.draw(canvas);
		xStart += spacing;
		rightDrawable.setBounds(xStart, 0, xStart + shadowWidth, internals().viewObject().getHeight());
		rightDrawable.draw(canvas);
	}

}
