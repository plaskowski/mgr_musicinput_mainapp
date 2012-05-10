
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class VerticalNoteValueSpinner extends NoteValueSpinner_Vertical {
	private int mMaxHeight = Integer.MAX_VALUE;

	public VerticalNoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}

	public VerticalNoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(ExtendedResourcesFactory.styleResolver(context, attrs));
	}
	
	private void init(StyleResolver resolver) {
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.NoteValueSpinner);
		values = resolver.obtainStyledAttributes(R.styleable.CustomizableView);
		mMaxHeight = values.getDimensionPixelSize(R.styleable.CustomizableView_maxHeight, mMaxHeight);
		values.recycle();
	}
	
	@Override
	public void setupNoteViews(SheetParams globalParams)
			throws CreationException {
		super.setupNoteViews(globalParams);
        maxNoteHorizontalHalfWidth = 0;
        for (int i = 0; i <= minNoteValue; i++) {
			SheetAlignedElementView noteView = (SheetAlignedElementView) notesContainer.getChildAt(i);
			maxNoteHorizontalHalfWidth = Math.max(maxNoteHorizontalHalfWidth, Math.max(
				middleX(noteView),
				noteView.measureWidth()-middleX(noteView)
			));
		}
	}
	
	@Override
	protected void layoutViews() {
        // align notes on scrollbar
        int visibleRectHeight = getHeight(), availableWidth = getWidth() - notesContainer.getPaddingLeft()-notesContainer.getPaddingRight();
        int distanceBetweenNotesHeads = (int) (visibleRectHeight*0.4);
        
        int horizontalSpaceLeft = visibleRectHeight/2;
        SheetAlignedElementView current = null;
        // calculate scale so that any "half" of any note will fit in half of available width
        params.setScale((availableWidth/2)/((float) maxNoteHorizontalHalfWidth));
        LinearLayout.LayoutParams params = null, templateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i <= minNoteValue; i++) {
        	current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	current.setSheetParams(this.params);
			params = new LinearLayout.LayoutParams(templateParams);
    		params.leftMargin = availableWidth/2-middleX(current);
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
	
	@Override
	protected void scrollToCurrent() {
        // position according to current value
        View currentView = notesContainer.getChildAt(getCurrentValue());
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
        int prevDist = notesContainer.getHeight();
        int newNoteHeight = getCurrentValue();
        for(int i = getCurrentValue(); i >= 0 && i <= minNoteValue; i += down ? 1 : -1) {
        	SheetAlignedElementView current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	int dist = Math.abs(t+cH/2-(current.getTop()+verticalAlignLine(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == getCurrentValue()) return;
//        info("NoteStemAndFlag change: %d -> %d", currentNoteLength, newNoteHeight);
        changeValue(newNoteHeight);
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