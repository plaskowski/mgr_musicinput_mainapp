package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.TimeDivider;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.BarLineHighlighter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;

public class BarLineHighlightStrategy extends DummyViewGroup implements BarLineHighlighter {
	
	private SheetAlignedElementView highlightedBar;
	private Drawable leftDrawable, rightDrawable;
	
	public BarLineHighlightStrategy(Context context, AttributeSet attrs) {
		super(context, attrs);
		int color = context.getResources().getColor(R.color.highlightColor);
		int whiteTrans = Color.argb(0, 255, 255, 255);
		int[] colors = new int[] { color, whiteTrans, whiteTrans, whiteTrans };
		leftDrawable = new GradientDrawable(Orientation.RIGHT_LEFT, colors);
		rightDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, colors);	}
	
	@Override
	public void setHighlightedBar(SheetAlignedElementView highlightedBar) {
		this.highlightedBar = highlightedBar;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
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
		leftDrawable.setBounds(xStart - shadowWidth, 0, xStart, getHeight());
		leftDrawable.draw(canvas);
		xStart += spacing;
		rightDrawable.setBounds(xStart, 0, xStart + shadowWidth, getHeight());
		rightDrawable.draw(canvas);
	}
}
