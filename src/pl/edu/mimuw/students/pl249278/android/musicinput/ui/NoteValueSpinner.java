
package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class NoteValueSpinner extends ScrollView {
	private Paint PAINT_NORMAL = new Paint();
	private Paint PAINT_SELECTED = new Paint(); 
	private static final int EFFECT_PADDING = 5;
	
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);

	public NoteValueSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public NoteValueSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public NoteValueSpinner(Context context) {
		super(context);
		init(context);
	}
	
	private ViewGroup notesContainer;
	
	// TODO externalize this params
	private int minNoteValue;
	private int currentValue = 0;
	
	private void init(Context ctx) {
		int normalColor = Color.WHITE;
		PAINT_NORMAL.setColor(normalColor);
		PAINT_NORMAL.setShadowLayer(EFFECT_PADDING, 0, 0, normalColor);
		PAINT_NORMAL.setAntiAlias(true);
		int selectionColor = ctx.getResources().getColor(R.color.highlightColor);
		PAINT_SELECTED.setColor(selectionColor);
		PAINT_SELECTED.setShadowLayer(EFFECT_PADDING*2, 0, 0, selectionColor);
		PAINT_SELECTED.setAntiAlias(true);
		
		minNoteValue = ctx.getResources().getInteger(R.integer.defaultMinNoteValue);
	}
	
	public void setupNoteViews() throws NoteDescriptionLoadingException {
        notesContainer = (ViewGroup) findViewById(R.id.EDIT_note_value_container);
        notesContainer.removeAllViews();
        params = new SheetParams(10, 100);
        params.setScale(1);
        maxNoteHorizontalHalfWidth = 0;
        for (int i = 0; i <= minNoteValue; i++) {
			NoteView noteView = new NoteView(getContext());
			noteView.setNoteSpec(getContext(), i, LINE4_ABSINDEX);
			noteView.setPaint(PAINT_NORMAL, EFFECT_PADDING);
			noteView.setSheetParams(params);
			maxNoteHorizontalHalfWidth = Math.max(maxNoteHorizontalHalfWidth, Math.max(
				noteView.getBaseMiddleX(),
				noteView.measureWidth()-noteView.getBaseMiddleX()
			));
			notesContainer.addView(noteView, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
        
        if(currentValue >= 0 && currentValue <= minNoteValue) {
        	setIsSelected(currentValue, true);
        }
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
        // align notes on scrollbar
        int visibleRectHeight = h, availableWidth = w - notesContainer.getPaddingLeft()-notesContainer.getPaddingRight();
        int distanceBetweenNotesBases = (int) (visibleRectHeight*0.4);
        
        int horizontalSpaceLeft = visibleRectHeight/2;
        NoteView current = null;
        // calculate scale so that any "half" of any note will fit in half of available width
        params.setScale((availableWidth/2)/((float) maxNoteHorizontalHalfWidth));
        LinearLayout.LayoutParams params = null, templateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for(int i = 0; i <= minNoteValue; i++) {
        	current = (NoteView) notesContainer.getChildAt(i);
        	current.setSheetParams(this.params);
			params = new LinearLayout.LayoutParams(templateParams);
    		params.leftMargin = availableWidth/2-current.getBaseMiddleX();
    		int verticalAlignLine = verticalAlignLine(current);
			params.topMargin = horizontalSpaceLeft - verticalAlignLine;
        	current.setLayoutParams(params);
        	horizontalSpaceLeft = distanceBetweenNotesBases-(current.measureHeight()-verticalAlignLine);
        }
        // bottomMargin for last
		params.bottomMargin = visibleRectHeight/2 - (current.measureHeight()-verticalAlignLine(current));
        current.setLayoutParams(params);
	}

	private int verticalAlignLine(NoteView noteView) {
		return noteView.measureHeight()/2;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(t == oldt) return;
		boolean down = t > oldt;
		
		// find which of notes base is nearest center of ScrollView
        int cH = this.getHeight();
        int prevDist = notesContainer.getHeight();
        int newNoteHeight = currentValue;
        for(int i = currentValue; i >= 0 && i <= minNoteValue; i += down ? 1 : -1) {
        	NoteView current = (NoteView) notesContainer.getChildAt(i);
        	int dist = Math.abs(t+cH/2-(current.getTop()+verticalAlignLine(current)));
        	if(dist > prevDist) break;
        	newNoteHeight = i;
        	prevDist = dist;
        }
        if(newNoteHeight == currentValue) return;
        setIsSelected(currentValue, false);
//        info("Note change: %d -> %d", currentNoteLength, newNoteHeight);
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
	 * - from left edge of View to baseMiddleX
	 * - from baseMiddleX to right edge of View
	 * with sheetParams.scale = 1
	 */
	private int maxNoteHorizontalHalfWidth = 0;

	public static interface OnValueChanged<ValueType> {
		public void onValueChanged(ValueType newValue, ValueType oldValue);
	}
	
	private void setIsSelected(int value, boolean isSelected) {
		((NoteView) notesContainer.getChildAt(value)).setPaint(
			isSelected ? PAINT_SELECTED : PAINT_NORMAL, EFFECT_PADDING
		);
	}

	public void setOnValueChangedListener(
			OnValueChanged<Integer> onValueChangedListener) {
		this.onValueChangedListener = onValueChangedListener;
	}
	
	
}