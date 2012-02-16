package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE4_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart.TOP_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateMargins;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateSize;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Action;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware.IndicatorOrigin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.DisplayMode;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TimeStepDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay.Observer;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.JoinArc;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup.GroupBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.CompoundTouchListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.HackedScrollViewChild;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.InterceptedHorizontalScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.InterceptedHorizontalScrollView.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LockableScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner.OnValueChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.QuickActionsView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ScaleGestureInterceptor;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ScaleGestureInterceptor.OnScaleListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils.OnLayoutListener;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

public class EditActivity extends FragmentActivity implements TimeStepDialog.OnPromptResult {
	protected static final int SPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);

	private static final int ANIM_TIME = 150;
	protected Paint noteHighlightPaint = new Paint();
	protected Paint fakePausePaint = new Paint();
	private int NOTE_DRAW_PADDING = 0;
	private int MIN_DRAW_SPACING;
	protected static final Paint normalPaint = new Paint();
	{
		normalPaint.setAntiAlias(true);
		noteHighlightPaint.setAntiAlias(true);
		fakePausePaint.setAntiAlias(true);
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
	private LockableScrollView vertscroll;
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

	protected int currentNoteLength = 0;
	
	private float noteMinDistToIA;
	private float defaultSpacingBaseFactor;
	private float minDrawSpacingFactor;
	private int minPossibleValue;
	private float afterTimeDividerVisualSpacingFactor;
	private float noteShadow;
	private float fakePauseEffectRadius;
	private int maxLinespaceThickness;
	
	/** takt muzyki */
	private class Time {
		/** index of left divider (TimeDivider class) in elementViews */ 
		private int rangeStart;
		/** current (to sheetParams) spacing after whole note */
		int spacingBase = -1;
		private TimeSpec spec;
		private SheetAlignedElementView view;
		private int capLeft;
		
		public Time(TimeSpec spec) {
			super();
			this.spec = spec;
		}
		
		public boolean isFull() {
			return capLeft == 0;
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
		scaleGestureDetector.setOnTouchListener(quickActionsDismiss);
		hscroll = (HorizontalScrollView) findViewById(R.id.EDIT_outer_hscrollview);
		hscroll.setOnTouchListener(quickActionsDismiss);
		vertscroll = (LockableScrollView) findViewById(R.id.EDIT_vertscrollview);
		vertscroll.setOnTouchListener(quickActionsDismiss);
		sheet = (ViewGroup) findViewById(R.id.EDIT_sheet_container);
		lines = (Sheet5LinesView) findViewById(R.id.EDIT_sheet_5lines);
		((HackedScrollViewChild) vertscroll.getChildAt(0)).setRuler(lines);
		int hColor = getResources().getColor(R.color.highlightColor);
		lines.setHiglightColor(hColor);
		noteHighlightPaint.setColor(hColor);
		scaleGestureDetector.setOnScaleListener(scaleListener);
		sheet.setOnTouchListener(new CompoundTouchListener(
			quickActionsDismiss,
			iaTouchListener,
			noteTouchListener,
			elementTouchListener
		));
		// setup noteValue spinner
		NoteValueSpinner valueSpinner = (NoteValueSpinner) findViewById(R.id.EDIT_note_value_scroll);
		try {
			valueSpinner.setupNoteViews(sheetParams);
			currentNoteLength = valueSpinner.getCurrentValue();
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
		// setup insert button
		findViewById(R.id.EDIT_button_insert).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showQuickActionsAbove(findViewById(R.id.EDIT_button_insert), rightToIA, insertActions);
			}
		});
		
		this.inputArea = findViewById(R.id.EDIT_inputArea);
		this.inputAreaWidth = getResources().getDimensionPixelSize(R.dimen.inputAreaWidth);
		ViewConfiguration configuration = ViewConfiguration.get(this);
        this.mTouchSlop = configuration.getScaledTouchSlop();
		
		noteShadow = readParametrizedFactor(R.string.noteShadow);
		fakePauseEffectRadius = readParametrizedFactor(R.string.fakePauseEffectRadius);
		noteMinDistToIA = readParametrizedFactor(R.string.minDistToIA);
		defaultSpacingBaseFactor = readParametrizedFactor(R.string.defaultTimeSpacingBaseFactor);
		minPossibleValue = getResources().getInteger(R.integer.minNotePossibleValue) + 1;
		minDrawSpacingFactor = readParametrizedFactor(R.string.minDrawSpacing);
		afterTimeDividerVisualSpacingFactor = readParametrizedFactor(R.string.timeDividerDrawAfterSpacingFactor);
		maxLinespaceThickness = getResources().getDimensionPixelSize(R.dimen.maxLinespaceThickness);
		
		try {
			prepareQuickActions();
		} catch (LoadingSvgException e) {
			e.printStackTrace();
			finish();
			return;
		}
		
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
		rawNotesSequence.add(elementSpecNN(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
//		n.setHasJoinArc(true);
		n.setToneModifier(NoteModifier.FLAT);
		rawNotesSequence.add(elementSpecNN(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(elementSpecNN(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.FLAT);
//		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(elementSpecNN(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setHasJoinArc(true);
//		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(elementSpecNN(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setToneModifier(NoteModifier.FLAT);
		n.setHasJoinArc(true);
		n.setIsGrouped(true);
		rawNotesSequence.add(elementSpecNN(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setHasJoinArc(true);
		rawNotesSequence.add(elementSpecNN(n));
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE));
//		n.setHasJoinArc(true);
		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(elementSpecNN(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE));
		n.setIsGrouped(true);
//		n.setHasDot(true);
		rawNotesSequence.add(elementSpecNN(n));
		
		n = new NoteSpec(NoteConstants.LEN_QUATERNOTE+2, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE));
		rawNotesSequence.add(elementSpecNN(n));
		
//		rawNotesSequence.add(new ElementSpec.Pause(new PauseSpec(NoteConstants.LEN_QUATERNOTE)));
//		rawNotesSequence.add(new ElementSpec.Pause(new PauseSpec(NoteConstants.LEN_QUATERNOTE+1)));
		
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
		ViewUtils.setupActivityOnLayout(this, new OnLayoutListener() {
			@Override
			public void onFirstLayoutPassed() {
				log.v("onGlobalLayout() >> HSCROLL %dx%d", hscroll.getWidth(), hscroll.getHeight());
				onContainerResize(hscroll.getWidth(), hscroll.getHeight());
			}
		});
	}
	
	private float readParametrizedFactor(int stringResId) {
		return sheetParams.readParametrizedFactor(getResources().getString(stringResId));
	}
	
	// TODO co jak nie ma ustalonego metrum?
	// TODO co jak zmienia się metrum? nie powienienem przepychać nuty do istniejących taktów tylko stworzyć nowy takt starego metrum
	
	private void rebuildTimes(int startTimeIndex) throws CreationException {
//		log.i("rebuildTimes(%d)", startTimeIndex);
		
		if(sheetParams.getTimeStep() != null) {
			TimeSpec.TimeStep currentMetrum = getCurrentTimeStep(startTimeIndex);
			int i = startTimeIndex < times.size() ? times.get(startTimeIndex).rangeStart : 0;
			int timeIndex = startTimeIndex;
			Time currentTime = null;
			int currentTimeCapcity = 0;
			int prevHandledTime = timeIndex-1;
			for(; i < elementViews.size();) {
				if(timeIndex > prevHandledTime) {
					currentTime = rebuildTime(timeIndex, i, currentMetrum);
					if(currentTime.spec.getTimeStep() != null) {
						currentMetrum = currentTime.spec.getTimeStep();
					}
					currentTimeCapcity = timeCapacity(currentMetrum, minPossibleValue);
					prevHandledTime = timeIndex;
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
				if(timeValue <= currentTime.capLeft) {
//					log.i("rebuildTimes(): element at %d of timeValue %d will shrink %d cap of time[%d]", i, timeValue, capLeft, timeIndex); 
					currentTime.capLeft -= timeValue;
					if(currentTime.capLeft == 0) {
//						log.i("rebuildTimes(): and forced it's end", i, timeValue, timeIndex); 
						timeIndex++;
					}
				} 
				else if(timeValue > currentTimeCapcity) { 
					// try to divide element to fit
					if(elementSpec.getType() != ElementType.NOTE) {
						// drop because no possibility of dividing
						removeElementView(view);
						continue;
					} else {
						ElementSpec.NormalNote note = (NormalNote) elementSpec;
						int capLeft = currentTime.capLeft;
						removeElementView(view);
						insertDiviedNote.insertDivided(i, capLeft, note.noteSpec(), true);
						insertDiviedNote.insertDivided(
							i+insertDiviedNote.getTotal(), 
							timeValue - capLeft, note.noteSpec(), false);
						continue;
					}
				} 
				else {
					fillWithFakePauses.insertDivided(i, currentTime.capLeft, true);
					i += fillWithFakePauses.getTotal();
					timeIndex++;
					continue;
				}
				i++;
			}
			if(timeIndex > prevHandledTime) {
				rebuildTime(timeIndex, i, currentMetrum);
			}
			// usuwam nadmiarowe (względem nowego wyliczenia) obiekty Time
			int lastOldTime = times.size()-1;
			while(lastOldTime > timeIndex) {
				Time removedTime = times.remove(lastOldTime--);
				removeElementView(removedTime.view, false);
			}
		}
	}
	
	private InsertDivided fillWithFakePauses = new InsertDivided() {
		@Override
		protected void handle(int atIndex, int baseLength, int dotExt) throws CreationException {
			PauseSpec pause = new PauseSpec(baseLength);
			pause.setDotExtension(dotExt);
			SheetAlignedElementView pauseView = addElementView(
				atIndex, 
				createDrawingModel(new ElementSpec.Pause(pause, true))
			);
			pauseView.setPaint(fakePausePaint, fakePauseEffectRadius*sheetParams.getScale());
			updatePosition(pauseView, positionAfter(atIndex-1), sheetElementY(pauseView));
		}
	};
	
	private class FillWithPauses extends InsertDivided {
		private Point rebuildRange = new Point();
		
		@Override
		protected void handle(int atIndex, int baseLength, int dotExt) throws CreationException {
			PauseSpec pause = new PauseSpec(baseLength);
			pause.setDotExtension(dotExt);
			SheetAlignedElementView newElement = insertElement(
				atIndex, 
				new ElementSpec.Pause(pause), 
				getTotal() != 1 ? null : rebuildRange
			);
			updatePosition(newElement, inIA_noteViewX(newElement), sheetElementY(newElement));
		}
	}
	private FillWithPauses fillWithPauses = new FillWithPauses();
	
	private class InsertDiviedNote extends InsertDivided {
		private NoteSpec template;
		private List<NoteSpec> specs = new ArrayList<NoteSpec>();

		void insertDivided(int insertIndex, int capToFill, NoteSpec template, boolean addJoinArcAtEnd) throws CreationException {
			this.template = template;
			specs.clear();
			super.insertDivided(insertIndex, capToFill, false);
			int total = specs.size();
			for(int i = 0; i < total; i++) {
				NoteSpec spec = specs.get(i);
				if(addJoinArcAtEnd || i != total-1) {
					spec.setHasJoinArc(true);
				}
				SheetAlignedElementView view = addElementView(
					insertIndex+i,
					createDrawingModel(elementSpecNN(spec))
				);
				updatePosition(view, positionAfter(insertIndex-1), sheetElementY(view));
			}
		}
		
		@Override
		protected void handle(int atIndex, int baseLength, int dotExt) throws CreationException {
			NoteSpec spec = new NoteSpec(template);
			spec.setLength(baseLength);
			spec.setDotExtension(dotExt);
			specs.add(spec);
		}
		
	}
	private InsertDiviedNote insertDiviedNote = new InsertDiviedNote();
	
	private abstract class InsertDivided extends DivideLengthStrategy {
		private int insertIndex;
		private int total;

		void insertDivided(int insertIndex, int capToFill, boolean multipleDots) throws CreationException {
			this.insertIndex = insertIndex;
			this.total = 0;
			divide(capToFill, multipleDots);
		}
		
		@Override
		protected void handle(int baseLength, int dotExt) throws CreationException {
			handle(insertIndex+(total++), baseLength, dotExt);
		}
		
		protected abstract void handle(int atIndex, int baseLength, int dotExt) throws CreationException;

		int getTotal() {
			return total;
		}
	}
	
	private abstract class DivideLengthStrategy {
		void divide(int capToFill, boolean multipleDots) throws CreationException {
			for(int pLength = 0; pLength <= minPossibleValue; pLength++) {
				int bitIndex = (minPossibleValue-pLength);
				if(IntUtils.getFlag(capToFill, bitIndex) == 1) {
					int baseLength = pLength;
					int dotExt = 0;
					for(pLength = pLength+1; pLength <= minPossibleValue; pLength++) {
						bitIndex = (minPossibleValue-pLength);
						if(IntUtils.getFlag(capToFill, bitIndex) == 1) {
							dotExt++;
							if(!multipleDots)
								break;
						} else {
							break;
						}
					}
					handle(baseLength, dotExt);
				}
			}
		}

		protected abstract void handle(int baseLength, int dotExt) throws CreationException;
	}
	
	private Time rebuildTime(int timeIndex, int newRangeStart, TimeStep prevMetrum) throws CreationException {
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
			updatePosition(currentTime.view, positionAfter(newRangeStart-1), sheetElementY(currentTime.view));
		} else {
			currentTime = times.get(timeIndex); 
			elementViews.add(newRangeStart, currentTime.view);
		}
		currentTime.rangeStart = newRangeStart;
		TimeStep metrum = currentTime.spec.getTimeStep();
		currentTime.capLeft = timeCapacity(metrum != null ? metrum : prevMetrum, minPossibleValue);
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
				ElementType type = elementViews.get(i+1).model().getElementSpec().getType();
				if(type != ElementType.TIMES_DIVIDER && type != ElementType.FAKE_PAUSE) {
					throw new RuntimeException(String.format(
						"elementViews[%d] of type FAKE_PAUSE doesn't precede TIME_DIVIDER",
						i
					));
				}
			}
		}
		for(int i = 0; i < times.size(); i++) {
			int rangeStart = times.get(i).rangeStart;
			ElementSpec elementSpec = specAt(rangeStart);
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
				GroupBuilder gb = new GroupBuilder(spec);
				int groupEl = elementI+1;
				for(; groupEl < totalSize; groupEl++) {
					if(!gb.tryExtend(specAt(groupEl))) {
						break;
					}
				}
				if(gb.isValid()) {
					group = gb.build();
					addOverlayView(group);
					// extends endIndex so we reach all grouped elements
					int groupEndIndex = elementI + group.elementsCount() - 1;
					endIndex = Math.max(endIndex, groupEndIndex);
					log.v("buildNoteGroup(): %d -> %d", elementI, groupEndIndex);
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
					log.v("buildJoinArc(): %d -> %d", elementViews.indexOf(arcStart), elementI);
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

	private SheetAlignedElementView insertElement(int insertIndex, ElementSpec spec, Point rebuildRange) throws CreationException {
		// TODO wywalić debugowanie w kodzie produkcyjnym
		debugTimes();
		debugViews();
		assertTimesValidity();
		int currTime = findTimeToInsertTo(insertIndex);
		// wstawić element w miejsce insertIndex
		SheetAlignedElementView newElement = addElementView(insertIndex, createDrawingModel(spec));
		// przeliczyć czy nie zmieniła się struktura taktów
		rebuildTimes(currTime);
		debugTimes();
		assertTimesValidity();
		
		// find NotesGroup start that could be affected by inserted element
		int i = insertIndex - 1;
		for(;i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int ngRebuildIndex = i;
		// find possible JoinArc that will be affected by rebuild of NotesGroup at ngRebuildIndex
		for(i = i-1; i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
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
		
		updatePositions(insertIndex+1, lastEl, visible2absX(visibleRectWidth-iaRightMargin+delta));
		if(rebuildRange != null) rebuildRange.set(jaRebuildIndex, lastEl);
		debugViews();
		return newElement;
	}

	private int findTimeToInsertTo(int insertIndex) {
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
			ElementSpec elementSpec = specAt(ngRebuildIndex);
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int jaRebuildIndex = Math.max(ngRebuildIndex-1, 0);
		for(; jaRebuildIndex > 0; jaRebuildIndex--) {
			ElementSpec elementSpec = specAt(jaRebuildIndex);
			if(JoinArc.canEndJA(elementSpec) || JoinArc.canStrartJA(elementSpec) || !JoinArc.canSkipOver(elementSpec)) {
				break;
			}
			                                                                        
		}
		int rebuildEnd;
		int size = elementViews.size();
		if(timesRebuildRequired) {
			rebuildEnd = size-1;
		} else {
			for(rebuildEnd = elementIndex+1; rebuildEnd < size; rebuildEnd++) {
				ElementSpec elementSpec = specAt(rebuildEnd);
				if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
					break;
				}
			}
			rebuildEnd = Math.min(rebuildEnd, size-1);
		}
		
		log.i(
			"updateElementSpec(%d) rebuilds ja %d->%d ng %d->%d",
			elementIndex,
			jaRebuildIndex, rebuildEnd,
			ngRebuildIndex, rebuildEnd
		);
		clearJoinArcs(jaRebuildIndex, rebuildEnd);
		clearNoteGroups(ngRebuildIndex, rebuildEnd);
		view.setModel(createDrawingModel(newSpec));
		view.setSheetParams(sheetParams);
		if(timesRebuildRequired) {
			int currTime = findTime(elementIndex);
			rebuildTimes(currTime);
		}
		rebuildEnd = Math.min(rebuildEnd, elementViews.size()-1);
		buildNoteGroups(ngRebuildIndex, rebuildEnd);
		buildJoinArcs(jaRebuildIndex, rebuildEnd);
		
		assertTimesValidity();
		rebuildRange.set(jaRebuildIndex, rebuildEnd);
	}
	
	private SheetAlignedElementView removeElement(int elementIndex, Point rebuildRange) throws CreationException {
		debugTimes();
		debugViews();
		assertTimesValidity();
		
		int currTime = findTime(elementIndex);
		// wstawić element w miejsce insertIndex
		SheetAlignedElementView elView = elementViews.get(elementIndex);
		
		// find NotesGroup start that could be affected by removal
		int i = elementIndex - 1;
		for(;i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		int ngRebuildIndex = i;
		// find possible JoinArc that will be affected by rebuild of NotesGroup at ngRebuildIndex
		for(i = i-1; i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
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
		
		if(elementIndex < rightToIA) {
			rightToIA--;
		}
		if(rebuildRange != null) {
			rebuildRange.set(jaRebuildIndex, lastEl);
		}
		return elView;
	}
	
	private void forceCloseTime(int insertIndex) {
		int timeIndex = findTimeToInsertTo(insertIndex);
		Time time = times.get(timeIndex);
		int capToFill = timeCapacity(getCurrentTimeStep(timeIndex), minPossibleValue);
		for(int i = time.rangeStart+1; i < insertIndex; i++) {
			ElementSpec spec = specAt(i);
			switch(spec.getType()) {
			case NOTE:
			case PAUSE:
				capToFill -= spec.timeValue(minPossibleValue);
				break;
			case FAKE_PAUSE:
				break;
			default:
				throw new CodeLogicError("Unexpected type of element: "+spec.getType().name());
			}
		}
		try {
			fillWithPauses.insertDivided(insertIndex, capToFill, false);
			if(fillWithPauses.getTotal() > 0) {
				fillWithPauses.rebuildRange.y += fillWithPauses.getTotal()-1;
				postInsert(insertIndex+fillWithPauses.getTotal()-1, fillWithPauses.rebuildRange);
			}
		} catch(CreationException e) {
			log.e(null, e);
			// TODO handle exception gracefully
			finish();
		}
	}
	
	private View.OnTouchListener elementTouchListener = new OnTouchListener() {
		Rect hitRect = new Rect();
		int selectedIndex = -1;
		IndexAwareAction[] actions;
		
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
				if(i < elementViews.size()) {
					switch(specAt(i).getType()) {
					case PAUSE:
						actions = possibleActions;
						break;
					case TIMES_DIVIDER:
						actions = timedividerActions;
						break;
					default:
						return false;
					}
					selectedIndex = i;
					SheetAlignedElementView view = elementViews.get(selectedIndex);
					view.setPaint(noteHighlightPaint, NOTE_DRAW_PADDING);
					return true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if(selectedIndex != -1) {
					showElementQuickActions(selectedIndex, actions);
				}
			case MotionEvent.ACTION_CANCEL:
				if(selectedIndex != -1) {
					SheetAlignedElementView view = elementViews.get(selectedIndex);
					view.setPaint(normalPaint, NOTE_DRAW_PADDING);
				}
				selectedIndex = -1;
			}
			return false;
		}
	};
	
	private View.OnTouchListener noteTouchListener = new OnTouchListener() {
		private static final int INVALID_POINTER = -1;
		private int activePointerId = INVALID_POINTER;
		
		Rect hitRect = new Rect();
		private int selectedIndex;
		private int touchYoffset;
		private int currentAnchor, startAnchor;
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
				if(i >= elementViews.size() || specAt(i).getType() != ElementType.NOTE) {
					break;
				}
				this.selectedIndex = i;
				SheetAlignedElementView view = elementViews.get(selectedIndex);
				view.setPaint(noteHighlightPaint, NOTE_DRAW_PADDING);
				setVerticalScrollingLocked(true);
				currentAnchor = startAnchor = view.model().getElementSpec().positonSpec().positon();
				highlightAnchor(currentAnchor);
				touchYoffset = (int) event.getY() - line0Top() - sheetParams.anchorOffset(currentAnchor, AnchorPart.MIDDLE);
				activePointerId = event.getPointerId(event.getActionIndex());
				absMiddleX = middleAbsoluteX(view);
				return true;
			case MotionEvent.ACTION_MOVE:
				if(activePointerId == INVALID_POINTER)
					break;
				int newAnchor = nearestAnchor((int) event.getY(event.findPointerIndex(activePointerId)) - touchYoffset);
				if(newAnchor != currentAnchor) {
					currentAnchor = newAnchor;
					highlightAnchor(currentAnchor);
					try {
						SheetAlignedElementView elView = elementViews.get(selectedIndex);
						updateElementSpec(selectedIndex, elementSpecNN(new NoteSpec(((NormalNote) elView.model().getElementSpec()).noteSpec(), currentAnchor)), temp);
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
				if(startAnchor == currentAnchor) {
					showElementQuickActions(selectedIndex, possibleActions);
				}
			case MotionEvent.ACTION_CANCEL:
				activePointerId = INVALID_POINTER;
				elementViews.get(selectedIndex).setPaint(normalPaint, NOTE_DRAW_PADDING);
				setVerticalScrollingLocked(false);
				highlightAnchor(null);
				if(startAnchor != currentAnchor) {
					animatedRepositioning(
						selectedIndex, selectedIndex, 
						selectedIndex, 
						abs2visibleX(middleAbsoluteX(elementViews.get(selectedIndex))), 
						300
					);
				}				
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
		private boolean addGroupFlag;
		
		private Runnable lazyActionDown = new Runnable() {
			public void run() {
				if(downPointerId != INVALID_POINTER && insideIA(downCoords.x)) {
					insertIndex = rightToIA;
					while(insertIndex > 0 && specAt(insertIndex-1).getType() == ElementType.FAKE_PAUSE) {
						insertIndex--;
					}
					currentAnchor = nearestAnchor(downCoords.y);
					ElementSpec.NormalNote newNoteSpec = elementSpecNN(new NoteSpec(currentNoteLength, currentAnchor));
					if(!willFitInTime(insertIndex, newNoteSpec)) {
						disrupt(R.string.EDIT_msg_inserterror_notetolong);
						downPointerId = INVALID_POINTER;
						return;
					}
					highlightAnchor(currentAnchor);
					try {
						rightToIA = insertIndex+1;
						activePointerId = downPointerId;
						downPointerId = INVALID_POINTER;
						// <!-- check if we insert inside NoteGroup. If so, automatically add GROUP flag.
						addGroupFlag = GroupBuilder.couldExtendGroup(newNoteSpec);
						for(int index = insertIndex-1; addGroupFlag && index > 0; index--) {
							ElementSpec spec = specAt(index);
							if(GroupBuilder.canStartGroup(spec)) {
								addGroupFlag = true;
								break;
							} else if(!GroupBuilder.canExtendGroup(spec)) {
								addGroupFlag = false;
								break;
							}
						}
						addGroupFlag &= insertIndex < elementViews.size() && GroupBuilder.canEndGroup(specAt(insertIndex));
						newNoteSpec.noteSpec().setIsGrouped(addGroupFlag);
						// -->
						SheetAlignedElementView newNote = insertElement(insertIndex, newNoteSpec, rebuildRange);
						newNote.setPaint(noteHighlightPaint, NOTE_DRAW_PADDING);
						updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
						setVerticalScrollingLocked(true);
					} catch (CreationException e) {
						e.printStackTrace();
						finish();
					}
				}
			}
		};
		
		Point temp = new Point(), rebuildRange = new Point();
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
					v.postDelayed(lazyActionDown, 100);
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
					highlightAnchor(newAnchor);
					try {
						NoteSpec spec = new NoteSpec(currentNoteLength, newAnchor);
						spec.setIsGrouped(addGroupFlag);
						updateElementSpec(insertIndex, elementSpecNN(spec), temp);
						rebuildRange.set(Math.min(temp.x, rebuildRange.x), Math.max(temp.y, rebuildRange.y));
						updatePositions(temp.x, insertIndex-1);
						SheetAlignedElementView newNote = elementViews.get(insertIndex);
						updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
						updatePositions(insertIndex+1, temp.y, visible2absX(visibleRectWidth-iaRightMargin+delta));
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

		private void insertNoteAndClean() {
			SheetAlignedElementView noteView = elementViews.get(insertIndex);
			noteView.setPaint(normalPaint, NOTE_DRAW_PADDING);
			highlightAnchor(null);
			setVerticalScrollingLocked(false);
			postInsert(insertIndex, rebuildRange);
		}
		
		/**
		 * Clear all artifacts introduced by touch inside IA box
		 */
		private void cancel() {
			setVerticalScrollingLocked(false);
			highlightAnchor(null);
			
			if(downPointerId != INVALID_POINTER) {
				hscroll.removeCallbacks(lazyActionDown);
				log.i("iaTouchListener::cancel() insert reverted");
			} else if(activePointerId != INVALID_POINTER) {
				try {
					removeElement(insertIndex, null);
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

		/**
		 * @param x in sheet view coordinates
		 * @return if point (x,?) is inside input area box
		 */
		private boolean insideIA(int x) {
			int pos = abs2visibleX(x);
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
		int indexDeltaHead = y - (line0Top() + line0middle - delta/2);
		int indexDelta = indexDeltaHead/delta;
		return Math.max( 
			Math.min(
			LINE0_ABSINDEX + indexDelta + (indexDeltaHead < 0 ? -1 : 0),
			NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
			),
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
		);
	}
	
	private boolean willFitInTime(int insertIndex, ElementSpec spec) {
		int timeIndex = findTimeToInsertTo(insertIndex);
		TimeStep currentTimeStep = getCurrentTimeStep(timeIndex);
		if(currentTimeStep == null) {
			return true;
		}
		int capLeft = timeCapacity(currentTimeStep, minPossibleValue);
		for(int i = times.get(timeIndex).rangeStart + 1; i < insertIndex; i++) {
			capLeft -= specAt(i).timeValue(minPossibleValue);
		}
		return spec.timeValue(minPossibleValue) <= capLeft;
	}
	
	private TimeStep getCurrentTimeStep(int timeIndex) {
		TimeStep result = sheetParams.getTimeStep(), curr;
		timeIndex = Math.min(timeIndex, times.size()-1);
		for(int i = 0; i <= timeIndex; i++) {
			if((curr = times.get(i).spec.getTimeStep()) != null) {
				result = curr;
			}
		}
		return result;
	}
	
	private OnScrollChangedListener horizontalScrollListener = new OnScrollChangedListener() {
		@Override
		public void onScrollChanged(int l, int oldl) {
			hideQuickActionsPopup();
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
			}
		}
		
		/**
		 * @param elIndex index of element
		 * @return if element was on the right of IA should be moved to the left of IA. 
		 */
		private boolean shouldBeMovedLeft(int elIndex) {
			int middle = elementMiddleVisibleX(elIndex);
			return middle < moveLeftBorder();
		}
		
		/**
		 * @param elIndex index of element
		 * @return if element was on the left of IA should be moved to the right of IA. 
		 */
		private boolean shouldBeMovedRight(int elIndex) {
			int middle = elementMiddleVisibleX(elIndex);
			return middle > moveRightBorder();
		}

		/**
		 * Horizontal position of element relative to visible rectangle.
		 * If element is subject of animation, take it's destination position instead of temporary one. 
		 */
		private int elementMiddleVisibleX(int elIndex) {
			SheetAlignedElementView element = elementViews.get(elIndex);
			return abs2visibleX(viewStableX(element)) + middleX(element);
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
		private int originalRightToIA;
		private boolean scalingOccured = false;
		
		public void onScaleBegin() {
			originalRightToIA = rightToIA;
			rightToIA = elementViews.size();
			scalingOccured = false;
		}
		
		@Override
		public void onScale(float scaleFactor, PointF focusPoint) {
			isScaling = true;
			animator.forceFinishAll();
			hideQuickActionsPopup();
			float oldScale = sheetParams.getScale();
			float newScale = oldScale*scaleFactor;
			sheetParams.setScale(newScale);
			if(sheetParams.getLinespacingThickness() > maxLinespaceThickness ||
				sheetParams.getLineThickness() < 1) {
				sheetParams.setScale(oldScale);
				return;
			}
			int fpNewRelX = (int) ((visible2absX((int) focusPoint.x)-notesAreaX)*scaleFactor);
			int line0NewVisibleY = (int) (focusPoint.y + (abs2visibleY(line0Top()) - focusPoint.y)*scaleFactor);
			updateScaleFactor(newScale);
			hscroll.scrollTo((int) (fpNewRelX+notesAreaX-focusPoint.x), 0);
			fixLine0VisibleY(line0NewVisibleY);
			scalingOccured = true;
		}

		@Override
		public void onScaleEnd() {
			if(!scalingOccured) {
				rightToIA = originalRightToIA;
				return;
			}
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
			}
//			log.i("onScaleEnd(): new right: %d", rightToIA);
			
			int leftToIAArea = moveRightBorder();
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
			return abs2visibleX(middleAbsoluteX(view));
		}
		
		private void scalingFinished() {
			isScaling = false;
			scaleGestureDetector.setTouchInputLocked(false);
		}
	};
	
	private void postInsert(int insertIndex, Point rebuildRange) {
		int timeIndex = findTime(insertIndex);
		boolean isLastNoteOfTime = timeIndex+1 < times.size() && insertIndex+1 == times.get(timeIndex+1).rangeStart; 
		animatedRepositioning(
			rebuildRange.x, 
			rebuildRange.y, 
			isLastNoteOfTime && times.get(timeIndex).isFull() ? insertIndex+1 : insertIndex, 
			visibleRectWidth - inputAreaWidth - iaRightMargin - delta, 
			500
		);
	}
	
	private void animatedRepositioning(int rebuildStart, int rebuildEnd, int pinnedElementIndex, int pinVisiblePositionX, long animationDuration) {
		animator.forceFinishAll();
		isPositioning = true;
		int startTime = findTime(rebuildStart), endTime = findTime(rebuildEnd);
		for(int timeI = startTime; timeI <= endTime; timeI++) {
			updateTimeSpacingBase(timeI, false);
		}
		
		// find new rightToIA
		int elementsCount = elementViews.size();
		if(pinVisiblePositionX >= moveLeftBorder()) {
			int currX = pinVisiblePositionX;
			int i = pinnedElementIndex-1;
			for(; i >= 0; i--) {
				SheetAlignedElementView view = elementViews.get(i);
				currX -= afterElementSpacing(times.get(findTime(i)), view.model());
				if(currX < moveLeftBorder())
					break;
			}
			rightToIA = i+1;
		} else {
			int nextX = pinVisiblePositionX;
			int i = pinnedElementIndex;
			for(; i < elementsCount-1; i++) {
				SheetAlignedElementView view = elementViews.get(i);
				nextX += afterElementSpacing(times.get(findTime(i)), view.model());
				if(nextX > moveRightBorder())
					break;
			}
			rightToIA = i+1;
		}
		
		Time time = times.get(startTime);
		int repositioningStart = time.rangeStart;
		WaitManyRunOnce animationsEndListener = new WaitManyRunOnce(1+elementsCount-repositioningStart) {
			@Override
			protected void allFinished() {
				correctSheetWidth();
				isPositioning = false;
				scaleGestureDetector.setTouchInputLocked(false);
			}
		};
		int x = positionAfter(time.rangeStart-1);
		int timeIndex = startTime-1;
		int rescrollDest = -1;
		for(int i = repositioningStart; i < elementsCount; i++) {
			if(i == rightToIA) {
				x += moveDistance();
			}
			SheetAlignedElementView v = elementViews.get(i);
			// animate view to it's correct position
			int dx = (x - middleX(v)) - left(v);
			animator.startRLAnimation(v, dx, animationDuration, animationsEndListener);
			if(i == pinnedElementIndex) {
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
		
		/** destination value of hscroll.scrollX */
		int destScrollX = rescrollDest-pinVisiblePositionX;
		/** how long part of resized sheet will remain in visible rect or further to right after scrolling 
		 *  to destScrollX
		 */
		int newWidth = getValidSheetWidth();
		int rest = newWidth - destScrollX;
		if(rest < visibleRectWidth) {
			/* if remaining part is to short we correct pinning */
			pinVisiblePositionX += visibleRectWidth - rest;
		}
		if(getCurrentSheetWidth() < newWidth) {
			correctSheetWidth();
		}
		
		animator.startHScrollAnimation(
			hscroll, 
			abs2visibleX(rescrollDest)-pinVisiblePositionX, 
			animationDuration, 
			animationsEndListener
		);
		
		scaleGestureDetector.setTouchInputLocked(true);		
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
			anim.setOnAnimationEndListener(listn);
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
		
		iaRightMargin = ((View) inputArea.getParent()).getWidth() - inputArea.getRight();
		
		// calculate default scale so spaces/lines (from space -1 to space 5) fit visible height
		float scale = ((float) (visibleRectHeight)) / ((float) (
			sheetParams.getLineFactor() * 5 + sheetParams.getLinespacingFactor() * 6
		));
		sheetParams.setScale(scale);
		int optimal = getResources().getDimensionPixelSize(R.dimen.optimalLinespaceThickness);
		int current = sheetParams.getLinespacingThickness();
		if(current > optimal) {
			scale = scale * optimal / current;
		}
		sheetParams.setScale(scale);
		if(sheetParams.getLineThickness() < 1) {
			scale = scale * 1 / sheetParams.getLineThickness();
		}
		updateScaleFactor(scale);
		
		int vis2 = visibleRectHeight/2;
		int linesHalf = sheetParams.anchorOffset(NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE), AnchorPart.MIDDLE);
		fixLine0VisibleY(vis2 - linesHalf);
		
		// TODO calculate sheet start scroll position
		hscroll.post(new Runnable() {
		    @Override
		    public void run() {
				hscroll.scrollTo(declaredWidth(sheet), 0);
				sheet.setVisibility(View.VISIBLE);
		    } 
		});
	}
	
	private void onFirstTimeDividerChanged() {
		int paddingLeft = 
		Math.max(
			lines.getMinNotesAreaLeftPadding() + middleX(elementViews.get(0)),
			// assure that when sheet is scrolled to start IA left edge matches start of area where notes are placed
			visibleRectWidth-inputAreaWidth-iaRightMargin + mTouchSlop - timeDividerSpacing(times.get(0), true)
		);
		lines.setNotesAreaLeftPadding(paddingLeft);
		this.notesAreaX = paddingLeft;
	}
	
	private void fixLine0VisibleY(int visY) {
		((HackedScrollViewChild) vertscroll.getChildAt(0)).fixRulerVisibleY(visY - lines.getPaddingTop());
	}

	private void updateScaleFactor(float newScaleFactor) {
		log.d("newScaleFactor: %f", newScaleFactor);
		sheetParams.setScale(newScaleFactor);
		MIN_DRAW_SPACING = (int) (minDrawSpacingFactor*sheetParams.getScale());
		NOTE_DRAW_PADDING = (int) (noteShadow * sheetParams.getScale());
		noteHighlightPaint.setShadowLayer(NOTE_DRAW_PADDING, NOTE_DRAW_PADDING/2, NOTE_DRAW_PADDING, Color.BLACK);		
		fakePausePaint.setMaskFilter(new BlurMaskFilter(fakePauseEffectRadius*sheetParams.getScale(), Blur.OUTER));
		NOTE_DRAW_PADDING = (int) Math.max(fakePauseEffectRadius*sheetParams.getScale(), NOTE_DRAW_PADDING);
		NOTE_DRAW_PADDING = Math.max(MIN_DRAW_SPACING, NOTE_DRAW_PADDING);
		delta = (int) (sheetParams.getScale()*noteMinDistToIA);
		log.d("updateScaleFactor(%f): delta = %d", newScaleFactor, delta);
		// <!-- correct "5 lines" View to assure that min/maxSpaceAnchor is visible
		int minLinespaceTopOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE), 
			AnchorPart.TOP_EDGE
		);
		int maxLinespaceBottomOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE),
			AnchorPart.BOTTOM_EDGE
		);
		int line4bottomOffset = sheetParams.anchorOffset(
			NoteConstants.LINE4_ABSINDEX,
			AnchorPart.BOTTOM_EDGE
		);
		lines.setParams(sheetParams, 
			Math.abs(minLinespaceTopOffset), 
			Math.abs(maxLinespaceBottomOffset - line4bottomOffset)
		);
		updatePosition(lines, null, 0);
		// -->
		onFirstTimeDividerChanged();
		
		int spacingAfter = notesAreaX;
		int x = 0;
		int timeIndex = -1;
		for(int i = 0; i < overlaysViews.size(); i++) {
			overlaysViews.get(i).updateDrawRadius(NOTE_DRAW_PADDING);
		}
		for(int i = 0; i < elementViews.size(); i++) {
			x += spacingAfter;
			SheetAlignedElementView v = elementViews.get(i);
			v.updateDrawRadius(NOTE_DRAW_PADDING);
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
	
	private void setVerticalScrollingLocked(boolean verticalScrollingLocked) {
		vertscroll.setVerticalScrollingLocked(verticalScrollingLocked);
	}

	/**
	 * Resize sheet accordingly to last element position.
	 */
	private void correctSheetWidth() {
		int newSheetWidth = getValidSheetWidth();
		updateSize(sheet, newSheetWidth, null);
		updateSize(lines, newSheetWidth, null);
	}

	private int getValidSheetWidth() {
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
		return newSheetWidth;
	}
	
	private int getCurrentSheetWidth() {
		return sheet.getLayoutParams().width;
	}
	
	private Handler popupHideHandler = new Handler();
	private Runnable mHideInfoPopupTask = new Runnable() {
	   public void run() {
		   findViewById(R.id.EDIT_info_popup).setVisibility(View.GONE);
	   }
	};
	
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
	
	private IndexAwareAction[] possibleActions;
	private IndexAwareAction[]	modifiersActions;
	private IndexAwareAction[] insertActions;
	private IndexAwareAction[] timedividerActions;
	private int elementActionIndex;
	
	private abstract class IndexAwareAction implements Action {
		protected boolean mPostHide = true; 
		@Override
		public final void perform() {
			perform(elementActionIndex);
			if(mPostHide)
				hideQuickActionsPopup();
		}
		
		protected abstract void perform(int elementIndex);
		protected abstract boolean isValidOn(int elementIndex);
		
		protected boolean isValidIndex(int elementIndex) {
			return elementIndex >= 0 && elementIndex < elementViews.size();
		}

		protected Boolean getState(int contextElementIndex) {
			return null;
		}
		
		@Override
		public Boolean getState() {
			return getState(elementActionIndex);
		}
	};
	
	private abstract class SvgIconAction extends IndexAwareAction {
		private SvgImage icon;
		
		public SvgIconAction(int svgResId) throws LoadingSvgException {
			icon = NotePartFactory.prepareSvgImage(EditActivity.this, svgResId);
		}
		
		@Override
		public SvgImage icon() {
			return icon;
		}
	};
	
	private abstract class UpdateElementAction extends SvgIconAction {
		
		public UpdateElementAction(int svgResId) throws LoadingSvgException {
			super(svgResId);
		}

		@Override
		protected void perform(int elementIndex) {
			if(!isValidIndex(elementIndex))
				throw new InvalidParameterException();
			try {
				SheetAlignedElementView view = elementViews.get(elementIndex);
				int absMiddleX = middleAbsoluteX(view);
				Point range = new Point();
				updateElementSpec(elementIndex, updatedSpec(view.model().getElementSpec()), range);
				updatePosition(view, absMiddleX-middleX(view), sheetElementY(view));
				animatedRepositioning(range.x, range.y, elementIndex, abs2visibleX(absMiddleX), 500);
			} catch (CreationException e) {
				// TODO more gracefully
				e.printStackTrace();
				finish();
				return;
			}
		}

		protected abstract ElementSpec updatedSpec(ElementSpec elementSpec);
	};
	
	private class RemoveElementAction extends SvgIconAction {
		
		public RemoveElementAction(int svgResId) throws LoadingSvgException {
			super(svgResId);
		}

		Point rebuildRange = new Point();
		@Override
		protected void perform(int elementIndex) {
			if(!isValidIndex(elementIndex))
				throw new InvalidParameterException();
			try {
				removeElement(elementIndex, rebuildRange);
				int pinElIndex = elementIndex-1;
				animatedRepositioning(
					rebuildRange.x, rebuildRange.y, 
					pinElIndex, abs2visibleX(middleAbsoluteX(elementViews.get(pinElIndex))), 
					500
				);
			} catch (CreationException e) {
				e.printStackTrace();
				finish();
				return;
			}
		}
		
		@Override
		protected boolean isValidOn(int elementIndex) {
			if(isValidIndex(elementIndex)) {
				switch(specAt(elementIndex).getType()) { 
				case NOTE:
				case PAUSE:
					return true;
				}
			}
			return false;
		}
	}
	
	private class ToggleJoinArc extends UpdateElementAction {

		public ToggleJoinArc(int svgResId) throws LoadingSvgException {
			super(svgResId);
		}

		@Override
		protected ElementSpec updatedSpec(ElementSpec elementSpec) {
			ElementSpec.NormalNote spec = (NormalNote) elementSpec;
			return elementSpecNN(new NoteSpec(spec.noteSpec(), NoteSpec.TOGGLE_FIELD.HAS_JOIN_ARC));
		}

		@Override
		protected boolean isValidOn(int elementIndex) {
			if(!isValidIndex(elementIndex))
				return false;
			ElementSpec elementSpec = elementViews.get(elementIndex).model().getElementSpec();
			if(!JoinArc.couldStartWithJA(elementSpec))
				return false;
			for(int i = elementIndex+1; i < elementViews.size(); i++) {
				elementSpec = elementViews.get(i).model().getElementSpec();
				if(JoinArc.canEndJA(elementSpec)) {
					return true;
				} else if(!JoinArc.canSkipOver(elementSpec)) {
					break;
				}
 			}
			return false;
		}
		
		@Override
		protected Boolean getState(int elementIndex) {
			return isValidIndex(elementIndex) 
			&& ((ElementSpec.NormalNote) elementViews.get(elementIndex).model().getElementSpec()).noteSpec().hasJoinArc();
		}
	}
	
	@SuppressWarnings("unchecked")
	private abstract class ToggleDot<T extends ElementSpec> extends UpdateElementAction {
		ElementType type;

		private ToggleDot(int svgResId, ElementType type)
				throws LoadingSvgException {
			super(svgResId);
			this.type = type;
		}

		@Override
		protected ElementSpec updatedSpec(ElementSpec elementSpec) {
			return toggledCopy((T) elementSpec);
		}

		protected abstract ElementSpec toggledCopy(T spec);

		@Override
		protected boolean isValidOn(int elementIndex) {
			if(isValidIndex(elementIndex)) {
				ElementSpec elementSpec = elementViews.get(elementIndex).model().getElementSpec();
				if(elementSpec.getType() == type) {
					return willFitInTime(elementIndex, toggledCopy((T) elementSpec));
				}
			}
			return false;
		}
		
		@Override
		protected Boolean getState(int elementIndex) {
			return isValidIndex(elementIndex) 
			&& specAt(elementIndex).lengthSpec().dotExtension() > 0;
		}
	}
	
	private class ToggleNoteDot extends ToggleDot<ElementSpec.NormalNote> {

		public ToggleNoteDot(int svgResId) throws LoadingSvgException {
			super(svgResId, ElementType.NOTE);
		}

		protected ElementSpec toggledCopy(NormalNote spec) {
			NoteSpec copy = new NoteSpec(spec.noteSpec());
			if(copy.dotExtension() > 0) {
				copy.setDotExtension(0);
			} else {
				copy.setDotExtension(1);
			}
			return elementSpecNN(copy);
		}
	}
	
	private class TogglePauseDot extends ToggleDot<ElementSpec.Pause> {

		public TogglePauseDot(int svgResId) throws LoadingSvgException {
			super(svgResId, ElementType.PAUSE);
		}

		protected ElementSpec toggledCopy(ElementSpec.Pause spec) {
			PauseSpec copy = new PauseSpec(spec.pauseSpec());
			if(copy.dotExtension() > 0) {
				copy.setDotExtension(0);
			} else {
				copy.setDotExtension(1);
			}
			return new ElementSpec.Pause(copy);
		}
	}
	
	private abstract class IndexAwareActionWrapper extends IndexAwareAction {
		private int startIndex;
		private int lastElementIndex;
		private IndexAwareAction wrappedElement;

		public IndexAwareActionWrapper(IndexAwareAction wrappedElement) throws LoadingSvgException {
			this.wrappedElement = wrappedElement;
		}

		@Override
		protected void perform(int elementIndex) {
			if(elementIndex != lastElementIndex)
				throw new InvalidParameterException("Called perform() without calling isValidOn() first");
			wrappedElement.perform(startIndex);
		}

		@Override
		protected boolean isValidOn(int elementIndex) {
			Integer actualIndex = getActualIndex(elementIndex);
			if(actualIndex == null) {
				return false;
			} else {
				this.startIndex = actualIndex;
				this.lastElementIndex = elementIndex;
				return wrappedElement.isValidOn(startIndex);
			}
		}
		
		protected abstract Integer getActualIndex(int elementIndex);

		@Override
		protected Boolean getState(int elementIndex) {
			if(elementIndex != lastElementIndex)
				throw new InvalidParameterException("Called getState() without calling isValidOn() first");
			return wrappedElement.getState(startIndex);
		}
		
		@Override
		public SvgImage icon() {
			return wrappedElement.icon();
		}
	}

	private class ToggleJoinArcEnd extends IndexAwareActionWrapper {
		public ToggleJoinArcEnd(int svgResId)	throws LoadingSvgException {
			super(new ToggleJoinArc(svgResId));
		}

		@Override
		protected Integer getActualIndex(int elementIndex) {
			if(!isValidIndex(elementIndex))
				return null;
			ElementSpec elementSpec = elementViews.get(elementIndex).model().getElementSpec();
			for(int i = elementIndex-1; i >= 0; i--) {
				elementSpec = elementViews.get(i).model().getElementSpec();
				if(JoinArc.couldStartWithJA(elementSpec)) {
					return i;
				} else if(!JoinArc.canSkipOver(elementSpec)) {
					break;
				}
			}
			return null;
		}
	}
	
	private class ToggleNoteGroup extends UpdateElementAction {

		public ToggleNoteGroup(int svgResId) throws LoadingSvgException {
			super(svgResId);
		}

		@Override
		protected ElementSpec updatedSpec(ElementSpec elementSpec) {
			ElementSpec.NormalNote spec = (NormalNote) elementSpec;
			return elementSpecNN(new NoteSpec(spec.noteSpec(), NoteSpec.TOGGLE_FIELD.IS_GROUPED));
		}

		@Override
		protected boolean isValidOn(int elementIndex) {
			if(!isValidIndex(elementIndex) || !isValidIndex(elementIndex+1))
				return false;
			ElementSpec elementSpec = elementViews.get(elementIndex).model().getElementSpec();
			ElementSpec nextElementSpec = elementViews.get(elementIndex+1).model().getElementSpec();
			return 
			NotesGroup.GroupBuilder.couldExtendGroup(elementSpec)
			&& NotesGroup.GroupBuilder.canEndGroup(nextElementSpec);
		}
		
		@Override
		protected Boolean getState(int elementIndex) {
			return isValidIndex(elementIndex) 
			&& ((ElementSpec.NormalNote) elementViews.get(elementIndex).model().getElementSpec()).noteSpec().isGrouped();
		}
	}
	
	private class ToggleNoteGroupEnd extends IndexAwareActionWrapper {

		public ToggleNoteGroupEnd(int svgResId) throws LoadingSvgException {
			super(new ToggleNoteGroup(svgResId));
		}

		@Override
		protected Integer getActualIndex(int elementIndex) {
			return elementIndex-1;
		}
		
	}
	
	private class ToggleNoteModifier extends UpdateElementAction {
		private NoteModifier modifier;
		
		public ToggleNoteModifier(int svgResId, NoteModifier modifier)
				throws LoadingSvgException {
			super(svgResId);
			this.modifier = modifier;
		}

		@Override
		protected ElementSpec updatedSpec(ElementSpec elementSpec) {
			NoteSpec newSpec = new NoteSpec(((NormalNote) elementSpec).noteSpec());
			if(modifier == newSpec.getToneModifier()) {
				newSpec.clearToneModifier();
			} else {
				newSpec.setToneModifier(modifier);
			}
			return elementSpecNN(newSpec);
		}

		@Override
		protected boolean isValidOn(int elementIndex) {
			if(!isValidIndex(elementIndex))
				return false;
			ElementSpec elementSpec = elementViews.get(elementIndex).model().getElementSpec();
			return elementSpec.getType() == ElementType.NOTE;
		}
		
		@Override
		protected Boolean getState(int elementIndex) {
			return isValidIndex(elementIndex) 
			&& modifier ==
				((ElementSpec.NormalNote) elementViews.get(elementIndex).model().getElementSpec()).noteSpec().getToneModifier()
			;
		}
	}
	
	private class InsertPause extends SvgIconAction {
		private int pauseLength;

		public InsertPause(int svgResId, int pauseLength)
				throws LoadingSvgException {
			super(svgResId);
			this.pauseLength = pauseLength;
		}

		@Override
		protected void perform(int elementIndex) {
			try {
				SheetAlignedElementView newNote = insertElement(elementIndex, spec, rebuildRange);
				updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
				postInsert(elementIndex, rebuildRange);
				spec = null;
			} catch (CreationException e) {
				log.e("Cannot instantiate graphics for pause", e);
				finish();
			}
		}
		
		private Point rebuildRange = new Point();
		private ElementSpec.Pause spec;

		@Override
		protected boolean isValidOn(int elementIndex) {
			if(spec == null) {
				spec = new ElementSpec.Pause(new PauseSpec(pauseLength));
			}
			return willFitInTime(elementIndex, spec);
		}
		
	};
	
	private static final String DIALOGTAG_TIMESTEP = "timestep";
	
	private class AlterTimeStep extends SvgIconAction {

		public AlterTimeStep(int svgResId) throws LoadingSvgException {
			super(svgResId);
		}

		@Override
		protected void perform(int elementIndex) {
			// TODO Auto-generated method stub
			try {
				contextTimeIndex = getTimeIndex(elementIndex);
				DialogFragment newFragment = TimeStepDialog.newInstance(EditActivity.this, 
					times.get(contextTimeIndex).spec.getTimeStep());
			    newFragment.show(getSupportFragmentManager(), DIALOGTAG_TIMESTEP);
			} catch (LoadingSvgException e) {
				log.e("Failed to initialize TimeStep dialog.", e);
				// TODO display some kind of info to user
			}
		}

		protected int getTimeIndex(int elementIndex) {
			return findTimeToInsertTo(elementIndex);
		}

		@Override
		protected boolean isValidOn(int elementIndex) {
			return getTimeIndex(elementIndex) != 0;
		}
		
	}
	
	private abstract class ToggleTimebarMark extends SvgIconAction {
		private AdditionalMark mark;

		public ToggleTimebarMark(int svgResId, AdditionalMark mark)
				throws LoadingSvgException {
			super(svgResId);
			this.mark = mark;
		}

		@Override
		protected void perform(int elementIndex) {
			try {
				int timeIndex = getTimeIndex(elementIndex);
				Time time = times.get(timeIndex);
				if(time.spec.hasMark(mark)) {
					time.spec.removeMark(mark);
				} else {
					time.spec.addMark(mark);
				}
				SheetAlignedElementView pinView = elementViews.get(elementIndex);
				int pinVisiblePositionX = abs2visibleX(viewStableX(pinView) + middleX(pinView));
				int oldAbsMiddleX = viewStableX(time.view) + middleX(time.view);
				time.view.setModel(createDrawingModel(new ElementSpec.TimeDivider(
					timeIndex > 0 ? times.get(timeIndex-1).spec : null,
					time.spec
				)));
				updatePosition(time.view, oldAbsMiddleX - middleX(time.view), sheetElementY(time.view));
				if(timeIndex + 1 < times.size()) {
					Time nextTime = times.get(timeIndex+1);
					oldAbsMiddleX = viewStableX(nextTime.view) + middleX(nextTime.view);
					nextTime.view.setModel(createDrawingModel(new ElementSpec.TimeDivider(
						time.spec,
						nextTime.spec
					)));
					updatePosition(
						nextTime.view, 
						oldAbsMiddleX - middleX(nextTime.view), 
						sheetElementY(nextTime.view)
					);
				}
				if(timeIndex == 0) {
					onFirstTimeDividerChanged();
				}
				animatedRepositioning(
					// TODO not from 0
					0, elementViews.size()-1, 
					elementIndex, 
					pinVisiblePositionX, 
					300
				);				
			} catch (CreationException e) {
				log.e("", e);
				// TODO more gracefully
				finish();
			}
		}
		
		protected abstract int getTimeIndex(int elementIndex);

		@Override
		protected boolean isValidOn(int elementIndex) {
			return isValidIndex(elementIndex);
		}
		
		@Override
		protected Boolean getState(int contextElementIndex) {
			return times.get(getTimeIndex(contextElementIndex)).spec.hasMark(mark);			
		}
		
	}
	
	// TODO persist in onSaveInstance
	private int contextTimeIndex = -1;
	
	@Override
	/**
	 * Result of prompt for new TimeStep for current Time
	 */
	public void onResult(TimeStep enteredValue) {
		int timeIndex = contextTimeIndex;
		if(timeIndex == 0 || timeIndex >= times.size()) {
			log.w("Tried to alter timestep of incorrect time %d", timeIndex);
		} else try {
			Time time = times.get(timeIndex);
			time.spec.setTimeStep(enteredValue);
			int pinVisiblePositionX = abs2visibleX(viewStableX(time.view) + middleX(time.view));
			time.view.setModel(createDrawingModel(new ElementSpec.TimeDivider(
				timeIndex > 0 ? times.get(timeIndex-1).spec : null,
				time.spec
			)));
			updatePosition(time.view, null, sheetElementY(time.view));
			if(timeIndex + 1 < times.size()) {
				Time nextTime = times.get(timeIndex+1);
				nextTime.view.setModel(createDrawingModel(new ElementSpec.TimeDivider(
					time.spec,
					nextTime.spec
				)));
				updatePosition(nextTime.view, null, sheetElementY(nextTime.view));
			}
			int endIndex = elementViews.size()-1;
			clearJoinArcs(0, endIndex);
			clearNoteGroups(0, endIndex);
			rebuildTimes(timeIndex-1);
			endIndex = elementViews.size()-1;
			buildNoteGroups(0, endIndex);
			buildJoinArcs(0, endIndex);
			animatedRepositioning(
				0, elementViews.size()-1, 
				times.get(Math.min(timeIndex, times.size()-1)).rangeStart, 
				pinVisiblePositionX, 
				300
			);
		} catch(CreationException e) {
			log.e("", e);
			// TODO exit gracefully
			finish();
		}
	}
	
	private QuickActionsView qActionsView; 
	private void prepareQuickActions() throws LoadingSvgException {
		qActionsView = (QuickActionsView) findViewById(R.id.EDIT_quickactions);
		qActionsView.setVisibility(View.INVISIBLE);
		possibleActions = new IndexAwareAction[] {
			new TogglePauseDot(R.xml.button_dot),
			new ToggleJoinArcEnd(R.xml.button_joinarc_left),
			new ToggleNoteGroupEnd(R.xml.button_notegroup_left),
			new ToggleNoteDot(R.xml.button_dot),
			new RemoveElementAction(R.xml.button_trash),
			new SvgIconAction(R.xml.button_tonemodifiers) {
				{ 	this.mPostHide = false; }
				@Override
				protected void perform(int elementIndex) {
					if(!isValidIndex(elementIndex))
						throw new InvalidParameterException();
					showElementQuickActions(elementIndex, modifiersActions);
				}
				@Override
				protected boolean isValidOn(int elementIndex) {
					if(!isValidIndex(elementIndex))
						return false;
					SheetAlignedElementView view = elementViews.get(elementIndex);
					return view.model().getElementSpec().getType().equals(ElementType.NOTE);
				}
			},
			new ToggleNoteGroup(R.xml.button_notegroup_right),
			new ToggleJoinArc(R.xml.button_joinarc_right)
		};
		modifiersActions = new IndexAwareAction[] {
			new ToggleNoteModifier(R.xml.flat, NoteModifier.FLAT),
			new ToggleNoteModifier(R.xml.sharp_online, NoteModifier.SHARP),
			new ToggleNoteModifier(R.xml.natural_online, NoteModifier.NATURAL)
		};
		ArrayList<IndexAwareAction> insertActions = new ArrayList<EditActivity.IndexAwareAction>();
		insertActions.add(new SvgIconAction(R.xml.button_timedivider) {
			@Override
			protected void perform(int elementIndex) {
				forceCloseTime(elementIndex);
			}
			@Override
			protected boolean isValidOn(int elementIndex) {
				return 
				(rightToIA >= elementViews.size() || elementViews.get(rightToIA).model().getElementSpec().getType() != ElementType.TIMES_DIVIDER)
				&&
				(rightToIA-1 < 0 || elementViews.get(rightToIA-1).model().getElementSpec().getType() != ElementType.TIMES_DIVIDER);
			}
		});
		insertActions.add(new AlterTimeStep(R.xml.button_timestep));
		TypedArray iconsMapping = getResources().obtainTypedArray(R.array.insertPauseIcons);
		if(iconsMapping.length() < minPossibleValue) {
			log.e("InsertPause svg icons mapping doesn't cover whole range", null);
			finish();
			// TODO handle this gracefully
			return;
		}
		for(int i = 0; i < minPossibleValue; i++) {
			insertActions.add(new InsertPause(iconsMapping.getResourceId(i, 0), i));
		}
		this.insertActions = insertActions.toArray(new IndexAwareAction[0]);
		iconsMapping.recycle();
		
		timedividerActions = new IndexAwareAction[] {
			new ToggleTimebarMark(R.xml.button_timebar_endrepeat, AdditionalMark.END_REPEAT) {
				@Override
				protected int getTimeIndex(int elementIndex) {
					return findTimeToInsertTo(elementIndex);
				}
				@Override
				protected boolean isValidOn(int elementIndex) {
					return super.isValidOn(elementIndex) && getTimeIndex(elementIndex) >= 0;
				}
			},
			new AlterTimeStep(R.xml.qab_button_timestep) {
				@Override
				protected int getTimeIndex(int elementIndex) {
					return findTime(elementIndex);
				}
			},
			new ToggleTimebarMark(R.xml.button_timebar_beginrepeat, AdditionalMark.BEGIN_REPEAT) {
				@Override
				protected int getTimeIndex(int elementIndex) {
					return findTime(elementIndex);
				}
			}
		};
	}
	private View.OnTouchListener quickActionsDismiss = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			hideQuickActionsPopup();
			return false;
		}
	};
	
	private void showQuickActionsAbove(View view, int actionsTargetIndex, IndexAwareAction[] possibleActions) {
		showQuickActions(
			actionsTargetIndex, possibleActions,
			view.getLeft() + view.getWidth()/2,
			view.getTop()
		);
	}
	
	private void showElementQuickActions(int contextElementIndex, IndexAwareAction[] possibleActions) {
		int middleX, middleY;
		SheetAlignedElementView view = elementViews.get(contextElementIndex);
		switch(view.model().getElementSpec().getType()) {
		case NOTE:
			middleX = middleX(view);
			middleY = -view.getOffsetToAnchor(view.model().getElementSpec().positonSpec().positon(), AnchorPart.MIDDLE);
			break;
		case TIMES_DIVIDER:
			middleX = middleX(view);
			middleY = view.measureHeight()/2;
			break;
		default:
			middleX = view.getWidth()/2;
			middleY = view.measureHeight()/2;
		}
		int refPointVisibleX = abs2visibleX(viewStableX(view)) + middleX;
		int refPointVisibleY = abs2visibleY(top(view)) + middleY;
		
		showQuickActions(contextElementIndex, possibleActions, refPointVisibleX, refPointVisibleY);
	}
	
	private void showQuickActions(int contextElementIndex, IndexAwareAction[] possibleActions, int refPointVisibleX, int refPointVisibleY) {
		List<Action> model = new ArrayList<Action>(possibleActions.length);
		for(int i = 0; i < possibleActions.length; i++) {
			IndexAwareAction action = possibleActions[i];
			if(action.isValidOn(contextElementIndex)) {
				model.add(action);
			}
		}
		if(!model.isEmpty()) {
			elementActionIndex = contextElementIndex;
			qActionsView.setModel(model);

			// validate ref point according to visible rect
			refPointVisibleX = Math.min(Math.max(0, refPointVisibleX), visibleRectWidth);
			refPointVisibleY = Math.min(Math.max(0, refPointVisibleY), visibleRectHeight);
			
			// validate ref point y-coordinate according to action bar height
			qActionsView.setIndicatorOrigin(IndicatorOrigin.BOTTOM);
			int height = qActionsView.measureHeight();
			qActionsView.setIndicatorOrigin(IndicatorOrigin.TOP);
			int heightUpdown = qActionsView.measureHeight();
			if(refPointVisibleY - height >= 0) {
				qActionsView.setIndicatorOrigin(IndicatorOrigin.BOTTOM);
			} else if(refPointVisibleY + heightUpdown <= visibleRectHeight) {
				qActionsView.setIndicatorOrigin(IndicatorOrigin.TOP);
			} else {
				qActionsView.setIndicatorOrigin(IndicatorOrigin.NONE);
				refPointVisibleY -= qActionsView.measureHeight()/2;
			}
			
			Rect margins = new Rect();
			// calculate indicator origin horizontal position
			qActionsView.getOriginPostionMargin(margins);
			int mL = margins.left;
			int mR = margins.top;
			int width = qActionsView.measureWidth();
			int defX = mL+(width-mL-mR)/2;
			int originX = defX;
			if(refPointVisibleX < defX && width < visibleRectWidth) {
				originX = Math.max(refPointVisibleX, mL);
			} else if(refPointVisibleX + (width-defX) > visibleRectWidth && width < visibleRectWidth) {
				originX = Math.min(refPointVisibleX - (visibleRectWidth - width), width - mR);
			}
			qActionsView.setOriginX(originX);
			
			qActionsView.measure();
			updateMargins(
				qActionsView,
				refPointVisibleX - originX,
				refPointVisibleY - qActionsView.getIndicatorEndY()
			);
			qActionsView.setVisibility(View.VISIBLE);
		}
	}

	private void hideQuickActionsPopup() {
		qActionsView.setModel(null);
		qActionsView.setVisibility(View.GONE);
		elementActionIndex = -1;
	}
	
	@Override
	public void onBackPressed() {
		if(qActionsView.getVisibility() == View.VISIBLE) {
			hideQuickActionsPopup();
		} else {
			super.onBackPressed();
		}
	}

	private void addOverlayView(final ElementsOverlay overlay) {
		SheetElementView<SheetElement> elementView;
		elementView = new SheetElementView<SheetElement>(this, overlay);
		elementView.setPaint(normalPaint, NOTE_DRAW_PADDING);
		elementView.setSheetParams(sheetParams);
		overlay.setTag(elementView);
		overlaysViews.add(elementView);
		sheet.addView(elementView);
		updateOverlayPosition(overlay, elementView);
		overlay.setObserver(new Observer() {
			@Override
			public void onMeasureInvalidated() {
				// find view
				SheetElementView<SheetElement> ovView = (SheetElementView<SheetElement>) overlay.getTag();
				ovView.invalidateMeasure();
				ovView.invalidate();
				// reposition it
				updateOverlayPosition(overlay, ovView);
				updateSize(ovView, ovView.measureWidth(), ovView.measureHeight());
			}
		});
	}
	
	private SheetAlignedElementView addElementView(int index, SheetAlignedElement model) {
		SheetAlignedElementView elementView;
		elementView = new SheetAlignedElementView(this, model);
		elementView.setPaint(normalPaint, NOTE_DRAW_PADDING);
		elementView.setSheetParams(sheetParams);
		elementViews.add(index, elementView);
		sheet.addView(elementView);
		return elementView;
	}
	
	private void removeElementView(SheetAlignedElementView view) {
		removeElementView(view, true);
	}
	private void removeElementView(SheetAlignedElementView view, boolean assertRemoved) {
		boolean removed = elementViews.remove(view);
		if(assertRemoved && !removed) {
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
	
	private ElementSpec.NormalNote elementSpecNN(NoteSpec spec) {
		int orientation;
		DisplayMode mode = sheetParams.getDisplayMode();
		switch (mode) {
		case LOWER_VOICE:
			orientation = NoteConstants.ORIENT_DOWN;
			break;
		case UPPER_VOICE:
			orientation = NoteConstants.ORIENT_UP;
			break;
		case NORMAL:
			orientation = NoteConstants.defaultOrientation(spec.positon());
			break;
		default:
			throw CodeLogicError.unhandledEnumValue(mode);
		}
		return new ElementSpec.NormalNote(spec, orientation);
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
	
	private ElementSpec specAt(int elementIndex) {
		return elementViews.get(elementIndex).model().getElementSpec();
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
		return line0Top() + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE);
	}
	
	private int line0Top() {
		return top(lines) + lines.getPaddingTop();
	}

	private void highlightAnchor(Integer anchorAbsIndex) {
		lines.highlightAnchor(anchorAbsIndex);
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
		dispatchPositionChanged(overlay, view);
	}
	
	private static int left(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
	}
	private static int top(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
	}

	private void updatePosition(View v, Integer left, Integer top) {
		updateMargins(v, left, top);
		if(v instanceof SheetAlignedElementView) {
			SheetAlignedElementView view = (SheetAlignedElementView) v;
			Set<ElementsOverlay> overlays = bindMap.get(view);
			if(overlays != null) {
				for(ElementsOverlay ov: overlays) {
					dispatchPositionChanged(ov, view);
				}
			}
		}
	}
	
	private void dispatchPositionChanged(ElementsOverlay overlay, SheetAlignedElementView view) {
		overlay.positionChanged(
			view.model(), 
			left(view) + view.getPaddingLeft(), 
			top(view) + view.getPaddingTop() - line0Top()
		);
	}

	private int moveLeftBorder() {
		return visibleRectWidth - iaRightMargin + delta - mTouchSlop;
	}


	private int moveRightBorder() {
		return visibleRectWidth - inputAreaWidth - iaRightMargin - delta + mTouchSlop;
	}
	
	private int abs2visibleX(int absoluteX) {
		return absoluteX - hscroll.getScrollX();
	}
	
	private int visible2absX(int visibleX) {
		return visibleX + hscroll.getScrollX();
	}

	/**
	 * @param absoluteY y-axis value in "sheet" ViewGroup coordinates
	 * @return y-axis value in "vertscroll" View coordinates
	 */
	private int abs2visibleY(int absoluteY) {
		return absoluteY + sheet.getTop() - vertscroll.getScrollY();
	}
	
	private int inIA_noteViewX(SheetAlignedElementView noteView) {
		return visible2absX(visibleRectWidth-iaRightMargin-inputAreaWidth/2)-middleX(noteView);
	}

	private void updateOverlayPosition(ElementsOverlay overlay, SheetElementView<SheetElement> ovView) {
		updatePosition(ovView, overlay.left()-ovView.getPaddingLeft(), line0Top() + overlay.top()-ovView.getPaddingTop());
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