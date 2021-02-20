
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.PagedHorizontalScrollView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.NoteValueWidget;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class HorizontalNoteValueSpinner extends PagedHorizontalScrollView_WithMixin<NoteValueSpinner>
		implements NoteValueWidget {
	/**
	 * Max note height when sheetParams.scale = 1
	 */
	private int maxNoteHeight = 0;

	public HorizontalNoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
	}

	public HorizontalNoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMixin(createMixin(), new ViewInflationContext(context, attrs));
	}

	private NoteValueSpinner createMixin() {
		return new NoteValueSpinner(new ViewGroupStrategyChainRoot(new Internals())) {
			@Override
			protected void layoutViews() {
				HorizontalNoteValueSpinner.this.layoutViews();
			}
			@Override
			protected void scrollToCurrent() {
				HorizontalNoteValueSpinner.this.scrollToCurrent();
			}
		};
	}

	@Override
	public void setOnValueChangedListener(OnValueChanged<Integer> onValueChangedListener) {
		mixin.setOnValueChangedListener(onValueChangedListener);
	}

	@Override
	public int getCurrentValue() {
		return mixin.getCurrentValue();
	}

	@Override
	public void setupNoteViews(SheetParams globalParams, int initialCurrentValue) throws CreationException {
		mixin.setupNoteViews(globalParams, initialCurrentValue);
		pageRatio = mixin.getItemSpacing();
        maxNoteHeight = 0;
        for (int i = 0; i <= mixin.getMinNoteValue(); i++) {
			SheetAlignedElementView noteView = (SheetAlignedElementView) mixin.getNotesContainer().getChildAt(i);
			maxNoteHeight = Math.max(maxNoteHeight, noteView.measureHeight());
		}
	}

	/* {@link NoteValueSpinner} callback */
	private void layoutViews() {
		int w = getWidth();
		int h = getHeight();
        // align notes on scrollbar
		ViewGroup notesContainer = mixin.getNotesContainer();
		int availableHeight = h - notesContainer.getPaddingTop()-notesContainer.getPaddingBottom();
        int distanceBetweenMiddles = (int) (w * mixin.getItemSpacing());
        int verticalSpaceLeft = w/2;
        SheetAlignedElementView current = null;
        // calculate scale so that all notes fits vertically
        mixin.getParams().setScale(availableHeight/((float) maxNoteHeight));
        LinearLayout.LayoutParams params = null, templateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i <= mixin.getMinNoteValue(); i++) {
        	current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	current.setSheetParams(mixin.getParams());
			params = new LinearLayout.LayoutParams(templateParams);
			params.topMargin = availableHeight/2 - current.measureHeight()/2;
			params.leftMargin = Math.max(0, verticalSpaceLeft - mixin.middleX(current));
        	current.setLayoutParams(params);
        	verticalSpaceLeft = distanceBetweenMiddles - (current.measureWidth()-mixin.middleX(current));
        }
        // bottomMargin for last
        params.rightMargin = Math.max(0, w/2 - (current.measureWidth()-mixin.middleX(current)));
        current.setLayoutParams(params);
	}

	/* {@link NoteValueSpinner} callback */
	private void scrollToCurrent() {
        View currentView = mixin.getNotesContainer().getChildAt(getCurrentValue());
        scrollTo(
          currentView.getLeft()
          + mixin.middleX((SheetAlignedElementView) currentView)
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
        int prevDist = mixin.getNotesContainer().getWidth();
        int newNoteHeight = getCurrentValue();
        for(int i = getCurrentValue(); i >= 0 && i <= mixin.getMinNoteValue(); i += next ? 1 : -1) {
        	SheetAlignedElementView current = (SheetAlignedElementView) mixin.getNotesContainer().getChildAt(i);
        	int dist = Math.abs(centerAbsX - (current.getLeft() + mixin.middleX(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == getCurrentValue()) return;
        mixin.changeValue(newNoteHeight);
	}	
}