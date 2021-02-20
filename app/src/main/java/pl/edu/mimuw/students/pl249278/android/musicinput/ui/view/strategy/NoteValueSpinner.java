
package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.content.res.TypedArray;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

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
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.NoteValueWidget;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.NoteValueWidget.OnValueChanged;

import static android.view.View.VISIBLE;

public abstract class NoteValueSpinner extends ViewGroupStrategyBase
		implements ViewGroupStrategy {
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	public static final int INITIAL_VALUE_UNDEFINED = -1;
	
	private Paint itemPaint = new Paint();
	private Paint itemSelectedPaint = new Paint();
	private int maxPaintRadius = 0;
	private float itemSpacing;
	private ViewGroup notesContainer;
	private int minNoteValue;
	private int currentValue = 0;	
	private OnValueChanged<Integer> onValueChangedListener = null;
	private SheetParams params;
	/**
	 * If we need to scroll to currently selected element in next {@link View#onLayout(boolean, int, int, int, int)} pass.
	 */
	private boolean scrollInOnLayout = false;

	public NoteValueSpinner(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ViewGroup.class);
		checkThatViewImplements(NoteValueWidget.class);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);

		StyleResolver resolver = ExtendedResourcesFactory.styleResolver(viewInflationContext);
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
			itemSpacing = values.getFloat(R.styleable.NoteValueSpinner_spacing, 0.5f);
		} finally {
			values.recycle();
		}
	}

	public void setupNoteViews(SheetParams globalParams, int initialCurrentValue) throws CreationException {
		setupNoteViews(globalParams);
		if (initialCurrentValue != INITIAL_VALUE_UNDEFINED) {
			currentValue = Math.min(initialCurrentValue, minNoteValue);
		}
	}
	
	/** Setup views, set scale to 1 */
	private void setupNoteViews(SheetParams globalParams) throws CreationException {
		if(getChildCount() == 1) {
			notesContainer = (ViewGroup) getChildAt(0);
		} else {
			throw new RuntimeException("Must contain exactly 1 child");
		}
        notesContainer.removeAllViews();
        params = new SheetParams(globalParams);
        params.setScale(1);
        for (int i = 0; i <= minNoteValue; i++) {
			SheetAlignedElement model = DrawingModelFactory.createDrawingModel(getContext(), 
				new ElementSpec.NormalNote(new NoteSpec(i, LINE4_ABSINDEX), NoteConstants.ORIENT_UP)
			);
			SheetAlignedElementView noteView = new SheetAlignedElementView(getContext(), model);
			noteView.setSheetParams(params);
			noteView.setPaint(itemPaint, maxPaintRadius);
			notesContainer.addView(noteView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}
	
	public int middleX(SheetAlignedElementView noteView) {
		return noteView.getPaddingLeft()+noteView.model().getMiddleX();
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh, OnSizeChangedSuperCall superCall) {
		super.onSizeChanged(w, h, oldw, oldh, superCall);
		// post outside layout pass so changes in layout parameters will take effect
		internals().viewObject().post(new Runnable() {
			@Override
			public void run() {
				layoutViews();
				scrollInOnLayout = true;
			}
		});
		getChildAt(0).setVisibility(View.INVISIBLE);
	}
	
	/** layout note views, called when size of scrolling widget is known, outside of layout pass */
	protected abstract void layoutViews();

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b, OnLayoutSuperCall superCall) {
		super.onLayout(changed, l, t, r, b, superCall);
		if(changed) {
        	setIsSelected(currentValue, true);
		}
		if(scrollInOnLayout) {
			scrollInOnLayout = false;
			getChildAt(0).setVisibility(VISIBLE);
			scrollToCurrent();
		}
	}
	
	/** scroll this container so that currently selected value will be centered */ 
	protected abstract void scrollToCurrent();

	public void changeValue(int newNoteHeight) {
        setIsSelected(currentValue, false);
		int oldValue = currentValue;
        currentValue = newNoteHeight;
        setIsSelected(currentValue, true);
        if(onValueChangedListener != null) {
        	onValueChangedListener.onValueChanged(currentValue, oldValue);
        }
	}
	
	private void setIsSelected(int value, boolean isSelected) {
		((SheetAlignedElementView) notesContainer.getChildAt(value)).setPaint(
			isSelected ? itemSelectedPaint : itemPaint, maxPaintRadius
		);
	}

	public void setOnValueChangedListener(
			OnValueChanged<Integer> onValueChangedListener) {
		this.onValueChangedListener = onValueChangedListener;
	}

	public int getCurrentValue() {
		return currentValue;
	}

	public float getItemSpacing() {
		return itemSpacing;
	}

	public int getMinNoteValue() {
		return minNoteValue;
	}

	public ViewGroup getNotesContainer() {
		return notesContainer;
	}

	public SheetParams getParams() {
		return params;
	}

}