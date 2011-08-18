package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE4_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.TOP_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.CompoundTouchListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ModifiedScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner.OnValueChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.OutlineDrawable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor.OnScaleListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay.Observer;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.JoinArc;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup.GroupBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.SheetElementView;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

public class EditActivity extends Activity {
	protected static final int SPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);

	private static final int ANIM_TIME = 150;
	protected Paint noteHighlightPaint = new Paint();
	private int NOTE_DRAW_PADDING = 0;
	private int MIN_DRAW_SPACING;
	protected static final Paint normalPaint = new Paint();
	{
		normalPaint.setAntiAlias(true);
		noteHighlightPaint.setAntiAlias(true);
	}

	private static LogUtils log = new LogUtils(EditActivity.class);
	
	private Sheet5LinesView lines;
	private SheetParams sheetParams;
	private ViewGroup sheet;
	private ArrayList<Time> times = new ArrayList<EditActivity.Time>();
	private ArrayList<SheetAlignedElementView> elementViews = new ArrayList<SheetAlignedElementView>();
	private ArrayList<SheetElementView<SheetElement>> overlaysViews = new ArrayList<SheetElementView<SheetElement>>();
	private int inputAreaWidth;
	private View inputArea;
	private HorizontalScrollView hscroll;
	private ModifiedScrollView vertscroll;
	private ScaleGestureInterceptor scaleGestureDetector;
	private Animator animator = new EditActivity.Animator(this);
	
	/**
	 * Index (in elementViews) of element that is first on right side of InputArea,
	 * when there is no such element rightToIA = elementViews.size()
	 */
	private int rightToIA;
	private int iaRightMargin;
	private int delta;
	private int mTouchSlop;
	protected boolean isScaling = false, isPositioning = false;
	private int visibleRectWidth;
	private int visibleRectHeight;
	private int notesAreaX;
	private int line0Top;

	protected int currentNoteLength = 0;
	
	private float noteMinDistToIA;
	private float defaultSpacingBaseFactor;
	private float minDrawSpacingFactor;
	private int minPossibleValue;
	private float afterTimeDividerVisualSpacingFactor;
	
	/** takt muzyki */
	private class Time {
		/** index of left divider (TimeDivider class) in elementViews */ 
		private int rangeStart;
		/** current (to sheetParams) spacing after whole note */
		int spacingBase = -1;
		private TimeSpec spec;
		private SheetAlignedElementView view;
		
		public Time(TimeSpec spec) {
			super();
			this.spec = spec;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscreen);
		
		// TODO sheetParams comes from previous view
		sheetParams = new SheetParams(
			getResources().getInteger(R.integer.lineThickness),
			getResources().getInteger(R.integer.linespaceThickness)
		);
		sheetParams.setTimeStep(new TimeSpec.TimeStep(3, 2));
		sheetParams.setClef(NoteConstants.Clef.VIOLIN);
		sheetParams.setKeySignature(KeySignature.B_DUR);
		sheetParams.setMinSpaceAnchor(getResources().getInteger(R.integer.minSpaceDefault));
		sheetParams.setMaxSpaceAnchor(getResources().getInteger(R.integer.maxSpaceDefault));
		
		scaleGestureDetector = (ScaleGestureInterceptor) findViewById(R.id.EDIT_scale_detector);
		hscroll = (HorizontalScrollView) findViewById(R.id.EDIT_outer_hscrollview);
		vertscroll = (ModifiedScrollView) findViewById(R.id.EDIT_vertscrollview);
		sheet = (ViewGroup) findViewById(R.id.EDIT_sheet_container);
		lines = (Sheet5LinesView) findViewById(R.id.EDIT_sheet_5lines);
		int hColor = getResources().getColor(R.color.highlightColor);
		lines.setHiglightColor(hColor);
		noteHighlightPaint.setColor(hColor);
		scaleGestureDetector.setOnScaleListener(scaleListener);
		sheet.setOnTouchListener(new CompoundTouchListener(
			iaTouchListener,
			noteTouchListener
		));
		// setup noteValue spinner
		NoteValueSpinner valueSpinner = (NoteValueSpinner) findViewById(R.id.EDIT_note_value_scroll);
		try {
			valueSpinner.setupNoteViews();
		} catch (CreationException e) {
			e.printStackTrace();
			finish();
			return;
		}
		valueSpinner.setOnValueChangedListener(new OnValueChanged<Integer>() {
			@Override
			public void onValueChanged(Integer newValue, Integer oldValue) {
				currentNoteLength = newValue;
				popupCurrentNoteLength();
			}
		});
		
		// setup info popup
		SheetAlignedElementView popupIcon = (SheetAlignedElementView) findViewById(R.id.EDIT_info_popup_note);
		Paint iconPaint = new Paint();
		iconPaint.setColor(getResources().getColor(R.color.infoPopupIconColor));
		iconPaint.setAntiAlias(true);
		popupIcon.setPaint(iconPaint);
		Paint ipopupBgPaint = new Paint();
		ipopupBgPaint.setStyle(Style.FILL);
		ipopupBgPaint.setColor(getResources().getColor(R.color.infoPopupBgColor));
		ipopupBgPaint.setAntiAlias(true);
		int ipopupShadow = getResources().getDimensionPixelSize(R.dimen.infoPoupBgShadow);
		ipopupBgPaint.setShadowLayer(ipopupShadow, ipopupShadow/3, ipopupShadow/3, Color.BLACK);
		ipopupBgPaint.setPathEffect(new CornerPathEffect(getResources().getDimensionPixelSize(R.dimen.infoPoupBgCorner)));
		OutlineDrawable ipopupBg = new OutlineDrawable();
		ipopupBg.addPaint(ipopupBgPaint, ipopupShadow*2);
		findViewById(R.id.EDIT_info_popup).setBackgroundDrawable(ipopupBg);
		
		this.inputArea = findViewById(R.id.EDIT_inputArea);
		this.inputAreaWidth = getResources().getDimensionPixelSize(R.dimen.inputAreaWidth);
		ViewConfiguration configuration = ViewConfiguration.get(this);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        Paint iaPaint = new Paint();
        iaPaint.setColor(getResources().getColor(R.color.iaFrameColor));
        int iaSWidth = getResources().getDimensionPixelSize(R.dimen.iaFrameStrokeWidth);
		iaPaint.setStyle(Style.STROKE);
		iaPaint.setStrokeWidth(iaSWidth);
		int iaSShadow = iaSWidth/2;
		iaPaint.setShadowLayer(iaSShadow, iaSShadow, iaSShadow, Color.BLACK);
		iaPaint.setPathEffect(new DashPathEffect(new float[] {4*iaSWidth, 2*iaSWidth, 6*iaSWidth, 2*iaSWidth}, 0));
        OutlineDrawable outlineDrawable = new OutlineDrawable();
        outlineDrawable.addPaint(iaPaint, iaSWidth);
		inputArea.setBackgroundDrawable(outlineDrawable);
		
		noteShadow = readParametrizedFactor(R.string.noteShadow);
		noteMinDistToIA = readParametrizedFactor(R.string.minDistToIA);
		defaultSpacingBaseFactor = readParametrizedFactor(R.string.defaultTimeSpacingBaseFactor);
		minPossibleValue = getResources().getInteger(R.integer.minNotePossibleValue) + 1;
		minDrawSpacingFactor = readParametrizedFactor(R.string.minDrawSpacing);
		afterTimeDividerVisualSpacingFactor = readParametrizedFactor(R.string.timeDividerDrawAfterSpacingFactor);
		
		// create elements
		ArrayList<ElementSpec> rawNotesSequence = new ArrayList<ElementSpec>();
		NoteSpec n;

//		n = new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasDot(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		rawNotesSequence.add(new ElementSpec.NormalNote(n4));
		
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.SHARP);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE));
////		n.setHasDot(true);
//		n.setToneModifier(NoteModifier.SHARP);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.SHARP);
////		n.setHasDot(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setIsGrouped(true);
//		n.setHasDot(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setIsGrouped(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(6, NoteConstants.ANCHOR_TYPE_LINESPACE));
////		n.setIsGrouped(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setIsGrouped(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+2, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setIsGrouped(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(5, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setIsGrouped(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
		n.setHasJoinArc(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
		n.setToneModifier(NoteModifier.FLAT);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.FLAT);
//		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasJoinArc(true);
//		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.FLAT);
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setHasJoinArc(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setHasJoinArc(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		try {
			for(ElementSpec spec: rawNotesSequence) {
				addElementView(elementViews.size(), createDrawingModel(spec));
			}
			// build times
			rebuildTimes(0);
			// rebuild overlays
			int size = elementViews.size();
			buildNoteGroups(0, size-1);
			buildJoinArcs(0, size-1);
		} catch (CreationException e) {
			e.printStackTrace();
			finish();
			return;
		}
		
		((InterceptedHorizontalScrollView) hscroll).setListener(horizontalScrollListener);
		
		rightToIA = elementViews.size();
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				hscroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				LogUtils.info("onGlobalLayout() >> HSCROLL %dx%d", hscroll.getWidth(), hscroll.getHeight());
				onContainerResize(hscroll.getWidth(), hscroll.getHeight());
			}
		};
		hscroll.getViewTreeObserver().addOnGlobalLayoutListener(listener);
	}
	
	private float readParametrizedFactor(int stringResId) {
		String rawValue = getResources().getString(stringResId);
		float factor = Float.parseFloat(rawValue.substring(0, rawValue.length()-1));
		char c = rawValue.charAt(rawValue.length()-1);
		if(c == 'l') {
			return factor*sheetParams.getLineFactor();
		} else if(c == 's') {
			return factor*sheetParams.getLinespacingFactor();
		}
		throw new UnsupportedOperationException();
	}
	
	// TODO co jak nie ma ustalonego metrum?
	// TODO co jak zmienia się metrum? nie powienienem przepychać nuty do istniejących taktów tylko stworzyć nowy takt starego metrum
	
	private void rebuildTimes(int startTimeIndex) throws CreationException {
//		log.i("rebuildTimes(%d)", startTimeIndex);
		
		if(sheetParams.getTimeStep() != null) {
			TimeSpec.TimeStep currentMetrum = sheetParams.getTimeStep();
			for(int ti = 0; ti <= startTimeIndex && ti < times.size(); ti++) {
				TimeStep ts = times.get(ti).spec.getTimeStep();
				if(ts != null) {
					currentMetrum = ts;
				}
			}
			int capLeft = 0;
			int i = startTimeIndex < times.size() ? times.get(startTimeIndex).rangeStart : 0;
			int timeIndex = startTimeIndex;
			int prevHandledTime = timeIndex-1;
			for(; i < elementViews.size();) {
				if(timeIndex > prevHandledTime) {
					Time currentTime = rebuildTime(timeIndex, i);
					if(currentTime.spec.getTimeStep() != null) {
						currentMetrum = currentTime.spec.getTimeStep();
					}
					prevHandledTime = timeIndex;
					capLeft = timeCapacity(currentMetrum, minPossibleValue);
					i++;
					continue;
				}
				SheetAlignedElementView view = elementViews.get(i);
				ElementSpec elementSpec = view.model().getElementSpec();
				if(elementSpec.getType() == ElementType.FAKE_PAUSE) {
					removeElementView(view);
					continue;
				} else if(elementSpec.getType() == ElementType.TIMES_DIVIDER) {
					elementViews.remove(i);
					continue;
				}
				int timeValue = elementSpec.timeValue(minPossibleValue);
				// FIXME problem when timeValue == 0
				if(timeValue <= capLeft) {
//					log.i("rebuildTimes(): element at %d of timeValue %d will shrink %d cap of time[%d]", i, timeValue, capLeft, timeIndex); 
					capLeft -= timeValue;
					if(capLeft == 0) {
//						log.i("rebuildTimes(): and forced it's end", i, timeValue, timeIndex); 
						timeIndex++;
					}
				} else {
					/** create fake pause with given capLeft */
//					log.i("rebuildTimes(): element at %d of timeValue %d didnt fit to %d cap of time[%d] so forced fake pause and end", i, timeValue, capLeft, timeIndex); 
					addElementView(i, createDrawingModel(new ElementSpec.FakePause(capLeft, minPossibleValue)));
					timeIndex++;
				}
				i++;
			}
			if(timeIndex > prevHandledTime) {
				rebuildTime(timeIndex, i);
			}
			// usuwam nadmiarowe (względem nowego wyliczenia) obiekty Time
			int lastOldTime = times.size()-1;
			while(lastOldTime > timeIndex) {
				Time removedTime = times.remove(lastOldTime--);
				removeElementView(removedTime.view);
			}
		}
	}

	private Time rebuildTime(int timeIndex, int newRangeStart) throws CreationException {
		Time currentTime;
		if(timeIndex >= times.size()) {
			currentTime = new Time(timeIndex == 0 ? 
				new TimeSpec(sheetParams.getTimeStep(), sheetParams.getClef(), sheetParams.getKeySignature()) 
				: new TimeSpec()
			);
			currentTime.view = addElementView(newRangeStart, createDrawingModel(new ElementSpec.TimeDivider(
				timeIndex > 0 ? times.get(timeIndex-1).spec : null,
				currentTime.spec
			)));
			times.add(timeIndex, currentTime);
			updatePosition(currentTime.view, null, sheetElementY(currentTime.view));
		} else {
			currentTime = times.get(timeIndex); 
			elementViews.add(newRangeStart, currentTime.view);
		}
		currentTime.rangeStart = newRangeStart;
		return currentTime;
	}
	
	private void debugViews() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < elementViews.size(); i++) {
			SheetAlignedElementView view = elementViews.get(i);
			str.append(view.model().getElementSpec().getType().name()+", ");
		}
		log.i("elementViews(#%d) { "+str.toString()+" }", elementViews.size());
	}
	private void debugTimes() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < times.size(); i++) {
			str.append(times.get(i).rangeStart+", ");
		}
		log.i("times(#%d) { "+str.toString()+" }", times.size());
	}
	private void assertTimesValidity() {
		int timeIndex = -1;
		for(int i = 0; i < elementViews.size(); i++) {
			SheetAlignedElementView view = elementViews.get(i);
			ElementSpec spec = view.model().getElementSpec();
			if(spec.getType() == ElementType.TIMES_DIVIDER) {
				timeIndex++;
				Time currTime = times.get(timeIndex);
				if(currTime.rangeStart != i) {
					throw new RuntimeException(String.format(
						"elementViews[%d] of type TIME_DIVIDER doesn't match times[%d].rangeStart = %d",
						i, timeIndex, currTime.rangeStart
					));
				}
			} else if(spec.getType() == ElementType.FAKE_PAUSE) {
				if(elementViews.get(i+1).model().getElementSpec().getType() != ElementType.TIMES_DIVIDER) {
					throw new RuntimeException(String.format(
						"elementViews[%d] of type FAKE_PAUSE doesn't precede TIME_DIVIDER",
						i
					));
				}
			}
		}
		for(int i = 0; i < times.size(); i++) {
			int rangeStart = times.get(i).rangeStart;
			ElementSpec elementSpec = elementViews.get(rangeStart).model().getElementSpec();
			ElementType type = elementSpec.getType();
			if(type != ElementType.TIMES_DIVIDER) {
				throw new RuntimeException(String.format(
					"times[%d].rangeStart = %d points an element of type %s instead of TIME_DIVIDER",
					i, rangeStart, type.name() 
				));
			}
		}
	}

	/**
	 * build greedily NotesGroups starting from startIndex 
	 * @param endIndex maximal index of first element of NotesGroup
	 * @param instantRedraw do we want to reposition views immediately after changing their drawing model
	 */
	private void buildNoteGroups(int startIndex, int endIndex) throws CreationException {
		NotesGroup group = null;
		int totalSize = elementViews.size();
		for(int elementI = startIndex; elementI <= endIndex; elementI++) {
			SheetAlignedElementView view = elementViews.get(elementI);
			ElementSpec spec = view.model().getElementSpec();
			if(group == null && GroupBuilder.canStartGroup(spec)) {
				GroupBuilder gb = new GroupBuilder(sheetParams, spec);
				int groupEl = elementI+1;
				for(; groupEl < totalSize; groupEl++) {
					if(!gb.tryExtend(elementViews.get(groupEl).model().getElementSpec())) {
						break;
					}
				}
				if(gb.isValid()) {
					group = gb.build(normalPaint);
					addOverlayView(group);
					// extends endIndex so we reach all grouped elements
					int groupEndIndex = elementI + group.elementsCount() - 1;
					endIndex = Math.max(endIndex, groupEndIndex);
					log.i("buildNoteGroup(): %d -> %d", elementI, groupEndIndex);
				}
			} 
			if(group != null) {
				// recreate model because ElementSpec has been modified by GroupBuilder
				SheetAlignedElement model = createDrawingModel(spec);
				view.setModel(model);
				group.wrapNext(model);
				bind(group, view);
				model.setSheetParams(sheetParams);
				updatePosition(view, null, sheetElementY(view));
				if(!group.hasNext()) {
					 group = null;
				 }
			}
		}
	}
	
	/**
	 * Builds any JoinArc that starts at position from specified range 
	 * @param startIndex minimal index of JoinArc start element
	 * @param endIndex maximal index of JoinArc start element
	 */
	private void buildJoinArcs(int startIndex, int endIndex) throws CreationException {
		SheetAlignedElementView arcStart = null;
		int lastPossibleEl = elementViews.size()-1;
		// such index that would allow me to finish JoinArc that starts at (or skip over) endIndex position
		int extendedEndIndex = endIndex;
		for(int elementI = startIndex; elementI <= extendedEndIndex; elementI++) {
			SheetAlignedElementView view = elementViews.get(elementI);
			ElementSpec spec = view.model().getElementSpec();
			if(arcStart != null) {
				if(JoinArc.canEndJA(spec)) {
					JoinArc arc = new JoinArc(arcStart.model());
					arc.setRightElement(view.model());
					bind(arc, arcStart);
					bind(arc, view);
					addOverlayView(arc);
					log.i("buildJoinArc(): %d -> %d", elementViews.indexOf(arcStart), elementI);
					arc.positionChanged(arcStart.model(), left(arcStart), top(arcStart));
					arc.positionChanged(view.model(), left(view), top(view));
					arcStart = null;
				} else if(JoinArc.canSkipOver(spec)) {
					if(elementI == extendedEndIndex) {
						extendedEndIndex = Math.min(extendedEndIndex+1, lastPossibleEl);
					}
					continue;
				} else {
					arcStart = null;
				}
			}
			if(arcStart == null && elementI <= endIndex && JoinArc.canStrartJA(spec)) {
				arcStart = view;
				if(elementI == extendedEndIndex) {
					extendedEndIndex = Math.min(extendedEndIndex+1, lastPossibleEl);
				}
			} 
		}
	}
		
	private void clearJoinArcs(int startIndex, int endIndex) {
		clearOverlays(startIndex, endIndex, JoinArc.class, null);
	}
	private List<SheetAlignedElementView> undoList = new LinkedList<SheetAlignedElementView>();
	private void clearNoteGroups(int startIndex, int endIndex) throws CreationException {
		clearOverlays(startIndex, endIndex, NotesGroup.class, undoList);
		for(SheetAlignedElementView view: undoList) {
			ElementSpec elementSpec = view.model().getElementSpec();
			elementSpec.clear();
			view.setModel(createDrawingModel(elementSpec));
			view.setSheetParams(sheetParams);
			updatePosition(view, null, sheetElementY(view));
		}
		undoList.clear();
	}
	private void clearOverlays(int startIndex, int endIndex, Class<? extends ElementsOverlay> overlayClass, List<SheetAlignedElementView> modifiedElements) {
		for(int elementI = startIndex; elementI <= endIndex; elementI++) {
			SheetAlignedElementView view = elementViews.get(elementI);
			ElementsOverlay boundOverlay = getBoundOverlay(view, overlayClass);
			if(boundOverlay != null) {
				log.i(
					"clearOverlay(of class %s): %d -> %d",
					overlayClass.getSimpleName(), 
					elementViews.indexOf(boundOverlay.getElement(0).getTag()),
					elementViews.indexOf(boundOverlay.getElement(boundOverlay.elementsCount()-1).getTag())
				);
				unbind(boundOverlay, modifiedElements);
				SheetElementView<SheetElement> overlayView = (SheetElementView<SheetElement>) boundOverlay.getTag();
				overlaysViews.remove(overlayView);
				sheet.removeView(overlayView);
				boundOverlay.setTag(null);
			}
		}
	}	
	private void unbind(ElementsOverlay overlay, List<SheetAlignedElementView> modifiedElements) {
		for(int i = 0; i < overlay.elementsCount(); i++) {
			SheetAlignedElement drawingModel = overlay.getElement(i);
			SheetAlignedElementView elementView = (SheetAlignedElementView) drawingModel.getTag();
			Set<ElementsOverlay> set = bindMap.get(elementView);
			if(set != null) {
				if(set.remove(overlay) && modifiedElements != null) {
					modifiedElements.add(elementView);
				}
			}
		}
	}
	private ElementsOverlay getBoundOverlay(SheetAlignedElementView startElementView, Class<? extends ElementsOverlay> overlayClass) {
		Set<ElementsOverlay> set = bindMap.get(startElementView);
		if(set != null) for(ElementsOverlay overlay: set){
			if(overlayClass.isAssignableFrom(overlay.getClass()) && overlay.getElement(0) == startElementView.model()) {
				return overlay;
			}
		}
		return null;
	}

	private SheetAlignedElementView insertElement(int insertIndex, ElementSpec spec) throws CreationException {
		debugTimes();
		debugViews();
		assertTimesValidity();
		int currTime = insertTime(insertIndex);
		// wstawić element w miejsce insertIndex
		SheetAlignedElementView newElement = addElementView(insertIndex, createDrawingModel(spec));
		// przeliczyć czy nie zmieniła się struktura taktów
		rebuildTimes(currTime);
		debugTimes();
		assertTimesValidity();
		
		// find NotesGroup start that could be affected by inserted element
		int i = insertIndex - 1;
		for(;i > 0; i--) {
			ElementSpec elementSpec = elementViews.get(i).model().getElementSpec();
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int ngRebuildIndex = i;
		// find possible JoinArc that will be affected by rebuild of NotesGroup at ngRebuildIndex
		for(i = i-1; i > 0; i--) {
			ElementSpec elementSpec = elementViews.get(i).model().getElementSpec();
			if(JoinArc.canEndJA(elementSpec) || JoinArc.canStrartJA(elementSpec) || !JoinArc.canSkipOver(elementSpec)) {
				break;
			}
			                                                                        
		}
		int jaRebuildIndex = Math.max(i, 0);
		log.i("insertElement() jaRebuildIndex = %d, ngRebuildIndex = %d", jaRebuildIndex, ngRebuildIndex);
		int lastEl = elementViews.size() - 1;
		clearJoinArcs(jaRebuildIndex, lastEl);
		clearNoteGroups(ngRebuildIndex, lastEl);
		buildNoteGroups(ngRebuildIndex, lastEl);
		buildJoinArcs(jaRebuildIndex, lastEl);
		
		updatePositions(insertIndex+1, lastEl, hscroll.getScrollX()+visibleRectWidth-iaRightMargin+delta);
		
		debugViews();
		return newElement;
	}


	private int insertTime(int insertIndex) {
		int currTime = findTime(insertIndex);
		if(times.get(currTime).rangeStart == insertIndex) {
			// when insertIndex is index of TimeDivider we want to insert into left Time 
			currTime--;
		}
		return currTime;
	}
	
	private void updatePositions(int startIndex, int endIndex) {
		int x;
		x = positionAfter(startIndex-1);
		updatePositions(startIndex, endIndex, x);
	}

	private int positionAfter(int elementIndex) {
		int x;
		if(elementIndex >= 0) {
			SheetAlignedElementView prevEl = elementViews.get(elementIndex);
			x = middleAbsoluteX(prevEl) + afterElementSpacing(times.get(findTime(elementIndex)), prevEl.model());
		} else {
			x = notesAreaX;
		}
		return x;
	}
	private void updatePositions(int startIndex, int endIndex, int xstart) {
		int timeIndex = findTime(startIndex);
		if(times.get(timeIndex).rangeStart == startIndex) timeIndex--;
		for(int elementI = startIndex; elementI <= endIndex; elementI++) {
			SheetAlignedElementView v = elementViews.get(elementI);
			SheetAlignedElement model = v.model();
			updatePosition(v, xstart-middleX(v), null);
			if(model.getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
				timeIndex++;
			}
			xstart += afterElementSpacing(times.get(timeIndex), model);
		}
	}
	
	private void updateElementSpec(int elementIndex, ElementSpec newSpec, Point rebuildRange) throws CreationException {
		assertTimesValidity();
		SheetAlignedElementView view = elementViews.get(elementIndex);
		boolean timesRebuildRequired = view.model().getElementSpec().timeValue(minPossibleValue) != newSpec.timeValue(minPossibleValue);
		
		int ngRebuildIndex = elementIndex-1;
		for(;ngRebuildIndex > 0; ngRebuildIndex--) {
			ElementSpec elementSpec = elementViews.get(ngRebuildIndex).model().getElementSpec();
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int jaRebuildIndex = Math.max(ngRebuildIndex-1, 0);
		for(; jaRebuildIndex > 0; jaRebuildIndex--) {
			ElementSpec elementSpec = elementViews.get(jaRebuildIndex).model().getElementSpec();
			if(JoinArc.canEndJA(elementSpec) || JoinArc.canStrartJA(elementSpec) || !JoinArc.canSkipOver(elementSpec)) {
				break;
			}
			                                                                        
		}
		int ngRebuildEnd, jaRebuildEnd;
		int size = elementViews.size();
		if(timesRebuildRequired) {
			ngRebuildEnd = jaRebuildEnd = size-1;
		} else {
			for(ngRebuildEnd = elementIndex+1; ngRebuildEnd < size; ngRebuildEnd++) {
				ElementSpec elementSpec = elementViews.get(ngRebuildEnd).model().getElementSpec();
				if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
					break;
				}
			}
			jaRebuildEnd = ngRebuildEnd = Math.min(ngRebuildEnd, size-1);
		}
		
		log.i(
			"updateElementSpec(%d) rebuilds ja %d->%d ng %d->%d",
			elementIndex,
			jaRebuildIndex, jaRebuildEnd,
			ngRebuildIndex, ngRebuildEnd
		);
		clearJoinArcs(jaRebuildIndex, jaRebuildEnd);
		clearNoteGroups(ngRebuildIndex, ngRebuildEnd);
		view.setModel(createDrawingModel(newSpec));
		view.setSheetParams(sheetParams);
		if(timesRebuildRequired) {
			int currTime = findTime(elementIndex);
			rebuildTimes(currTime);
		}
		buildNoteGroups(ngRebuildIndex, ngRebuildEnd);
		buildJoinArcs(jaRebuildIndex, jaRebuildEnd);
		
		assertTimesValidity();
		rebuildRange.set(jaRebuildIndex, jaRebuildEnd);
	}
	
	private SheetAlignedElementView removeElement(int elementIndex) throws CreationException {
		debugTimes();
		debugViews();
		assertTimesValidity();
		
		int currTime = findTime(elementIndex);
		// wstawić element w miejsce insertIndex
		SheetAlignedElementView elView = elementViews.get(elementIndex);
		
		// find NotesGroup start that could be affected by removal
		int i = elementIndex - 1;
		for(;i > 0; i--) {
			ElementSpec elementSpec = elementViews.get(i).model().getElementSpec();
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int ngRebuildIndex = i;
		// find possible JoinArc that will be affected by rebuild of NotesGroup at ngRebuildIndex
		for(i = i-1; i > 0; i--) {
			ElementSpec elementSpec = elementViews.get(i).model().getElementSpec();
			if(JoinArc.canEndJA(elementSpec) || JoinArc.canStrartJA(elementSpec) || !JoinArc.canSkipOver(elementSpec)) {
				break;
			}
			                                                                        
		}
		int jaRebuildIndex = Math.max(i, 0);
		log.i("removeElement() jaRebuildIndex = %d, ngRebuildIndex = %d", jaRebuildIndex, ngRebuildIndex);
		
		// usunąć m.in. wszystkie Overlay-ie które operować na usuwanym elemencie
		int lastEl = elementViews.size() - 1;
		clearJoinArcs(jaRebuildIndex, lastEl);
		clearNoteGroups(ngRebuildIndex, lastEl);
		
		// usunąć widok elementu
		removeElementView(elView);
		// przeliczyć czy nie zmieniła się struktura taktów
		rebuildTimes(currTime);
		debugTimes();
		
		lastEl = elementViews.size() - 1;
		// odbudować Overlay-ie uwzględniając nowy układ (nieobecność usuniętego elementu i ew. przesunięcia pomiędzy taktami)
		buildNoteGroups(ngRebuildIndex, lastEl);
		buildJoinArcs(jaRebuildIndex, lastEl);
		
		debugViews();
		assertTimesValidity();
		return elView;
	}
	
	private View.OnTouchListener noteTouchListener = new OnTouchListener() {
		private static final int INVALID_POINTER = -1;
		private int activePointerId = INVALID_POINTER;
		
		Rect hitRect = new Rect();
		private int selectedIndex;
		private int touchYoffset;
		private int currentAnchor;
		private int absMiddleX;
		private Point temp = new Point();
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				int i = 0;
				for(; i < elementViews.size(); i++) {
					elementViews.get(i).getHitRect(hitRect);
					if(hitRect.contains((int) event.getX(), (int) event.getY())) {
						break;
					}
				}
				if(i >= elementViews.size() || elementViews.get(i).model().getElementSpec().getType() != ElementType.NOTE) {
					break;
				}
				this.selectedIndex = i;
				SheetAlignedElementView view = elementViews.get(selectedIndex);
				view.setPaint(noteHighlightPaint);
				vertscroll.setVerticalScrollingLocked(true);
				currentAnchor = view.model().getElementSpec().positonSpec().positon();
				lines.highlightAnchor(currentAnchor);
				touchYoffset = (int) event.getY() - line0Top - sheetParams.anchorOffset(currentAnchor, AnchorPart.MIDDLE);
				activePointerId = event.getPointerId(event.getActionIndex());
				absMiddleX = middleAbsoluteX(view);
				return true;
			case MotionEvent.ACTION_MOVE:
				if(activePointerId == INVALID_POINTER)
					break;
				int newAnchor = nearestAnchor((int) event.getY(event.findPointerIndex(activePointerId)) - touchYoffset);
				if(newAnchor != currentAnchor) {
					currentAnchor = newAnchor;
					lines.highlightAnchor(currentAnchor);
					try {
						SheetAlignedElementView elView = elementViews.get(selectedIndex);
						updateElementSpec(selectedIndex, new ElementSpec.NormalNote(new NoteSpec(((NormalNote) elView.model().getElementSpec()).noteSpec(), currentAnchor)), temp);
						updatePosition(elView, absMiddleX-middleX(elView), sheetElementY(elView));
					} catch (CreationException e) {
						e.printStackTrace();
						finish();
						return false;
					}
				}
				return true;
			case MotionEvent.ACTION_POINTER_1_UP:
				if(event.getPointerId(event.getActionIndex()) != activePointerId)
					break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				activePointerId = INVALID_POINTER;
				elementViews.get(selectedIndex).setPaint(normalPaint);
				vertscroll.setVerticalScrollingLocked(false);
				lines.highlightAnchor(null);
				return true;
			}
			return false;
		}
	};
	
	private View.OnTouchListener iaTouchListener = new OnTouchListener() {
		private static final int INVALID_POINTER = -1;
		private int activePointerId = INVALID_POINTER;
		private int currentAnchor;
		private int insertIndex;
		private int downPointerId = INVALID_POINTER;
		private Point downCoords = new Point();
		
		private Runnable lazyActionDown = new Runnable() {
			public void run() {
				if(downPointerId != INVALID_POINTER && insideIA(downCoords.x)) {
					insertIndex = rightToIA;
					currentAnchor = nearestAnchor(downCoords.y);
					ElementSpec.NormalNote newNoteSpec = new ElementSpec.NormalNote(new NoteSpec(currentNoteLength, currentAnchor));
					if(!willFitInTime(insertIndex, newNoteSpec)) {
						disrupt(R.string.EDIT_msg_inserterror_notetolong);
						downPointerId = INVALID_POINTER;
						return;
					}
					lines.highlightAnchor(currentAnchor);
					try {
						rightToIA++;
						activePointerId = downPointerId;
						downPointerId = INVALID_POINTER;
						SheetAlignedElementView newNote = insertElement(insertIndex, newNoteSpec);
						newNote.setPaint(noteHighlightPaint);
						updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
						vertscroll.setVerticalScrollingLocked(true);
					} catch (CreationException e) {
						e.printStackTrace();
						finish();
					}
				}
			}
		};
		
		Point temp = new Point();
		@Override
		public boolean onTouch(View v, MotionEvent event) {
//			log.i(
//				"sheet::onTouch(): %s activePointerId %d", 
//				ReflectionUtils.findConst(MotionEvent.class, "ACTION_", event.getActionMasked()),	
//				activePointerId
//			);
			switch(event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				if(insideIA((int) event.getX())) {
					downCoords.set((int) event.getX(), (int) event.getY());
					downPointerId = event.getPointerId(event.getActionIndex());
					sheet.postDelayed(lazyActionDown, 100);
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(activePointerId == INVALID_POINTER) {
					if(downPointerId != INVALID_POINTER) {
						downCoords.set((int) event.getX(), (int) event.getY());
						return true;
					}
					break;
				} else if(!insideIA((int) event.getX())) {
					break;
				}
				int newAnchor = nearestAnchor((int) event.getY());
				if(newAnchor != currentAnchor) {
					lines.highlightAnchor(newAnchor);
					try {
						updateElementSpec(insertIndex, new ElementSpec.NormalNote(new NoteSpec(currentNoteLength, newAnchor)), temp);
						updatePositions(temp.x, insertIndex-1);
						SheetAlignedElementView newNote = elementViews.get(insertIndex);
						updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
						updatePositions(insertIndex+1, temp.y, hscroll.getScrollX()+visibleRectWidth-iaRightMargin+delta);
					} catch (CreationException e) {
						e.printStackTrace();
						finish();
						return false;
					}
					currentAnchor = newAnchor;
				}
				return true;
			case MotionEvent.ACTION_POINTER_1_UP:
				if(event.getPointerId(event.getActionIndex()) != activePointerId) {
					return false;
				}
			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_UP:
				if(activePointerId == INVALID_POINTER)
					break;
				activePointerId = INVALID_POINTER;
				insertNoteAndClean();
				return true;
			}
			cancel();
			return false;
		}
		
		private boolean willFitInTime(int insertIndex, ElementSpec spec) {
			int timeIndex = insertTime(insertIndex);
			TimeStep currentTimeStep = getCurrentTimeStep(timeIndex);
			if(currentTimeStep == null) {
				return true;
			}
			int capLeft = timeCapacity(currentTimeStep, minPossibleValue);
			for(int i = times.get(timeIndex).rangeStart + 1; i < insertIndex; i++) {
				capLeft -= elementViews.get(i).model().getElementSpec().timeValue(minPossibleValue);
			}
			return spec.timeValue(minPossibleValue) <= capLeft;
		}

		private TimeStep getCurrentTimeStep(int timeIndex) {
			TimeStep result = null, curr;
			for(int i = 0; i <= timeIndex; i++) {
				if((curr = times.get(i).spec.getTimeStep()) != null) {
					result = curr;
				}
			}
			return result;
		}

		private void insertNoteAndClean() {
			animator.forceFinishAll();
			isPositioning = true;
			SheetAlignedElementView noteView = elementViews.get(insertIndex);
			noteView.setPaint(normalPaint);
			rightToIA = insertIndex+1;
			if(isStickyTimeDivider(rightToIA)) {
				rightToIA++;
			}
			
			int currentTimeIndex = findTime(insertIndex);
			for(int i = currentTimeIndex; i < times.size(); i++) {
				updateTimeSpacingBase(i, false);
			}
			Time currentTime = times.get(currentTimeIndex);
			int repositioningStart = currentTime.rangeStart+1;
			long duration = 500;
			WaitManyRunOnce animationsEndListener = new WaitManyRunOnce(1+elementViews.size()-repositioningStart) {
				@Override
				protected void allFinished() {
					isPositioning = false;
					scaleGestureDetector.setTouchInputLocked(false);
				}
			};
			SheetAlignedElementView timeDividerView = elementViews.get(currentTime.rangeStart);
			int x = middleAbsoluteX(timeDividerView) + afterElementSpacing(currentTime, timeDividerView.model());
			int timeIndex = currentTimeIndex;
			int rescrollDest = -1;
			for(int i = repositioningStart; i < elementViews.size(); i++) {
				if(i == rightToIA) {
					x += moveDistance();
				}
				SheetAlignedElementView v = elementViews.get(i);
				// animate view to it's correct position
				int dx = (x - middleX(v)) - left(v);
				animator.startRLAnimation(v, dx, duration, animationsEndListener);
				if(i == rightToIA-1) {
					rescrollDest = x;
				}
				if(v.model().getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
					timeIndex++;
				}
				x += afterElementSpacing(times.get(timeIndex), v.model());
			}
			if(rescrollDest == -1) {
				throw new RuntimeException();
			}
			
			correctSheetWidth();
			animator.startHScrollAnimation(
				hscroll, 
				(rescrollDest-hscroll.getScrollX())-((visibleRectWidth - inputAreaWidth - iaRightMargin - delta)), 
				duration, 
				animationsEndListener
			);
			
			lines.highlightAnchor(null);
			vertscroll.setVerticalScrollingLocked(false);
			scaleGestureDetector.setTouchInputLocked(true);
		}
		
		/**
		 * Clear all artifacts introduced by touch inside IA box
		 */
		private void cancel() {
			vertscroll.setVerticalScrollingLocked(false);
			lines.highlightAnchor(null);
			
			if(downPointerId != INVALID_POINTER) {
				sheet.removeCallbacks(lazyActionDown);
				log.i("iaTouchListener::cancel() insert reverted");
			} else if(activePointerId != INVALID_POINTER) {
				try {
					removeElement(insertIndex);
					// aktualizacja rightToIA
					rightToIA = insertIndex;
					// przywrócić właściwe pozycje
					updatePositions(
						rightToIA, 
						elementViews.size()-1,
						positionAfter(rightToIA-1) + moveDistance()
					);
				} catch (CreationException e) {
					e.printStackTrace();
					finish();
				}
			}
			activePointerId = INVALID_POINTER;
			downPointerId = INVALID_POINTER;
		}

		private int inIA_noteViewX(SheetAlignedElementView noteView) {
			return hscroll.getScrollX()+visibleRectWidth-iaRightMargin-inputAreaWidth/2-middleX(noteView);
		}

		/**
		 * @param x in sheet view coordinates
		 * @return if point (x,?) is inside input area box
		 */
		private boolean insideIA(int x) {
			int pos = x-hscroll.getScrollX();
			return pos >= visibleRectWidth-inputAreaWidth-iaRightMargin && pos <= visibleRectWidth-iaRightMargin;
		}

	};
	
	/**
	 * find which anchor from range <minSpaceAbsIndex, maxSpaceAbsIndex> is nearest to horizontal line y
	 * @param y in sheet view coordinates
	 * @return index of anchor
	 */
	private int nearestAnchor(int y) {
		int line0middle = sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.MIDDLE);
		int delta =
			sheetParams.anchorOffset(SPACE0_ABSINDEX, AnchorPart.MIDDLE)
			- line0middle;
		int indexDeltaHead = y - (line0Top + line0middle - delta/2);
		int indexDelta = indexDeltaHead/delta;
		return Math.max( 
			Math.min(
			LINE0_ABSINDEX + indexDelta + (indexDeltaHead < 0 ? -1 : 0),
			NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
			),
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
		);
	}
	
	private OnScrollChangedListener horizontalScrollListener = new OnScrollChangedListener() {
		@Override
		public void onScrollChanged(int l, int oldl) {
//			LogUtils.info("scrollChange (%d, %d)", l, oldl);
			if(isScaling || isPositioning) return;
			// skip if there is only TimeDivider of Time_0
			if(elementViews.size()==1) return;
			if(l < oldl && rightToIA-1 >= 1) {
				// move all elements that crossed "to the left of IA" border
				while(rightToIA-1 >= 1) {
					if(shouldBeMovedRight(rightToIA-1)) {
						rightToIA--;
						moveRight(rightToIA);
					} else {
						break;
					}
				}
				if(isStickyTimeDivider(rightToIA)) {
					// move also element to the left of TimeDivider to keep sticky constraint 
					rightToIA--;
					moveRight(rightToIA);
				}
			} else if(l > oldl && rightToIA < elementViews.size()) {
				// move all elements that crossed "to the right of IA" border 
				while(rightToIA < elementViews.size()) {
					if(shouldBeMovedLeft(rightToIA)) {
						moveLeft(rightToIA);
						rightToIA++;
					} else {
						break;
					}
				}
				if(isStickyTimeDivider(rightToIA)) {
					// move also sticky TimeDivider to keep sticky constraint 
					moveLeft(rightToIA);
					rightToIA++;
				}
			}
		}
		
		/**
		 * @param elIndex index of element
		 * @return if element was on the right of IA should be moved to the left of IA. 
		 */
		private boolean shouldBeMovedLeft(int elIndex) {
			int middle = elementMiddleVisibleX(elIndex);
			return middle < visibleRectWidth - iaRightMargin + delta - mTouchSlop;
		}
		
		/**
		 * @param elIndex index of element
		 * @return if element was on the left of IA should be moved to the right of IA. 
		 */
		private boolean shouldBeMovedRight(int elIndex) {
			int middle = elementMiddleVisibleX(elIndex);
			return middle > visibleRectWidth - inputAreaWidth - iaRightMargin - delta + mTouchSlop;
		}

		/**
		 * Horizontal position of element relative to visible rectangle.
		 * If element is subject of animation, take it's destination position instead of temporary one. 
		 */
		private int elementMiddleVisibleX(int elIndex) {
			SheetAlignedElementView element = elementViews.get(elIndex);
			return viewStableX(element) - hscroll.getScrollX() + middleX(element);
		}
		
		private void moveRight(int elIndex) {
			move(elIndex, moveDistance());
		}
		
		private void moveLeft(int elIndex) {
			move(elIndex, -moveDistance());
		}
		
		private void move(int elIndex, int xDelta) {
			SheetAlignedElementView element = elementViews.get(elIndex);
			LayoutAnimator.LayoutAnimation<EditActivity, ?> anim = animator.getAnimation(element);
			if(anim != null) {
				// reverse animation
				animator.stopAnimation(anim);
				int dx = anim.startValue()-left(element);
//				log.i("Reverse animation %d--[%d]-->", left(firstToRight), dx);
				animator.startRLAnimation(element, dx, ANIM_TIME/3);
			} else {
				animator.startRLAnimation(element, xDelta, ANIM_TIME);
			}
		}
			
	};
	
	private ScaleGestureInterceptor.OnScaleListener scaleListener = new OnScaleListener() {
		int originalRightToIA;
		public void onScaleBegin() {
			originalRightToIA = rightToIA;
			rightToIA = elementViews.size();
		}
		
		@Override
		public void onScale(float scaleFactor, PointF focusPoint) {
			isScaling = true;
			animator.forceFinishAll();
			int fpX = (int) ((focusPoint.x+hscroll.getScrollX()-notesAreaX)*scaleFactor);
			int fpY = (int) ((focusPoint.y+vertscroll.getScrollY())*scaleFactor);
			updateScaleFactor(sheetParams.getScale()*scaleFactor);
			hscroll.scrollTo((int) (fpX+notesAreaX-focusPoint.x), 0);
			vertscroll.scrollTo(0, (int) (fpY-focusPoint.y));
		}

		@Override
		public void onScaleEnd() {
			scaleGestureDetector.setTouchInputLocked(true);
			// find new rightToIA
			int IAmiddle = visibleRectWidth - iaRightMargin -inputAreaWidth/2;
			final int elementsCount = elementViews.size();
			rightToIA = originalRightToIA;
			if(elementsCount == 1) {
				rightToIA = 1;
			} else {
				if(rightToIA >= elementsCount) {
					rightToIA = searchRightToBackwards(elementsCount-1, IAmiddle);
				} else {
					SheetAlignedElementView prevToRight = elementViews.get(rightToIA);
					if(middleVisibleX(prevToRight) <= IAmiddle) {
						rightToIA = searchRightTo(rightToIA, IAmiddle);
					} else {
						rightToIA = searchRightToBackwards(rightToIA, IAmiddle);
					}
				}
				if(isStickyTimeDivider(rightToIA)) {
					rightToIA++;
				}
			}
//			log.i("onScaleEnd(): new right: %d", rightToIA);
			
			int leftToIAArea = visibleRectWidth -inputAreaWidth - iaRightMargin - delta + mTouchSlop;
			// scroll sheet so left border of IA matches: 
			int destScrollX = rightToIA == 1
				// free place in first Time
				? middleAbsoluteX(elementViews.get(0)) + timeDividerSpacing(times.get(0), false) - (visibleRectWidth - inputAreaWidth - iaRightMargin)
				// middle of leftToIA note + delta
				: middleAbsoluteX(elementViews.get(rightToIA-1)) - (leftToIAArea-mTouchSlop);
			animator.startHScrollAnimation(hscroll, destScrollX-hscroll.getScrollX(), 300, new Runnable() {
				@Override
				public void run() {
					if(rightToIA == elementsCount) {
						scalingFinished();
					} else {
						// translate all notes that are on right of leftToIA area to make space for IA */
						Runnable listn = new WaitManyRunOnce(elementsCount-rightToIA) {
							@Override
							protected void allFinished() {
								scalingFinished();
							}
						};
						for(int i = rightToIA; i < elementsCount; i++) {
							SheetAlignedElementView view = elementViews.get(i);
							animator.startRLAnimation(view, moveDistance(), 300, listn);
						}
					}
				}
			});
		}
		
		/**
		 * search for note which middle is further on x-axis that markerX
		 * @param index search from this index to the end of noteViews
		 * @return index of such note or models.size() if none
		 */
		private int searchRightTo(int index, int markerX) {
			int size = elementViews.size();
			for(; index < size; index++) {
				SheetAlignedElementView view = elementViews.get(index);
				if(middleVisibleX(view) > markerX) {
					break;
				}
			}
			return index;
		}

		/**
		 * search (in reverse order) for note which middle is further on x-axis that markerX
		 * @param index search from this index to the beginning of noteViews
		 * @return index of such note or models.size() if none
		 */
		private int searchRightToBackwards(int index, int markerX) {
			int prevGood = elementViews.size();
			for(; index >= 1; index--) {
				SheetAlignedElementView view = elementViews.get(index);
				if(middleVisibleX(view) > markerX) {
					prevGood = index;
				} else {
					break;
				}
			}
			return prevGood;
		}

		/**
		 * @return vertical position of note head middle inside visible rect
		 */
		public int middleVisibleX(SheetAlignedElementView view) {
			return middleAbsoluteX(view)-hscroll.getScrollX();
		}
		
		private void scalingFinished() {
			isScaling = false;
			scaleGestureDetector.setTouchInputLocked(false);
		}
	};
	
	/**
	 * @return if elementViews(index) is a TimeDivider that should stick to preceding element.
	 */
	private boolean isStickyTimeDivider(int index) {
		return
			index < elementViews.size() 
			// if metrum is defined
			&& sheetParams.getTimeStep() != null  
			// and element is a TimeDivider
			&& elementViews.get(index).model().getElementSpec().getType() == ElementType.TIMES_DIVIDER
			// which closes Time (that has no space left)
		    && index-1 > 0 && elementViews.get(index-1).model().getElementSpec().getType() != ElementType.FAKE_PAUSE;
	}

	protected int findTime(int elementIndex) {
		int i = 0;
		for(; i < times.size(); i++) {
			if(elementIndex < times.get(i).rangeStart) {
				break;
			}
		}
		return i-1;
	}

	private static class Animator extends LayoutAnimator<EditActivity> {
		public Animator(EditActivity ctx) {
			super(ctx);
		}
		
		private static class RLAnimation extends LayoutAnimation<EditActivity, View> {
			public RLAnimation(View view, int currentLMargin, int delta, long duration) {
				super(view, currentLMargin, delta, duration);
			}

			protected void apply(EditActivity ctx, float state) {
				ctx.updatePosition(view, start_value + (int) (delta*state), null);
			}
		}
		private static class HScrollAnimation extends LayoutAnimation<EditActivity, HorizontalScrollView> {
			
			public HScrollAnimation(HorizontalScrollView view, int scrollStartX, int scrollDelta, 
					long duration, Runnable onAnimationEndListener) {
				super(view, scrollStartX, scrollDelta, duration, onAnimationEndListener);
			}

			protected void apply(EditActivity ctx, float state) {
				view.scrollTo(start_value + (int) (delta*state), 0);
			}
		}
		
		public void startRLAnimation(View view, int dx, int duration) {
			startRLAnimation(view, dx, duration, null);
		}
		public void startRLAnimation(View view, int dx, long duration, Runnable listn) {
			RLAnimation anim = new RLAnimation(view, left(view), dx, duration);
			anim.onAnimationEndListener = listn;
//			log.i("startAnimation(): %d --[%d]--> %d, dur: %d", anim.start_value, dx, anim.start_value+dx, duration);
			mStartAnimation(anim);
		}
		public void startHScrollAnimation(HorizontalScrollView hscrollView, int scrollDelta, long duration, Runnable onAnimationEndListener) {
			HScrollAnimation anim = new HScrollAnimation(hscrollView, hscrollView.getScrollX(), scrollDelta, duration, onAnimationEndListener);
			mStartAnimation(anim);
		}
	}
	
	protected void onContainerResize(int visibleRectWidth, int visibleRectHeight) {
		if(visibleRectWidth == this.visibleRectWidth && visibleRectHeight == this.visibleRectHeight)
			return;
		this.visibleRectWidth = visibleRectWidth;
		this.visibleRectHeight = visibleRectHeight;
		// position IA
		MarginLayoutParams params = (MarginLayoutParams) inputArea.getLayoutParams();
		iaRightMargin = params.rightMargin = Math.min(
			getResources().getDimensionPixelSize(R.dimen.inputAreaMaxRightMargin),
			(int) (visibleRectWidth*0.4 - inputAreaWidth)
		);
		inputArea.setLayoutParams(params);
		
		// calculate default scale so spaces/lines (from space -1 to space 5) fit visible height
		float scale = ((float) (visibleRectHeight)) / ((float) (
			sheetParams.getLineFactor() * 5 + sheetParams.getLinespacingFactor() * 6
		));
		// TODO remove hardcoded
		scale = 0.441454f;
		updateScaleFactor(scale);
		
		// TODO calculate sheet start scroll position
		hscroll.post(new Runnable() {
		    @Override
		    public void run() {
				hscroll.scrollTo(declaredWidth(sheet), 0);
				vertscroll.scrollTo(0,
					line0Top + sheetParams.anchorOffset(NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE), AnchorPart.TOP_EDGE)
				);
		    } 
		});
	}

	private void updateScaleFactor(float newScaleFactor) {
		log.i("newScaleFactor: %f", newScaleFactor);
		sheetParams.setScale(newScaleFactor);
		MIN_DRAW_SPACING = (int) (minDrawSpacingFactor*sheetParams.getScale());
		NOTE_DRAW_PADDING = (int) (noteShadow * sheetParams.getScale());
		noteHighlightPaint.setShadowLayer(NOTE_DRAW_PADDING, NOTE_DRAW_PADDING/2, NOTE_DRAW_PADDING, Color.BLACK);		
		lines.setParams(sheetParams);
		int minLinespaceTopOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE), 
			AnchorPart.TOP_EDGE
		);
		line0Top = Math.abs(minLinespaceTopOffset);

		int paddingLeft = 
		Math.max(
			lines.getMinPadding(),
			// assure that when sheet is scrolled to start IA left edge matches start of area where notes are placed
			visibleRectWidth-inputAreaWidth-iaRightMargin - timeDividerSpacing(times.get(0), true)
		);
		lines.setNotesAreaLeftPadding(paddingLeft);
		
		this.notesAreaX = paddingLeft;
		delta = (int) (sheetParams.getScale()*noteMinDistToIA);
		int spacingAfter = notesAreaX;
		int x = 0;
		int timeIndex = -1;
		SheetAlignedElementView v = null;
		for(int i = 0; i < elementViews.size(); i++) {
			x += spacingAfter;
			v = elementViews.get(i);
			v.setPadding(NOTE_DRAW_PADDING);
			if(v.model().getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
				timeIndex++;
				updateTimeSpacingBase(timeIndex, true);
			}
			spacingAfter = afterElementSpacing(times.get(timeIndex), v.model());
			int xpos = x-middleX(v);
			int ypos = sheetElementY(v);
			updatePosition(
				v, 
				xpos,
				ypos
			);
//			log.i("onScaleFactor() note[%d] at: %dx%d", i, xpos, ypos);
		}
		
		correctSheetWidth();
		int maxLinespaceBottomOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE),
			AnchorPart.BOTTOM_EDGE
		);
		updateSize(sheet, null, maxLinespaceBottomOffset - minLinespaceTopOffset);
		updatePosition(lines, 0, line0Top-lines.getPaddingTop());
		updateSize(
			lines, 
			null, 
			sheetParams.anchorOffset(NoteConstants.anchorIndex(4, ANCHOR_TYPE_LINE), BOTTOM_EDGE)
			+ lines.getPaddingTop() + lines.getPaddingBottom()
		);
		
	}

	private void updateTimeSpacingBase(int timeIndex, boolean refreshSheetParams) {
		Time time = times.get(timeIndex);
		if(refreshSheetParams) {
			elementViews.get(time.rangeStart).setSheetParams(sheetParams); // update left TimeDivider
		}
		time.spacingBase = (int) (defaultSpacingBaseFactor * sheetParams.getScale()); // calculate default spacing base
		int baseLength = length(0, minPossibleValue);
		int firstEl = time.rangeStart+1;
		if(refreshSheetParams && firstEl < elementViews.size()) { // update first element of Time if present
			elementViews.get(firstEl).setSheetParams(sheetParams); 
		}
		int lastEl = (timeIndex + 1 < times.size() ? times.get(timeIndex+1).rangeStart : elementViews.size()) - 1;
		for(int i = firstEl; i <= lastEl; i++) { // for each element inside Time 
			SheetAlignedElementView el = elementViews.get(i);
			/** minimal visual spacing between 2 element's middles so that they don't collide */
			int minSpacing = el.model().collisionRegionRight()-el.model().getMiddleX() + MIN_DRAW_SPACING;
			if(i+1 < elementViews.size()) {
				SheetAlignedElementView next = elementViews.get(i+1);
				if(refreshSheetParams) { 
					next.setSheetParams(sheetParams); 
				}
				minSpacing += next.model().getMiddleX()-next.model().collisionRegionLeft();
			}
			time.spacingBase = (int) Math.max(
				time.spacingBase,
				minSpacing * baseLength / el.model().getElementSpec().spacingLength(minPossibleValue)
			);
		}
	}

	/**
	 * Resize sheet accordingly to last element position.
	 */
	private void correctSheetWidth() {
		int lastElIndex = elementViews.size()-1;
		SheetAlignedElementView lastView = elementViews.get(lastElIndex);
		int lastViewShiftedX = 
			viewStableX(lastView) 
			+ (lastElIndex < rightToIA ? moveDistance() : 0)
		;
		int newSheetWidth = Math.max(
			visibleRectWidth,
			lastViewShiftedX + middleX(lastView) + 
			Math.max(
				iaRightMargin - delta + mTouchSlop + 1,
				lastView.measureWidth() - middleX(lastView)
			)
		);
		updateSize(sheet, newSheetWidth, null);
		updateSize(lines, newSheetWidth, null);
	}
	
	private Handler popupHideHandler = new Handler();
	private Runnable mHideInfoPopupTask = new Runnable() {
	   public void run() {
		   findViewById(R.id.EDIT_info_popup).setVisibility(View.GONE);
	   }
	};
	private float noteShadow;
	
	protected void popupCurrentNoteLength() {
		View popup = findViewById(R.id.EDIT_info_popup);
		SheetAlignedElementView noteView = (SheetAlignedElementView) popup.findViewById(R.id.EDIT_info_popup_note);
		SheetParams params = new SheetParams(sheetParams);
		params.setScale(1);
		params.setScale(
			((float) getResources().getDimensionPixelSize(R.dimen.infoPopupIconHeight))
		/	params.anchorOffset(LINE4_ABSINDEX, BOTTOM_EDGE)
		);
		try {
			SheetAlignedElement model = createDrawingModel(new ElementSpec.NormalNote(
				new NoteSpec(
					currentNoteLength, 
					LINE4_ABSINDEX
				), 
				NoteConstants.ORIENT_UP
			)); 
			noteView.setModel(model);
		} catch (CreationException e) {
			e.printStackTrace();
			finish();
			return;
		}
		noteView.setSheetParams(params);
		popup.requestLayout();
		popup.setVisibility(View.VISIBLE);		
		popupHideHandler.removeCallbacks(mHideInfoPopupTask);
		popupHideHandler.postDelayed(mHideInfoPopupTask, getResources().getInteger(R.integer.infoPopupLife));
	}
	
	private Toast lastDisruptText = null;
	protected void disrupt(int stringResId) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		if(lastDisruptText == null) {
			lastDisruptText = Toast.makeText(this, "", Toast.LENGTH_SHORT);			
		}
		lastDisruptText.cancel();
		lastDisruptText.setText(stringResId);
		lastDisruptText.show();
	}

	private void addOverlayView(final ElementsOverlay overlay) {
		SheetElementView<SheetElement> elementView;
		elementView = new SheetElementView<SheetElement>(this, overlay);
		elementView.setPaint(normalPaint);
		elementView.setSheetParams(sheetParams);
		overlay.setTag(elementView);
		overlaysViews.add(elementView);
		sheet.addView(elementView);
		overlay.setObserver(new Observer() {
			@Override
			public void onMeasureInvalidated() {
				// find view
				SheetElementView<SheetElement> ovView = (SheetElementView<SheetElement>) overlay.getTag();
				ovView.invalidateMeasure();
				ovView.invalidate();
				// reposition it
				updatePosition(ovView, overlay.left()-ovView.getPaddingLeft(), overlay.top()-ovView.getPaddingTop());
				updateSize(ovView, ovView.measureWidth(), ovView.measureHeight());
			}
		});
	}
	
	private SheetAlignedElementView addElementView(int index, SheetAlignedElement model) {
		SheetAlignedElementView elementView;
		elementView = new SheetAlignedElementView(this, model);
		elementView.setPaint(normalPaint);
		elementView.setPadding((int) NOTE_DRAW_PADDING);
		elementView.setSheetParams(sheetParams);
		elementViews.add(index, elementView);
		sheet.addView(elementView);
		return elementView;
	}
	
	private void removeElementView(SheetAlignedElementView view) {
		boolean removed = elementViews.remove(view);
		if(!removed) {
			throw new RuntimeException("Tried to remove element view that hasn't been in elementViews");
		}
		view.model().setTag(null);
		sheet.removeView(view);
		if(bindMap.get(view) != null && !bindMap.get(view).isEmpty()) {
			throw new RuntimeException("Removing element view that still have overlays bound to it");
		}
	}
	
	private SheetAlignedElement createDrawingModel(ElementSpec elementSpec) throws CreationException {
		return DrawingModelFactory.createDrawingModel(this, elementSpec);
	}

	private int afterElementSpacing(Time time, SheetAlignedElement sheetAlignedElement) {
		ElementSpec elementSpec = sheetAlignedElement.getElementSpec();
		if(elementSpec.getType() == ElementType.TIMES_DIVIDER) {
			return timeDividerSpacing(time, false);
		} else {
			return length2spacing(time, elementSpec.spacingLength(minPossibleValue), minPossibleValue);
		}
	}
	
	private static int length2spacing(Time time, double lengthInMU, int measureUnit) {
		int baseLength = length(0, measureUnit);
		return (int) (time.spacingBase * lengthInMU / baseLength);
	}
	
	private int timeDividerSpacing(Time time, boolean updateSheetParams) {
		SheetAlignedElementView v = elementViews.get(time.rangeStart);
		if(updateSheetParams) v.setSheetParams(sheetParams);
		int minSpacing = v.model().collisionRegionRight()-v.model().getMiddleX()+(int) (afterTimeDividerVisualSpacingFactor*sheetParams.getScale());
		if(time.rangeStart + 1 < elementViews.size()) {
			SheetAlignedElementView firstTimeEl = elementViews.get(time.rangeStart+1);
			if(updateSheetParams) firstTimeEl.setSheetParams(sheetParams);
			minSpacing += firstTimeEl.model().getMiddleX()-firstTimeEl.model().collisionRegionLeft();
		}
		return minSpacing;
	}
	
	private static int middleAbsoluteX(SheetAlignedElementView view) {
		return left(view)+middleX(view);
	}

	private static int middleX(SheetAlignedElementView view) {
		return view.getPaddingLeft()+view.model().getMiddleX();
	}
	
	/**
	 * Absolute position of element.
	 * If element is subject of animation, take it's destination position instead of temporary one. 
	 */
	private int viewStableX(SheetAlignedElementView element) {
		LayoutAnimator.LayoutAnimation<?, ?> anim = null;
		if((anim = animator.getAnimation(element)) != null) {
			return anim.destValue();
		} else {
			return left(element);
		}
	}
	
	private int sheetElementY(SheetElementView<?> v) {
		return line0Top + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE);
	}	
	
	private static int timeCapacity(TimeStep timeStep, int measureBaseUnit) {
		return length(timeStep.getTempoBaseLength(), measureBaseUnit)*timeStep.getBaseMultiplier();
	}
	
	private int moveDistance() {
		return 2*delta+inputAreaWidth-mTouchSlop;
	}
	
	private static int declaredWidth(View view) {
		return view.getLayoutParams().width;
	}
	
	private Map<SheetAlignedElementView, Set<ElementsOverlay>> bindMap = new HashMap<SheetAlignedElementView, Set<ElementsOverlay>>(); 
	private void bind(ElementsOverlay overlay, SheetAlignedElementView view) {
		if(bindMap.get(view) == null) {
			bindMap.put(view, new LinkedHashSet<ElementsOverlay>());
		}
		bindMap.get(view).add(overlay);
	}
	
	private static int left(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
	}
	private static int top(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
	}

	private void updatePosition(View v, Integer left, Integer top) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		if(left != null) params.leftMargin = left;
		if(top != null) params.topMargin = top;
		v.setLayoutParams(params);
		if(v instanceof SheetAlignedElementView) {
			SheetAlignedElementView view = (SheetAlignedElementView) v;
			Set<ElementsOverlay> overlays = bindMap.get(view);
			if(overlays != null) {
				for(ElementsOverlay ov: overlays) {
					ov.positionChanged(view.model(), params.leftMargin+v.getPaddingLeft(), params.topMargin+v.getPaddingTop());
				}
			}
		}
	}
	
	private static void updateSize(View v, Integer width, Integer height) {
		LayoutParams params = v.getLayoutParams();
		if(width != null) params.width = width;
		if(height != null) params.height = height;
		v.setLayoutParams(params);
	}
	
	private static abstract class WaitManyRunOnce implements Runnable {
		private int amount;
		public WaitManyRunOnce(int amount) {
			this.amount = amount;
		}

		@Override
		public void run() {
			amount--;
			if(amount == 0) {
				allFinished();
			}
		}

		protected abstract void allFinished();
	}
}