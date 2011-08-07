package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE4_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.TOP_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.overallLength;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ModifiedScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner.OnValueChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.OutlineDrawable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor.OnScaleListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.JoinArc;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup.GroupBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.TimeDivider;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.adapter.SheetElementView;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;

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
	private RelativeLayout sheet;
	private ArrayList<Time> times;
	private ArrayList<SheetAlignedElementView> elementViews = new ArrayList<SheetAlignedElementView>();
	private int inputAreaWidth;
	private View inputArea;
	private HorizontalScrollView hscroll;
	private ModifiedScrollView vertscroll;
	private ScaleGestureInterceptor scaleGestureDetector;
	private LayoutAnimator animator = new LayoutAnimator();
	private SheetAlignedElementView newNote;
	
	/**
	 * Index (in elementViews) of element that is first on right side of InputArea,
	 * when there is no such element rightToIA = elementViews.size()
	 */
	private int rightToIA;
	private int iaRightMargin;
	private int delta;
	private int mTouchSlop;
	protected boolean isScaling = false;
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
		sheetParams.setTimeStep(new TimeSpec.TimeStep(3, 1));
		sheetParams.setClef(NoteConstants.Clef.VIOLIN);
		sheetParams.setKeySignature(KeySignature.B_DUR);
		sheetParams.setMinSpaceAnchor(getResources().getInteger(R.integer.minSpaceDefault));
		sheetParams.setMaxSpaceAnchor(getResources().getInteger(R.integer.maxSpaceDefault));
		
		scaleGestureDetector = (ScaleGestureInterceptor) findViewById(R.id.EDIT_scale_detector);
		hscroll = (HorizontalScrollView) findViewById(R.id.EDIT_outer_hscrollview);
		vertscroll = (ModifiedScrollView) findViewById(R.id.EDIT_vertscrollview);
		sheet = (RelativeLayout) findViewById(R.id.EDIT_sheet_container);
		lines = new Sheet5LinesView(this);
		int hColor = getResources().getColor(R.color.highlightColor);
		lines.setHiglightColor(hColor);
		noteHighlightPaint.setColor(hColor);
		sheet.addView(lines, new LayoutParams(LayoutParams.FILL_PARENT, 100));
		scaleGestureDetector.setOnScaleListener(scaleListener);
		// TODO restore
//		sheet.setOnTouchListener(iaTouchListener);
		
		// setup noteValue spinner
		NoteValueSpinner valueSpinner = (NoteValueSpinner) findViewById(R.id.EDIT_note_value_scroll);
		try {
			valueSpinner.setupNoteViews();
		} catch (NoteDescriptionLoadingException e) {
			e.printStackTrace();
			finish();
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
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINE));
		n.setToneModifier(NoteModifier.SHARP);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasDot(true);
		n.setToneModifier(NoteModifier.SHARP);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINE));
		n.setToneModifier(NoteModifier.SHARP);
		n.setHasDot(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(5, NoteConstants.ANCHOR_TYPE_LINE));
		n.setIsGrouped(true);
		n.setHasDot(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINE));
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE));
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+2, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(5, NoteConstants.ANCHOR_TYPE_LINE));
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE));
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINE));
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(5, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
//		rawNotesSequence.add(new ElementSpec.NormalNote(n));
//		rawNotesSequence.add(new ElementSpec.NormalNote(n4));
//		rawNotesSequence.add(new ElementSpec.NormalNote(n4));
//		ns = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINE));
//		ns.setToneModifier(NoteModifier.SHARP);
//		ns.setHasDot(true);
//		models.add(new ElementSpec.NormalNote(ns));
//		models.add(new ElementSpec.NormalNote(
//			new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)))
//		);
//		models.add(new ElementSpec.NormalNote(
//			new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)))
//		);
//		models.add(new ElementSpec.NormalNote(
//			new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)))
//		);
//		models.add(new ElementSpec.NormalNote(
//			new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)))
//		);
//		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE)));
//		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
//		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
//		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		/*
		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINESPACE)));
//		models.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
		models.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		models.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE)));
//		models.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, LINE0_ABSINDEX));
		models.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		models.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINE)));
		*/
		// re-create times
		times = new ArrayList<EditActivity.Time>();
		Time firstTime = new Time(new TimeSpec(
			sheetParams.getTimeStep(), sheetParams.getClef(), sheetParams.getKeySignature()
		));
		times.add(firstTime);
		firstTime.rangeStart = 0;
		if(sheetParams.getTimeStep() != null) {
			TimeSpec.TimeStep currentMetrum = firstTime.spec.getTimeStep();
			int capLeft = timeCapacity(currentMetrum, minPossibleValue);
			int timeIndex = 0;
			for(int i = 0; i < rawNotesSequence.size();) {
				if(timeIndex >= times.size()) {
					Time newTime = new Time(new TimeSpec());
					times.add(newTime);
					newTime.rangeStart = i;
					capLeft = timeCapacity(currentMetrum, minPossibleValue);
				}
				Time currentTime = times.get(timeIndex);
				if(rawNotesSequence.get(i).getType() == ElementType.FAKE_PAUSE) {
					rawNotesSequence.remove(i);
					continue;
				}
				int width = rawNotesSequence.get(i).timeValue(minPossibleValue);
				if(width <= capLeft) {
					capLeft -= width;
					if(capLeft == 0) {
						timeIndex++;
					}
				} else {
					/** create fake pause with given capLeft */
					rawNotesSequence.add(i, new ElementSpec.FakePause(capLeft, minPossibleValue));
					timeIndex++;
				}
				i++;
			}
			if(timeIndex >= times.size()) {
				Time newTime = new Time(new TimeSpec());
				times.add(newTime);
				newTime.rangeStart = rawNotesSequence.size();
			}
		}
		// create element and time views
		try {
			JoinArc arc = null;
			for(int i = 0; i < times.size(); i++) {
				addElementView(new TimeDivider(this, 
					i-1 > 0 ? times.get(i-1).spec : null, 
					times.get(i).spec
				));
				int firstEl = times.get(i).rangeStart;
				int lastEl = (i+1 < times.size() ? times.get(i+1).rangeStart : rawNotesSequence.size()) - 1; 
				GroupBuilder gb = null;
				for(int elI = firstEl; elI <= lastEl; elI++) {
					ElementSpec spec = rawNotesSequence.get(elI);
					if(gb == null && GroupBuilder.canStartGroup(spec)) {
						gb = new GroupBuilder(sheetParams, spec);
						for(int groupEl = elI+1; groupEl <= lastEl; groupEl++) {
							if(!gb.tryExtend(rawNotesSequence.get(groupEl))) {
								break;
							}
						}
						gb.build();
					} 
					SheetAlignedElement model = createDrawingModel(spec);
					if(gb != null) {
						model = gb.wrapNext(model, new GroupObserver(elementViews.size()));
						 if(!gb.hasNext()) {
							 gb = null;
						 }
					}
					if(spec.getType() == ElementType.NOTE) {
						if(arc != null) {
							model = arc.wrapRightElement(model);
						}
						if(((ElementSpec.NormalNote) spec).noteSpec().hasJoinArc()) {
							arc = new JoinArc(model);
							model = arc;
						}
					} else {
						arc = null;
					}
					addElementView(model);
				}
				times.get(i).rangeStart += i; // rangeStart is index in elementViews so I take into account inserted TimeDivider-s
			}
		} catch (NoteDescriptionLoadingException e) {
			e.printStackTrace();
			finish();
		} catch (LoadingSvgException e) {
			e.printStackTrace();
			finish();
		}
		
		((InterceptedHorizontalScrollView) hscroll).setListener(horizontalScrollListener);
		
		rightToIA = elementViews.size();
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
	
	// TODO restore
//	private View.OnTouchListener iaTouchListener = new OnTouchListener() {
//		private static final int INVALID_POINTER = -1;
//		private int activePointerId = INVALID_POINTER;
//		private int currentAnchor;
//		
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
////			log.i(
////				"sheet::onTouch(): %s activePointerId %d", 
////				ReflectionUtils.findConst(MotionEvent.class, "ACTION_", event.getActionMasked()),	
////				activePointerId
////			);
//			switch(event.getActionMasked()) {
//			case MotionEvent.ACTION_DOWN:
//				if(insideIA((int) event.getX())) {
//					currentAnchor = nearestAnchor((int) event.getY());
//					lines.highlightAnchor(currentAnchor);
//					activePointerId = event.getPointerId(event.getActionIndex());
//					if(newNote == null) {
//						newNote = new SheetAlignedElementView(EditActivity.this);
//					}
//					try {
//						newNote.setModel(createDrawingModel(new NoteSpec(currentNoteLength, currentAnchor)));
//					} catch (NoteDescriptionLoadingException e) {
//						e.printStackTrace();
//						finish();
//						return false;
//					}
//					newNote.setSheetParams(sheetParams);
//					newNote.setPaint(noteHighlightPaint);
//					newNote.setPadding((int) NOTE_DRAW_PADDING);
//					sheet.addView(newNote);
//					updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
//					vertscroll.setVerticalScrollingLocked(true);
//					return true;
//				}
//				break;
//			case MotionEvent.ACTION_MOVE:
//				if(activePointerId == INVALID_POINTER) 
//					break;
//				if(!insideIA((int) event.getX())) {
//					cancel();
//					activePointerId = INVALID_POINTER;
//					break;
//				}
//				int newAnchor = nearestAnchor((int) event.getY());
//				if(newAnchor != currentAnchor) {
//					lines.highlightAnchor(newAnchor);
//					try {
//						newNote.setModel(createDrawingModel(new NoteSpec(currentNoteLength, newAnchor)));
//					} catch (NoteDescriptionLoadingException e) {
//						e.printStackTrace();
//						finish();
//						return false;
//					}
//					updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
//					currentAnchor = newAnchor;
//				}
//				return true;
//			case MotionEvent.ACTION_POINTER_1_UP:
//				if(event.getPointerId(event.getActionIndex()) != activePointerId)
//					break;
//			case MotionEvent.ACTION_CANCEL:
//				activePointerId = INVALID_POINTER;
//				cancel();
//				return true;
//			case MotionEvent.ACTION_UP:
//				if(activePointerId == INVALID_POINTER)
//					break;
//				activePointerId = INVALID_POINTER;
//				insertNoteAndClean();
//				return true;
//			}
//			return false;
//		}
//		
//		private void insertNoteAndClean() {
//			int currentTime = findTime(rightToIA-1);
//			int index = rightToIA;
//			rightToIA++;
//			SheetAlignedElementView noteView = newNote;
//			newNote = null;
//			noteView.setPaint(normalPaint);
//			updatePosition(noteView, inIA_noteViewX(noteView), sheetElementY(noteView));
//			elementViews.add(index, noteView);
//			resizeSheetOnNoteInsert(index);
//			
//			int noteMiddle = middleAbsoluteX(noteView);
//			int destNoteMiddle;
//			SheetAlignedElementView prevNote = elementViews.get(index-1);
//			destNoteMiddle = middleAbsoluteX(prevNote) + afterElementSpacing(times.get(currentTime), prevNote);
//			long duration = 500;
//			
//			WaitManyRunOnce animationsEndListener = new WaitManyRunOnce(2+models.size()-rightToIA) {
//				@Override
//				protected void allFinished() {
//					scaleGestureDetector.setTouchInputLocked(false);
//				}
//			}; 
//			
//			animator.startHScrollAnimation(
//				hscroll, 
//				(destNoteMiddle-hscroll.getScrollX())-((visibleRectWidth - inputAreaWidth - iaRightMargin - delta)), 
//				duration, 
//				animationsEndListener
//			);
//			animator.startRLAnimation(
//				noteView, 
//				destNoteMiddle-noteMiddle, 
//				duration, 
//				animationsEndListener
//			);
//			int newNoteSpacing = afterElementSpacing(models.get(index)); 
//			for(int i = rightToIA; i < models.size(); i++) {
//				animator.startRLAnimation(elementViews.get(i), newNoteSpacing, duration, animationsEndListener);
//			}
//			
//			lines.highlightAnchor(null);
//			vertscroll.setVerticalScrollingLocked(false);
//			scaleGestureDetector.setTouchInputLocked(true);
//		}
//
//		private int inIA_noteViewX(SheetAlignedElementView noteView) {
//			return hscroll.getScrollX()+visibleRectWidth-iaRightMargin-inputAreaWidth/2-middleX(noteView);
//		}
//		
//		/**
//		 * Clear all artifacts introduced by touch inside IA box
//		 */
//		private void cancel() {
//			vertscroll.setVerticalScrollingLocked(false);
//			lines.highlightAnchor(null);
//			sheet.removeView(newNote);
//		}
//
//		/**
//		 * @param x in sheet view coordinates
//		 * @return if point (x,?) is inside input area box
//		 */
//		private boolean insideIA(int x) {
//			int pos = x-hscroll.getScrollX();
//			return pos >= visibleRectWidth-inputAreaWidth-iaRightMargin && pos <= visibleRectWidth-iaRightMargin;
//		}
//
//		/**
//		 * find which anchor from range <minSpaceAbsIndex, maxSpaceAbsIndex> is nearest to horizontal line y
//		 * @param y in sheet view coordinates
//		 * @return index of anchor
//		 */
//		private int nearestAnchor(int y) {
//			int line0middle = sheetParams.anchorOffset(LINE0_ABSINDEX, AnchorPart.MIDDLE);
//			int delta =
//				sheetParams.anchorOffset(SPACE0_ABSINDEX, AnchorPart.MIDDLE)
//				- line0middle;
//			int indexDeltaHead = y - (line0Top + line0middle - delta/2);
//			int indexDelta = indexDeltaHead/delta;
//			return Math.max( 
//				Math.min(
//				LINE0_ABSINDEX + indexDelta + (indexDeltaHead < 0 ? -1 : 0),
//				NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
//				),
//				NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
//			);
//		}
//	};
	
	private OnScrollChangedListener horizontalScrollListener = new OnScrollChangedListener() {
		@Override
		public void onScrollChanged(int l, int oldl) {
//			LogUtils.info("scrollChange (%d, %d)", l, oldl);
			if(isScaling) return;
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
			int middle = elementVisibleX(elIndex);
			return middle < visibleRectWidth - iaRightMargin + delta - mTouchSlop;
		}
		
		/**
		 * @param elIndex index of element
		 * @return if element was on the left of IA should be moved to the right of IA. 
		 */
		private boolean shouldBeMovedRight(int elIndex) {
			int middle = elementVisibleX(elIndex);
			return middle > visibleRectWidth - inputAreaWidth - iaRightMargin - delta + mTouchSlop;
		}

		/**
		 * Horizontal position of element relative to visible rectangle.
		 * If element is subject of animation, take it's destination position instead of temporary one. 
		 */
		private int elementVisibleX(int elIndex) {
			SheetAlignedElementView element = elementViews.get(elIndex);
			int x;
			LayoutAnimator.LayoutAnimation<?> anim = null;
			if((anim = animator.getAnimation(element)) != null) {
				x = anim.destValue();
			} else {
				x = leftMargin(element);
			}
			int middle = x - hscroll.getScrollX() + middleX(element);
			return middle;
		}
		
		private void moveRight(int elIndex) {
			move(elIndex, 2*delta+inputAreaWidth);
		}
		
		private void moveLeft(int elIndex) {
			move(elIndex, -2*delta-inputAreaWidth);
		}
		
		private void move(int elIndex, int xDelta) {
			SheetAlignedElementView element = elementViews.get(elIndex);
			LayoutAnimator.LayoutAnimation<?> anim = animator.getAnimation(element);
			if(anim != null) {
				// reverse animation
				animator.stopAnimation(anim);
				int dx = anim.startValue()-leftMargin(element);
//				log.i("Reverse animation %d--[%d]-->", leftMargin(firstToRight), dx);
				animator.startRLAnimation(element, dx, ANIM_TIME/3);
			} else {
				animator.startRLAnimation(element, xDelta, ANIM_TIME);
			}
		}
			
	};

	private ScaleGestureInterceptor.OnScaleListener scaleListener = new OnScaleListener() {
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
			log.i("onScaleEnd(): old right: %d", rightToIA);
			int IAmiddle = visibleRectWidth - iaRightMargin -inputAreaWidth/2;
			final int elementsCount = elementViews.size();
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
			log.i("onScaleEnd(): new right: %d", rightToIA);
			
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
							log.i("onScaleEnd() shifts note[%d] from x: %d", i, leftMargin(view)); 
							animator.startRLAnimation(view, inputAreaWidth+2*delta, 300, listn);
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
			// what closes Time (that has no space left)
		    && index-1 > 0 && elementViews.get(index-1).model().getElementSpec().getType() != ElementType.FAKE_PAUSE;
	}

	private static class LayoutAnimator implements Runnable {
		private static Interpolator interpolator = new AccelerateDecelerateInterpolator();

		private static abstract class LayoutAnimation<ViewType extends View> {
			protected int start_value;
			protected int delta;
			private long duration;
			private long elapsed = 0;
			protected ViewType view;
			protected Runnable onAnimationEndListener = null;
			
			public LayoutAnimation(ViewType view, int start_value, int delta, long duration, Runnable onAnimationEndListener) {
				this(view, start_value, delta, duration);
				this.onAnimationEndListener = onAnimationEndListener;
			}
			public LayoutAnimation(ViewType view, int start_value, int delta, long duration) {
				this.view = view;
				this.start_value = start_value;
				this.delta = delta;
				this.duration = duration;
			}
			public void apply() {
				apply(interpolator.getInterpolation(((float) elapsed)/duration));
			}
			protected abstract void apply(float state);
			public int startValue() {
				return start_value;
			}
			public int destValue() {
				return start_value+delta;
			}
			public boolean isFinished() {
				return elapsed == duration;
			}
		}
		private static class RLAnimation extends LayoutAnimation<View> {
			public RLAnimation(View view, int currentLMargin, int delta, long duration) {
				super(view, currentLMargin, delta, duration);
			}

			protected void apply(float state) {
				updatePosition(view, start_value + (int) (delta*state), null);
			}
		}
		private static class HScrollAnimation extends LayoutAnimation<HorizontalScrollView> {
			
			public HScrollAnimation(HorizontalScrollView view, int scrollStartX, int scrollDelta, 
					long duration, Runnable onAnimationEndListener) {
				super(view, scrollStartX, scrollDelta, duration, onAnimationEndListener);
			}

			protected void apply(float state) {
				view.scrollTo(start_value + (int) (delta*state), 0);
			}
		}
		
		private ArrayList<LayoutAnimation<?>> animations = new ArrayList<LayoutAnimator.LayoutAnimation<?>>();
		private Handler mHandler = new Handler();
		private boolean mIsRunning = false;
		private long lastticktime;
		
		public void startRLAnimation(View view, int dx, int duration) {
			startRLAnimation(view, dx, duration, null);
		}
		public void startRLAnimation(View view, int dx, long duration, Runnable listn) {
			RLAnimation anim = new RLAnimation(view, leftMargin(view), dx, duration);
			anim.onAnimationEndListener = listn;
//			log.i("startAnimation(): %d --[%d]--> %d, dur: %d", anim.start_value, dx, anim.start_value+dx, duration);
			mStartAnimation(anim);
		}
		public void startHScrollAnimation(HorizontalScrollView hscrollView, int scrollDelta, long duration, Runnable onAnimationEndListener) {
			HScrollAnimation anim = new HScrollAnimation(hscrollView, hscrollView.getScrollX(), scrollDelta, duration, onAnimationEndListener);
			mStartAnimation(anim);
		}
		private void mStartAnimation(LayoutAnimation<?> anim) {
			animations.add(anim);
			if(!mIsRunning) {
				lastticktime = System.currentTimeMillis(); 
				mHandler.post(this);
				mIsRunning = true;
			}
		}
		
		public void forceFinishAll() {
			if(mIsRunning) {
				mHandler.removeCallbacks(this);
				mIsRunning = false;
			}
			if(!animations.isEmpty()) {
				for (LayoutAnimation<?> anim: animations) {
					anim.elapsed = anim.duration;
					anim.apply();
				}
				animations.clear();
			}
		}

		public void stopAnimation(LayoutAnimation<?> anim) {
			animations.remove(anim);
			if(animations.isEmpty()) {
				mHandler.removeCallbacks(this);
				mIsRunning = false;
			}
		}

		public LayoutAnimation<?> getAnimation(View view) {
			for (LayoutAnimation<?> anim : animations) {
				if(anim.view == view) return anim;
			}
			return null;
		}

		private ArrayList<LayoutAnimation<?>> temp = new ArrayList<LayoutAnimation<?>>();
		@Override
		public void run() {
			long currTime = System.currentTimeMillis();
			long tick = currTime-lastticktime;
			lastticktime = currTime;
			for (Iterator<LayoutAnimation<?>> it = animations.iterator(); it.hasNext();) {
				LayoutAnimation<?> anim = (LayoutAnimation<?>) it.next();
				anim.elapsed = Math.min(anim.elapsed+tick, anim.duration);
				anim.apply();
				if(anim.isFinished()) {
					it.remove();
					temp.add(anim);
				}
			}
			mIsRunning = !animations.isEmpty();
			if(mIsRunning) {
				mHandler.post(this);
			}
			for(LayoutAnimation<?> anim: temp) {
				if(anim.onAnimationEndListener != null) {
					anim.onAnimationEndListener.run();
				}
			}
			temp.clear();
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
		sheetParams.setScale(newScaleFactor);
		MIN_DRAW_SPACING = (int) (minDrawSpacingFactor*sheetParams.getScale());
		NOTE_DRAW_PADDING = (int) (noteShadow * sheetParams.getScale());
		noteHighlightPaint.setShadowLayer(NOTE_DRAW_PADDING, NOTE_DRAW_PADDING/2, NOTE_DRAW_PADDING, Color.BLACK);		
		lines.setParams(sheetParams);
		// TODO extract this to sheetParams
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
			if(v.model().getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
				timeIndex++;
				updateTimeSpacingBase(timeIndex);
				spacingAfter = timeDividerSpacing(times.get(timeIndex), false);
			} else {
				spacingAfter = afterElementSpacing(times.get(timeIndex), v.model());
			}
			int xpos = x-middleX(v);
			int ypos = sheetElementY(v);
			updatePosition(
				v, 
				xpos,
				ypos
			);
//			log.i("onScaleFactor() note[%d] at: %dx%d", i, xpos, ypos);
		}
		
		// TODO calculate new sheet size
		int sheetNewWidth = Math.max(visibleRectWidth,
			// space for all notes on normal places
			x
			// space for last note shifted on right side of IA
			+ delta + inputAreaWidth + delta
			+ Math.max(
				// when sheet scrolled to end IA must reach shifted last note
				iaRightMargin - (delta - mTouchSlop) + 1,
				// space for right part of note
				v.measureWidth()-middleX(v)
			)
		);
		int maxLinespaceBottomOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE),
			AnchorPart.BOTTOM_EDGE
		);
		updateSize(sheet, sheetNewWidth, maxLinespaceBottomOffset - minLinespaceTopOffset);
		
		updatePosition(lines, 0, line0Top-lines.getPaddingTop());
		updateSize(
			lines, 
			sheetNewWidth, 
			sheetParams.anchorOffset(NoteConstants.anchorIndex(4, ANCHOR_TYPE_LINE), BOTTOM_EDGE)
			+ lines.getPaddingTop() + lines.getPaddingBottom()
		);
		
	}

	private void updateTimeSpacingBase(int timeIndex) {
		Time time = times.get(timeIndex);
		elementViews.get(time.rangeStart).setSheetParams(sheetParams); // update left TimeDivider
		time.spacingBase = (int) (defaultSpacingBaseFactor * sheetParams.getScale()); // calculate default spacing base
		int baseLength = length(0, minPossibleValue);
		int firstEl = time.rangeStart+1;
		if(firstEl < elementViews.size()) { // update first element of Time if present
			elementViews.get(firstEl).setSheetParams(sheetParams); 
		}
		int lastEl = (timeIndex + 1 < times.size() ? times.get(timeIndex+1).rangeStart : elementViews.size()) - 1;
		for(int i = firstEl; i <= lastEl; i++) { // for each element inside Time 
			SheetAlignedElementView el = elementViews.get(i);
			/** minimal visual spacing between 2 element's middles so that they don't collide */
			int minSpacing = el.model().collisionRegionRight()-el.model().getMiddleX() + MIN_DRAW_SPACING;
			if(i+1 < elementViews.size()) {
				SheetAlignedElementView next = elementViews.get(i+1);
				next.setSheetParams(sheetParams);
				minSpacing += next.model().getMiddleX()-next.model().collisionRegionLeft();
			}
			time.spacingBase = (int) Math.max(
				time.spacingBase,
				minSpacing * baseLength / el.model().spacingLength(minPossibleValue)
			);
		}
	}

	/**
	 * overall length = (1 + 1/2 + 1/4 + ... + 1/i) * length, where i := dotExtension()
	 * @return overall length in measureUnit-s
	 */

	/**
	 * Resize sheet accordingly to recently inserted note
	 * @param newNoteIndex index of inserted note
	 */
//	TODO restore
//	private void resizeSheetOnNoteInsert(int newNoteIndex) {
//		if(models.size() == 1) return;
//		int delta;
//		if(newNoteIndex < models.size()-1) {
//			// add spacing of inserted note
//			delta = afterElementSpacing(models.get(newNoteIndex));
//		} else {
//			// add spacing of note that precedes inserted note
//			delta = afterElementSpacing(models.get(models.size()-2));
//		}
//		int newSheetWidth = declaredWidth(sheet)+delta;
//		updateSize(sheet, newSheetWidth, null);
//		updateSize(lines, newSheetWidth, null);
//	}
	
	private Handler mHandler = new Handler();
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
		} catch (NoteDescriptionLoadingException e) {
			e.printStackTrace();
			finish();
		}
		noteView.setSheetParams(params);
		popup.requestLayout();
		popup.setVisibility(View.VISIBLE);		
		mHandler.removeCallbacks(mHideInfoPopupTask);
		mHandler.postDelayed(mHideInfoPopupTask, getResources().getInteger(R.integer.infoPopupLife));
	}
	
	private void addElementView(SheetAlignedElement model) {
		SheetAlignedElementView elementView;
		elementView = new SheetAlignedElementView(this, model);
		elementView.setPaint(normalPaint);
		elementView.setSheetParams(sheetParams);
		elementViews.add(elementView);
		sheet.addView(elementView);
	}
	
	protected SheetAlignedElement createDrawingModel(NoteSpec noteSpec) throws NoteDescriptionLoadingException {
		return createDrawingModel(new ElementSpec.NormalNote(noteSpec));
	}

	private SheetAlignedElement createDrawingModel(ElementSpec elementSpec) throws NoteDescriptionLoadingException {
		return DrawingModelFactory.createDrawingModel(this, elementSpec);
	}

	private int afterElementSpacing(Time time, SheetAlignedElement sheetAlignedElement) {
		return length2spacing(time, sheetAlignedElement.spacingLength(minPossibleValue), minPossibleValue);
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
		return leftMargin(view)+middleX(view);
	}

	private static int middleX(SheetAlignedElementView view) {
		return view.getPaddingLeft()+view.model().getMiddleX();
	}
	
	private int sheetElementY(SheetElementView<?> v) {
		return line0Top + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE);
	}	
	
	private static int timeCapacity(TimeStep timeStep, int measureBaseUnit) {
		return length(timeStep.getTempoBaseLength(), measureBaseUnit)*timeStep.getBaseMultiplier();
	}
	
	private static int leftMargin(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
	}

	private static int declaredWidth(View view) {
		return view.getLayoutParams().width;
	}
	
	private class GroupObserver implements NotesGroup.Observer {
		private int elementViewIndex;
		
		public GroupObserver(int elementViewIndex) {
			this.elementViewIndex = elementViewIndex;
		}

		@Override
		public void onMeasurementInvalid() {
			log.i("onMeasureInvalid() at %d", elementViewIndex);
			int oldMiddleX = lastMiddleAbsoluteX.get(elementViews.get(elementViewIndex));
			SheetAlignedElementView view = elementViews.get(elementViewIndex);
			view.invalidateMeasure();
			int xpos = oldMiddleX-view.model().getMiddleX()-view.getPaddingLeft();
			int ypos = sheetElementY(view);
			updatePosition(
				view, 
				xpos,
				ypos
			);
		}
	}	
	
	private static Map<SheetAlignedElementView, Integer> lastMiddleAbsoluteX = new HashMap<SheetAlignedElementView, Integer>();
	private static void updatePosition(View v, Integer left, Integer top) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		if(left != null) params.leftMargin = left;
		if(top != null) params.topMargin = top;
		v.setLayoutParams(params);
		if(v instanceof SheetAlignedElementView) {
			SheetAlignedElementView elView = (SheetAlignedElementView) v;
			SheetAlignedElement elModel = elView.model();
			lastMiddleAbsoluteX.put(elView, params.leftMargin + v.getPaddingLeft() + elView.model().getMiddleX());
			elModel.positionChanged(
				params.leftMargin + v.getPaddingLeft(), 
				params.topMargin + +v.getPaddingTop()
			);
		}
	}
	
	private static void updateSize(View v, int width, Integer height) {
		LayoutParams params = v.getLayoutParams();
		params.width = width;
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