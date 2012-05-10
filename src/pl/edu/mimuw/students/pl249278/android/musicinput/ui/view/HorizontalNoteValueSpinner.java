
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class HorizontalNoteValueSpinner extends NoteValueSpinner_Horizontal {
	/**
	 * Max note height when sheetParams.scale = 1
	 */
	private int maxNoteHeight = 0;

	public HorizontalNoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HorizontalNoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void setupNoteViews(SheetParams globalParams)
			throws CreationException {
		super.setupNoteViews(globalParams);
        maxNoteHeight = 0;
        for (int i = 0; i <= minNoteValue; i++) {
			SheetAlignedElementView noteView = (SheetAlignedElementView) notesContainer.getChildAt(i);
			maxNoteHeight = Math.max(maxNoteHeight, noteView.measureHeight());
		}
	}

	@Override
	protected void layoutViews() {
		int w = getWidth();
		int h = getHeight();
        // align notes on scrollbar
        int availableHeight = h - notesContainer.getPaddingTop()-notesContainer.getPaddingBottom();
        int distanceBetweenMiddles = (int) (w*itemSpacing);
        int verticalSpaceLeft = w/2;
        SheetAlignedElementView current = null;
        // calculate scale so that all notes fits vertically
        params.setScale(availableHeight/((float) maxNoteHeight));
        LinearLayout.LayoutParams params = null, templateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i <= minNoteValue; i++) {
        	current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	current.setSheetParams(this.params);
			params = new LinearLayout.LayoutParams(templateParams);
			params.topMargin = availableHeight/2 - current.measureHeight()/2;
			params.leftMargin = Math.max(0, verticalSpaceLeft - middleX(current));
        	current.setLayoutParams(params);
        	verticalSpaceLeft = distanceBetweenMiddles - (current.measureWidth()-middleX(current));
        }
        // bottomMargin for last
        params.rightMargin = Math.max(0, w/2 - (current.measureWidth()-middleX(current)));
        current.setLayoutParams(params);
	}
	
	@Override
	protected void scrollToCurrent() {
        View currentView = notesContainer.getChildAt(getCurrentValue());
        scrollTo(
          currentView.getLeft()
          + middleX((SheetAlignedElementView) currentView)
          - getWidth()/2, 
          0
        );
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(l == oldl) return;
		boolean next = l > oldl;
		// find which of notes middle is nearest center of ScrollView
        int centerAbsX = l + this.getWidth()/2;
        int prevDist = notesContainer.getWidth();
        int newNoteHeight = getCurrentValue();
        for(int i = getCurrentValue(); i >= 0 && i <= minNoteValue; i += next ? 1 : -1) {
        	SheetAlignedElementView current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	int dist = Math.abs(centerAbsX - (current.getLeft() + middleX(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == getCurrentValue()) return;
        changeValue(newNoteHeight);
	}	
}