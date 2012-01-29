
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class NoteValueSpinner extends ScrollView {
	private Paint itemPaint = new Paint();
	private Paint itemSelectedPaint = new Paint();
	private int maxPaintRadius = 0;
	
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);

	public NoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}

	public NoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(ExtendedResourcesFactory.styleResolver(context, attrs));
	}
	
	private ViewGroup notesContainer;
	
	private int minNoteValue;
	private int currentValue = 0;
	
	private void init(StyleResolver resolver) {
		minNoteValue = resolver.getResources().getInteger(R.integer.spinnerDefaultMinNoteValue);
		currentValue = minNoteValue/2;
		
		// initialize paints according to style attributes
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.NoteValueSpinner);
		try {
			int styleId;
			styleId = values.getResourceId(R.styleable.NoteValueSpinner_itemPaint, -1);
			if(styleId != -1) {
				PaintSetup setup = ExtendedResourcesFactory.createPaintSetup(resolver, styleId);
				maxPaintRadius = (int) Math.ceil(setup.drawRadius);
				itemPaint = setup.paint;
			}
			styleId = values.getResourceId(R.styleable.NoteValueSpinner_selectedItemPaint, -1);
			if(styleId != -1) {
				PaintSetup setup = ExtendedResourcesFactory.createPaintSetup(resolver, styleId);
				maxPaintRadius = (int) Math.max(maxPaintRadius, Math.ceil(setup.drawRadius));
				itemSelectedPaint = setup.paint;
			}
		} finally {
			values.recycle();
		}
	}
	
	public void setupNoteViews(SheetParams globalParams) throws CreationException {
		if(getChildCount() == 1) {
			notesContainer = (ViewGroup) getChildAt(0);
		} else {
			throw new RuntimeException("Must contain exactly 1 child");
		}
        notesContainer.removeAllViews();
        params = new SheetParams(globalParams);
        params.setScale(1);
        maxNoteHorizontalHalfWidth = 0;
        for (int i = 0; i <= minNoteValue; i++) {
			SheetAlignedElementView noteView = new SheetAlignedElementView(getContext());
			noteView.setModel(DrawingModelFactory.createDrawingModel(getContext(), 
				new ElementSpec.NormalNote(new NoteSpec(i, LINE4_ABSINDEX), NoteConstants.ORIENT_UP)
			));
			noteView.setSheetParams(params);
			noteView.setPaint(itemPaint);
			noteView.setPadding(maxPaintRadius);
			maxNoteHorizontalHalfWidth = Math.max(maxNoteHorizontalHalfWidth, Math.max(
				middleX(noteView),
				noteView.measureWidth()-middleX(noteView)
			));
			notesContainer.addView(noteView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
        
        if(currentValue >= 0 && currentValue <= minNoteValue) {
        	setIsSelected(currentValue, true);
        }
	}
	
	private static int middleX(SheetAlignedElementView noteView) {
		return noteView.getPaddingLeft()+noteView.model().getMiddleX();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
        // align notes on scrollbar
        int visibleRectHeight = h, availableWidth = w - notesContainer.getPaddingLeft()-notesContainer.getPaddingRight();
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
			params.topMargin = horizontalSpaceLeft - verticalAlignLine;
        	current.setLayoutParams(params);
        	horizontalSpaceLeft = distanceBetweenNotesHeads-(current.measureHeight()-verticalAlignLine);
        }
        // bottomMargin for last
		params.bottomMargin = visibleRectHeight/2 - (current.measureHeight()-verticalAlignLine(current));
        current.setLayoutParams(params);
	}

	private int verticalAlignLine(SheetAlignedElementView noteView) {
		return noteView.measureHeight()/2;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(changed) {
	        // position according to current value
	        View currentView = notesContainer.getChildAt(currentValue);
	        scrollTo(0, 
	          currentView.getTop()
			  + verticalAlignLine((SheetAlignedElementView) currentView)
			  - getHeight()/2);
		}
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(t == oldt) return;
		boolean down = t > oldt;
		
		// find which of notes head is nearest center of ScrollView
        int cH = this.getHeight();
        int prevDist = notesContainer.getHeight();
        int newNoteHeight = currentValue;
        for(int i = currentValue; i >= 0 && i <= minNoteValue; i += down ? 1 : -1) {
        	SheetAlignedElementView current = (SheetAlignedElementView) notesContainer.getChildAt(i);
        	int dist = Math.abs(t+cH/2-(current.getTop()+verticalAlignLine(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == currentValue) return;
        setIsSelected(currentValue, false);
//        info("NoteStemAndFlag change: %d -> %d", currentNoteLength, newNoteHeight);
        int oldValue = currentValue;
        currentValue = newNoteHeight;
        setIsSelected(currentValue, true);
        if(onValueChangedListener != null) {
        	onValueChangedListener.onValueChanged(currentValue, oldValue);
        }
	}
	
	private OnValueChanged<Integer> onValueChangedListener = null;
	private SheetParams params;
	/**
	 * Max value from widths of notes horizontal parts:
	 * - from left edge of View to headMiddleX
	 * - from headMiddleX to right edge of View
	 * with sheetParams.scale = 1
	 */
	private int maxNoteHorizontalHalfWidth = 0;

	public static interface OnValueChanged<ValueType> {
		public void onValueChanged(ValueType newValue, ValueType oldValue);
	}
	
	private void setIsSelected(int value, boolean isSelected) {
		((SheetAlignedElementView) notesContainer.getChildAt(value)).setPaint(
			isSelected ? itemSelectedPaint : itemPaint
		);
	}

	public void setOnValueChangedListener(
			OnValueChanged<Integer> onValueChangedListener) {
		this.onValueChangedListener = onValueChangedListener;
	}

	public int getCurrentValue() {
		return currentValue;
	}
	
	
}