
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.mixin.viewgroup.PagedScrollView_WithMixin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.NoteValueWidget;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy.ViewGroupStrategyChainRoot;

public class VerticalNoteValueSpinner extends PagedScrollView_WithMixin<NoteValueSpinner> implements NoteValueWidget {
	private int mMaxHeight = Integer.MAX_VALUE;

	public VerticalNoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initMixin(createMixin(), new ViewInflationContext(context, attrs, defStyle));
		init(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}

	public VerticalNoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMixin(createMixin(), new ViewInflationContext(context, attrs));
		init(ExtendedResourcesFactory.styleResolver(context, attrs));
	}

	private NoteValueSpinner createMixin() {
		return new NoteValueSpinner(new ViewGroupStrategyChainRoot(new Internals())) {
			@Override
			protected void layoutViews() {
				VerticalNoteValueSpinner.this.layoutViews();
			}
			@Override
			protected void scrollToCurrent() {
				VerticalNoteValueSpinner.this.scrollToCurrent();
			}
		};
	}

	private void init(StyleResolver resolver) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.NoteValueSpinner);
		values = resolver.obtainStyledAttributes(R.styleable.CustomizableView);
		mMaxHeight = values.getDimensionPixelSize(R.styleable.CustomizableView_maxHeight, mMaxHeight);
		values.recycle();
		pageRatio = mixin.getItemSpacing();
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
	public void setupNoteViews(SheetParams globalParams)
			throws CreationException {
		mixin.setupNoteViews(globalParams);
        maxNoteHorizontalHalfWidth = 0;
        for (int i = 0; i <= mixin.getMinNoteValue(); i++) {
			SheetAlignedElementView noteView = (SheetAlignedElementView) mixin.getNotesContainer().getChildAt(i);
			maxNoteHorizontalHalfWidth = Math.max(maxNoteHorizontalHalfWidth, Math.max(
				mixin.middleX(noteView),
				noteView.measureWidth() - mixin.middleX(noteView)
			));
		}
	}

	@Override
	public void setupNoteViews(SheetParams globalParams, int initialCurrentValue) throws CreationException {
		mixin.setupNoteViews(globalParams, initialCurrentValue);
	}

	/** {@link NoteValueSpinner} callback */
	private void layoutViews() {
        // align notes on scrollbar
		ViewGroup notesContainer = mixin.getNotesContainer();
		int visibleRectHeight = getHeight(), availableWidth = getWidth() - notesContainer.getPaddingLeft()- notesContainer.getPaddingRight();
        int distanceBetweenNotesHeads = (int) (visibleRectHeight * mixin.getItemSpacing());
        
        int horizontalSpaceLeft = visibleRectHeight/2;
        SheetAlignedElementView current = null;
        // calculate scale so that any "half" of any note will fit in half of available width
        mixin.getParams().setScale((availableWidth/2)/((float) maxNoteHorizontalHalfWidth));
        LinearLayout.LayoutParams params = null, templateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i <= mixin.getMinNoteValue(); i++) {
        	current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	current.setSheetParams(mixin.getParams());
			params = new LinearLayout.LayoutParams(templateParams);
    		params.leftMargin = availableWidth/2-mixin.middleX(current);
    		int verticalAlignLine = verticalAlignLine(current);
			params.topMargin = Math.max(0, horizontalSpaceLeft - verticalAlignLine);
        	current.setLayoutParams(params);
        	horizontalSpaceLeft = distanceBetweenNotesHeads-(current.measureHeight()-verticalAlignLine);
        }
        // bottomMargin for last
		params.bottomMargin = Math.max(0, visibleRectHeight/2 - (current.measureHeight()-verticalAlignLine(current)));
        current.setLayoutParams(params);
	}

	private int verticalAlignLine(SheetAlignedElementView noteView) {
		return noteView.measureHeight()/2;
	}

	/** {@link NoteValueSpinner} callback */
	private void scrollToCurrent() {
        // position according to current value
		View currentView = mixin.getNotesContainer().getChildAt(getCurrentValue());
        scrollTo(0, 
          currentView.getTop()
		  + verticalAlignLine((SheetAlignedElementView) currentView)
		  - getHeight()/2);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(t == oldt) return;
		boolean down = t > oldt;

		// find which of notes head is nearest center of ScrollView
        int cH = this.getHeight();
        int prevDist = mixin.getNotesContainer().getHeight();
        int newNoteHeight = getCurrentValue();
        for(int i = getCurrentValue(); i >= 0 && i <= mixin.getMinNoteValue(); i += down ? 1 : -1) {
        	SheetAlignedElementView current = (SheetAlignedElementView) mixin.getNotesContainer().getChildAt(i);
        	int dist = Math.abs(t+cH/2-(current.getTop()+verticalAlignLine(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == getCurrentValue()) return;
//        info("NoteStemAndFlag change: %d -> %d", currentNoteLength, newNoteHeight);
        mixin.changeValue(newNoteHeight);
	}

	/**
	 * Max value from widths of notes horizontal parts:
	 * - from left edge of View to headMiddleX
	 * - from headMiddleX to right edge of View
	 * with sheetParams.scale = 1
	 */
	private int maxNoteHorizontalHalfWidth = 0;
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		switch(MeasureSpec.getMode(heightMeasureSpec)) {
		case MeasureSpec.EXACTLY:
		case MeasureSpec.AT_MOST:
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(
				MeasureSpec.getMode(heightMeasureSpec),
				Math.min(mMaxHeight, MeasureSpec.getSize(heightMeasureSpec))
			);
			break;
		case MeasureSpec.UNSPECIFIED:
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(
				MeasureSpec.AT_MOST,
				mMaxHeight
			);
			break;
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}