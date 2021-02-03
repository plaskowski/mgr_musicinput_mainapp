package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.mimuw.students.pl249278.android.musicinput.StaticConfigurationError;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.StaveHighlighter;

public class StaveHighlightStrategy extends ViewGroupStrategyBase {
	private static final int LINESPACE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINESPACE5_ABSINDEX = NoteConstants.anchorIndex(5, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINESPACEM1_ABSINDEX = NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE);
	private static final int LINESPACEM2_ABSINDEX = NoteConstants.anchorIndex(-2, NoteConstants.ANCHOR_TYPE_LINESPACE);
	
	SheetVisualParams params;
	private Integer highlightedAnchor = null;
	private GradientDrawable downwardDrawable, upwardDrawable;
	private Sheet5LinesView staveView;

	public StaveHighlightStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ViewGroup.class);
		checkThatViewImplements(StaveHighlighter.class);
	}

	@Override
	public void onFinishInflate(OnFinishInflateSuperCall superCall) {
		super.onFinishInflate(superCall);
		int childCount = getChildCount();
		for(int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if(child instanceof Sheet5LinesView) {
				staveView = (Sheet5LinesView) child;
				break;
			}
		}
		if(staveView == null)
			throw new StaticConfigurationError(getClass().getSimpleName()+" does not contain stave view as a child");
	}
	
	public void setParams(SheetVisualParams params) {
		this.params = params;
		internals().viewObject().invalidate();
	}
	
	public void setHiglightColor(int color) {
		int whiteTrans = Color.argb(0, 255, 255, 255);
		int[] colors = new int[] { color, whiteTrans, whiteTrans, whiteTrans };
		downwardDrawable = new GradientDrawable(Orientation.TOP_BOTTOM, colors);
		upwardDrawable = new GradientDrawable(Orientation.BOTTOM_TOP, colors);
	}
	
	/**
	 * @param anchorAbsIndex null to turn off previous highlight
	 */
	public void highlightAnchor(Integer anchorAbsIndex) {
		this.highlightedAnchor = anchorAbsIndex;
		internals().viewObject().invalidate();
	}
	
	@Override
	public void onDraw(Canvas canvas, OnDrawSuperCall superCall) {
		super.onDraw(canvas, superCall);
		if(highlightedAnchor == null || params == null)
			return;
		
		int highlightedAnchor = this.highlightedAnchor;
		boolean isLine = NoteConstants.anchorType(highlightedAnchor) != NoteConstants.ANCHOR_TYPE_LINESPACE;
		int firstDownwardShade = Math.min(highlightedAnchor + (isLine ? 1 : 0), LINESPACE5_ABSINDEX);
		int lastDownwardShade = Math.max(highlightedAnchor + (isLine ? 1 : 0), LINESPACEM1_ABSINDEX);
		int firstUpwardShade = Math.min(highlightedAnchor + (isLine ? -1 : 0), LINESPACE4_ABSINDEX);
		int lastUpwardShade = Math.max(highlightedAnchor + (isLine ? -1 : 0), LINESPACEM2_ABSINDEX);
		drawDrawableOnLinespaces(canvas, downwardDrawable, firstDownwardShade, lastDownwardShade);
		drawDrawableOnLinespaces(canvas, upwardDrawable, firstUpwardShade, lastUpwardShade);
	}

	private void drawDrawableOnLinespaces(Canvas canvas, Drawable drawable, int firstLinespaceAbsIndex,
			int lastLinespaceAbsIndex) {
		int line0topY = staveView.getTop() + staveView.getPaddingTop();
		for(int anhor = firstLinespaceAbsIndex; anhor <= lastLinespaceAbsIndex; anhor +=2) {
			drawable.setBounds(
				0, 
				line0topY + params.anchorOffset(anhor, AnchorPart.TOP_EDGE),
				internals().viewObject().getWidth(),
				line0topY + params.anchorOffset(anhor, AnchorPart.BOTTOM_EDGE)
			);
			drawable.draw(canvas);
		}
	}
}
