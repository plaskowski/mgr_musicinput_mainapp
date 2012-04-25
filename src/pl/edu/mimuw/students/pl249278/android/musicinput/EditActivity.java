package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.middleX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.timeCapacity;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE4_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateMargins;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateSize;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.async.AsyncHelper;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.InsertDivided;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.Score.ParcelableScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentElem;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.SerializationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.AsyncServiceToastReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.ContentService;
import pl.edu.mimuw.students.pl249278.android.musicinput.services.FilterByRequestIdReceiver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Action;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware.IndicatorOrigin;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TimeStepDialog;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_ShowScore;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay;
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
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutAnimator;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner.OnValueChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.QuickActionsView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewUtils.OnLayoutListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptableOnScrollChanged.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.InterceptsScaleGesture;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.ScrollingLockable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature.TouchInputLockable;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.Toast;

public class EditActivity extends FragmentActivity_ErrorDialog_ShowScore implements TimeStepDialog.OnPromptResult {
	private static LogUtils log = new LogUtils(EditActivity.class);
	protected static final int SPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);
	/** of type long */
	public static final String STARTINTENT_EXTRAS_SCORE_ID = EditActivity.class.getCanonicalName()+".score_id";
	private static final int ANIM_TIME = 150;
	
	private static final String INSTANCE_STATE_SCORE = "state_score";
	private static final String INSTANCE_STATE_VISUALCONF = "state_visualconf";
	private static final String INSTANCE_STATE_RIGHT_TO_IA = "right2ia";
	/** 
	 * Instance key for {@link #rightToIAsavedPosition}.
	 * Required to restore horizontal scroll position.
	 */
	private static final String INSTANCE_STATE_RIGHT_TO_IA_POSITION = "right2iaPos";
	private static final String INSTANCE_STATE_SCALE = "scale";
	/** 
	 * Instance key for {@link #contextTimeIndex}.
	 * Required to persist context of displayed TimestepDialog during configuration restart.
	 */
	private static final String INSTANCE_STATE_CONTEXT_TIME_INDEX = "ctxTime";
	/** Instance key for {@link #currentNoteLength} */
 	private static final String INSTANCE_STATE_CURRENT_NOTE_LENGTH = "newNoteLength";
	
	private static final String CALLBACK_ACTION_GET = EditActivity.class.getName()+".callback_get";
	
	private int NOTE_DRAW_PADDING = 0;
	protected Paint noteHighlightPaint = new Paint();
	protected Paint fakePausePaint = new Paint();
	protected Paint normalPaint = new Paint();
	{
		normalPaint.setAntiAlias(true);
		noteHighlightPaint.setAntiAlias(true);
		fakePausePaint.setAntiAlias(true);
	}
	
	private ViewGroup sheet;
	private View inputArea;
	private HorizontalScrollView hscroll;
	private ScrollView vertscroll;
	private ViewGroup scaleGestureDetector;
	private Animator animator = new EditActivity.Animator(this);
	private ProgressDialog progressDialog;	
	private QuickActionsView qActionsView; 
	
	private boolean isScaleValid = false;
	private ArrayList<Time> times = new ArrayList<EditActivity.Time>();
	private Score score = null;
	private ScoreVisualizationConfig visualConf = null;
	private boolean skipOnStopCopy = false;	
	
	/**
	 * Index (in elementViews) of element that is first on right side of InputArea,
	 * when there is no such element rightToIA = elementViews.size()
	 */
	private int rightToIA = Integer.MAX_VALUE;
	/** elementViews[rightToIA].middle - moveLeftBorder() value before restart
	 * @see #INSTANCE_STATE_RIGHT_TO_IA_POSITION
	 */
	private int rightToIAsavedPosition;
	
	/** index of Time which {@link TimeStepDialog} operates on */
	private int contextTimeIndex = -1;

	/** length attribute for new note being inserted, provided by {@link NoteValueSpinner} */
	protected int currentNoteLength = 0;
	
	private int iaRightMargin;
	private int delta;
	private int mTouchSlop;
	protected boolean isScaling = false, isPositioning = false;
	private int visibleRectWidth;
	private int visibleRectHeight;
	private int notesAreaX;
	
	private int inputAreaWidth;
	private float noteMinDistToIA;
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
		
		public Time(TimeSpec spec, SheetAlignedElementView view) {
			this.spec = spec;
			this.view = view;
		}
		
		public boolean isFull() {
			return capLeft == 0;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscreen);

		scaleGestureDetector = (ViewGroup) findViewById(R.id.EDIT_scale_detector);
		scaleGestureDetector.setOnTouchListener(quickActionsDismiss);
		hscroll = (HorizontalScrollView) findViewById(R.id.EDIT_outer_hscrollview);
		vertscroll = (ScrollView) findViewById(R.id.EDIT_vertscrollview);
		sheet = (ViewGroup) findViewById(R.id.EDIT_sheet_container);
		lines = (Sheet5LinesView) findViewById(R.id.EDIT_sheet_5lines);
		((HackedScrollViewChild) vertscroll.getChildAt(0)).setRuler(lines);
		int hColor = getResources().getColor(R.color.highlightColor);
		lines.setHiglightColor(hColor);
		noteHighlightPaint.setColor(hColor);
		this.inputArea = findViewById(R.id.EDIT_inputArea);
		
		this.inputAreaWidth = getResources().getDimensionPixelSize(R.dimen.inputAreaWidth);
		ViewConfiguration configuration = ViewConfiguration.get(this);
        this.mTouchSlop = configuration.getScaledTouchSlop();
		noteShadow = readParametrizedFactor(R.string.noteShadow);
		fakePauseEffectRadius = readParametrizedFactor(R.string.fakePauseEffectRadius);
		noteMinDistToIA = readParametrizedFactor(R.string.minDistToIA);
		maxLinespaceThickness = getResources().getDimensionPixelSize(R.dimen.maxLinespaceThickness);
		
		NoteValueSpinner valueSpinner = (NoteValueSpinner) findViewById(R.id.EDIT_note_value_scroll);
		try {
			int newNoteLength = savedInstanceState == null ? 
				-1 : savedInstanceState.getInt(INSTANCE_STATE_CURRENT_NOTE_LENGTH, -1);
			if(newNoteLength == -1) {
				valueSpinner.setupNoteViews(sheetParams);
			} else {
				valueSpinner.setupNoteViews(sheetParams, newNoteLength);
			}
			currentNoteLength = valueSpinner.getCurrentValue();
		} catch (CreationException e) {
			log.e("Failed to setup spinner", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		}
		
		if(savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_STATE_SCORE)) {
			ParcelableScore parcelable = savedInstanceState.getParcelable(INSTANCE_STATE_SCORE);
			score = parcelable.getSource();
			visualConf = savedInstanceState.getParcelable(INSTANCE_STATE_VISUALCONF);
			rightToIA = savedInstanceState.getInt(INSTANCE_STATE_RIGHT_TO_IA);
			if(savedInstanceState.containsKey(INSTANCE_STATE_SCALE)) {
				sheetParams.setScale(savedInstanceState.getFloat(INSTANCE_STATE_SCALE));
				isScaleValid = true;
			}
			rightToIAsavedPosition = savedInstanceState.getInt(INSTANCE_STATE_RIGHT_TO_IA_POSITION);
			contextTimeIndex = savedInstanceState.getInt(INSTANCE_STATE_CONTEXT_TIME_INDEX);
			onModelLoaded();
		} else {
			long scoreId = getIntent().getLongExtra(STARTINTENT_EXTRAS_SCORE_ID, -1);
			if(scoreId == -1) {
				log.e("Activity started without score id in intent");
				showErrorDialog(R.string.errormsg_unrecoverable, null, true);
				return;
			}
			// sending request for Score object
			GetScoreReceiver getScoreReceiver = new GetScoreReceiver();	
			PendingIntent callbackIntent = PendingIntent.getBroadcast(this, 0, new Intent(CALLBACK_ACTION_GET), 0);
			Intent requestIntent = AsyncHelper.prepareServiceIntent(
				this, 
				ContentService.class, 
				ContentService.ACTIONS.GET_SCORE_BY_ID, 
				getScoreReceiver.getUniqueRequestID(true), 
				callbackIntent, 
				true
			);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, scoreId);
			requestIntent.putExtra(ContentService.ACTIONS.EXTRAS_ATTACH_SCORE_VISUAL_CONF, true);
			registerReceiver(getScoreReceiver, new IntentFilter(CALLBACK_ACTION_GET));
        	log.v("Sending GET_SCORE_BY_ID for id "+scoreId);
        	startService(requestIntent);
        	progressDialog = ProgressDialog.show(this, "", 
    			getString(R.string.msg_loading_please_wait), true);
		}
	}
	
	private class GetScoreReceiver extends FilterByRequestIdReceiver {
		@Override
		protected void onFailure(Intent response) {
			log.e("Failed to get score: " + AsyncHelper.getError(response));
			unregisterReceiver(this);
			progressDialog.dismiss();
			progressDialog = null;
			showErrorDialog(R.string.errormsg_unrecoverable, null, true);
		}
		
		@Override
		protected void onSuccess(Intent response) {
			unregisterReceiver(this);
			ParcelableScore parcelable = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_ENTITY);
			score = parcelable.getSource();
			visualConf = response.getParcelableExtra(ContentService.ACTIONS.RESPONSE_EXTRAS_VISUAL_CONF);
			onModelLoaded();
		}
	}
	
	@Override
	protected void lazyFinishCleanup() {
		skipOnStopCopy = true;
		super.lazyFinishCleanup();
	}
	
	@Override
	protected void onStop() {
		if(score != null && !skipOnStopCopy) {
			// parse Score::content from model
			score.setContent(parseModifiedCotentModel());
			// save a working copy
			Intent request = AsyncHelper.prepareServiceIntent(
				this, ContentService.class, 
				ContentService.ACTIONS.SAVE_SCORE_COPY, 
				null, null, false);
			try {
				request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE, score.prepareParcelable());
				request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE_VISUAL_CONF, visualConf);
				log.v("Sending request SAVE_SCORE_COPY of Score#%d", score.getId());
				startService(request);
			} catch (SerializationException e) {
				log.e("Failed to serialize model", e);
			}
		}
		
		super.onStop();
	}
	
	private static final int MENU_SAVE = 1;
	private static final int MENU_SAVE_AND_CLOSE = 2;
	private static final int MENU_DISCARD = 3;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, R.string.menu_label_save);
		menu.add(Menu.NONE, MENU_SAVE_AND_CLOSE, Menu.NONE, R.string.menu_label_save_exit);
		menu.add(Menu.NONE, MENU_DISCARD, Menu.NONE, R.string.menu_label_discard_changes);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
		case MENU_SAVE:
			saveChanges();
			break;
		case MENU_SAVE_AND_CLOSE:
			saveChanges();
			skipOnStopCopy = true;
			finish();
			break;
		case MENU_DISCARD:
			sendCleanCopy();
			skipOnStopCopy = true;
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void sendCleanCopy() {
		if(score != null) {
			Intent request = AsyncHelper.prepareServiceIntent(
				this, ContentService.class, 
				ContentService.ACTIONS.CLEAN_SCORE_SESSION_COPY, 
				null, null, false);
			request.putExtra(ContentService.ACTIONS.EXTRAS_ENTITY_ID, score.getId());
			startService(request);
		}
	}
	
	private void saveChanges() {
		if(score == null) {
			Toast.makeText(this, "Score not loaded yet.", Toast.LENGTH_SHORT).show();
			log.v("Trying to save changes when score is null");
		} else {
			score.setContent(parseModifiedCotentModel());
			String scoreTitle = score.getTitle();
			if(scoreTitle == null) {
				scoreTitle = getString(android.R.string.untitled);
			}
			PendingIntent callback = AsyncServiceToastReceiver.prepare(
				this, 
				getString(R.string.toast_score_saved, scoreTitle), 
				getString(R.string.toast_failed_to_save_score, scoreTitle), 
				false
			);
			Intent request = AsyncHelper.prepareServiceIntent(
				this, ContentService.class, 
				ContentService.ACTIONS.UPDATE_SCORE, 
				null, callback, false);
			try {
				request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE, score.prepareParcelable());
				request.putExtra(ContentService.ACTIONS.EXTRAS_SCORE_VISUAL_CONF, visualConf);
				log.v("Sending request UPDATE of Score#%d", score.getId());
				startService(request);
			} catch (SerializationException e) {
				showErrorDialog(R.string.errormsg_unrecoverable, e, true);
				log.e("Failed to serialize model", e);
			}
		}
	}
	
	private void onModelLoaded() {
		try {
			List<ScoreContentElem> content = score.getContent();
			for(ScoreContentElem el: content) {
				ElementSpec spec;
				if(el instanceof TimeSpec) {
					TimeSpec timeSpec = (TimeSpec) el;
					spec = new ElementSpec.TimeDivider(
						times.size() - 1 >= 0 ? times.get(times.size()-1).spec : null, 
						timeSpec
					);
				} else if(el instanceof NoteSpec) {
					spec = elementSpecNN((NoteSpec) el);
				} else if(el instanceof PauseSpec) {
					spec = new ElementSpec.Pause((PauseSpec) el, false);
				} else {
					log.e("Unsupported score content element " + el);
					showErrorDialog(R.string.errormsg_unrecoverable, null, true);
					return;
				}
				SheetAlignedElementView view = addElementView(elementViews.size(), createDrawingModel(spec));
				if(el instanceof TimeSpec) {
					times.add(new Time((TimeSpec) el, view));
				}
			}
			// build times
			rebuildTimes(0);
			// rebuild overlays
			int size = elementViews.size();
			buildNoteGroups(0, size-1);
			buildJoinArcs(0, size-1);
		} catch (CreationException e) {
			log.e("Failed to initialize", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		} catch (SerializationException e) {
			log.e("Failed to initialize", e);
			showErrorDialog(R.string.errormsg_unrecoverable, e, true);
			return;
		}
		
		rightToIA = Math.min(rightToIA, elementViews.size());
		
		setupListeners();
		ViewUtils.addActivityOnLayout(this, new OnLayoutListener() {
			@Override
			public void onFirstLayoutPassed() {
				log.v("onGlobalLayout() >> HSCROLL %dx%d", hscroll.getWidth(), hscroll.getHeight());
				onContainerResize(hscroll.getWidth(), hscroll.getHeight());
			}
		});
		hscroll.requestLayout();
		if(progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(score != null) {
			score.setContent(parseModifiedCotentModel());
			try {
				outState.putParcelable(INSTANCE_STATE_SCORE, score.prepareParcelable());
			} catch (SerializationException e) {
				throw new RuntimeException(e);
			}
			outState.putParcelable(INSTANCE_STATE_VISUALCONF, visualConf);
			outState.putInt(INSTANCE_STATE_RIGHT_TO_IA, rightToIA);
			if(isScaleValid) {
				outState.putFloat(INSTANCE_STATE_SCALE, sheetParams.getScale());
			}
			if(rightToIA < elementViews.size()) {
				SheetAlignedElementView view = elementViews.get(rightToIA);
				int middleX = viewStableX(view) + middleX(view);
				outState.putInt(INSTANCE_STATE_RIGHT_TO_IA_POSITION, abs2visibleX(middleX) - moveLeftBorder());
			}
			outState.putInt(INSTANCE_STATE_CONTEXT_TIME_INDEX, contextTimeIndex);
			outState.putInt(INSTANCE_STATE_CURRENT_NOTE_LENGTH, currentNoteLength);
		}
		super.onSaveInstanceState(outState);
	}

	private List<ScoreContentElem> parseModifiedCotentModel() {
		// collect new Score content
		List<ScoreContentElem> content = new ArrayList<ScoreContentElem>(elementViews.size());
		for(SheetAlignedElementView view: elementViews) {
			ElementSpec spec = view.model().getElementSpec();
			switch(spec.getType()) {
			case FAKE_PAUSE:
				continue;
			case NOTE:
				content.add(((ElementSpec.NormalNote) spec).noteSpec());
				break;
			case PAUSE:
				content.add(((ElementSpec.Pause) spec).pauseSpec());
				break;
			case SPECIAL_SIGN:
				// TODO implement or remove
				throw new UnsupportedOperationException();
			case TIMES_DIVIDER:
				content.add(((ElementSpec.TimeDivider) spec).getRightTime());
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		return content;
	}

	
	private void setupListeners() {
		hscroll.setOnTouchListener(quickActionsDismiss);
		vertscroll.setOnTouchListener(quickActionsDismiss);
		((InterceptsScaleGesture) scaleGestureDetector).setOnScaleListener(scaleListener);
		sheet.setOnTouchListener(new CompoundTouchListener(
			quickActionsDismiss,
			iaTouchListener,
			noteTouchListener,
			elementTouchListener
		));
		// setup noteValue spinner
		NoteValueSpinner valueSpinner = (NoteValueSpinner) findViewById(R.id.EDIT_note_value_scroll);
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
		try {
			prepareQuickActions();
		} catch (LoadingSvgException e) {
			e.printStackTrace();
			finish();
			return;
		}
		((InterceptableOnScrollChanged) hscroll).setListener(horizontalScrollListener);
		findViewById(R.id.EDIT_menu_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openOptionsMenu();
			}
		});
	}
	
	// TODO co jak nie ma ustalonego metrum?
	// TODO co jak zmienia się metrum? nie powienienem przepychać nuty do istniejących taktów tylko stworzyć nowy takt starego metrum
	
	private void rebuildTimes(int startTimeIndex) throws CreationException {
//		log.i("rebuildTimes(%d)", startTimeIndex);
		
		// TODO fix logic when no metrum
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
				if(currentMetrum == null) {
					timeIndex++;
				}
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
					insertDivdiedNote.insertDivided(i, capLeft, note.noteSpec(), true);
					insertDivdiedNote.insertDivided(
						i+insertDivdiedNote.getTotal(), 
						timeValue - capLeft, note.noteSpec(), false);
					continue;
				}
			} 
			else {
				fillWithFakePauses.insertDivided(i, currentTime.capLeft, true, minPossibleValue);
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
			insertElementAtIA(
				atIndex, 
				new ElementSpec.Pause(pause), 
				getTotal() != 1 ? null : rebuildRange
			);
		}
	}
	private FillWithPauses fillWithPauses = new FillWithPauses();
	
	private class InsertDividedNote extends InsertDivided {
		private NoteSpec template;
		private List<NoteSpec> specs = new ArrayList<NoteSpec>();

		void insertDivided(int insertIndex, int capToFill, NoteSpec template, boolean addJoinArcAtEnd) throws CreationException {
			this.template = template;
			specs.clear();
			super.insertDivided(insertIndex, capToFill, false, minPossibleValue);
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
	private InsertDividedNote insertDivdiedNote = new InsertDividedNote();
	
	private Time rebuildTime(int timeIndex, int newRangeStart, TimeStep prevMetrum) throws CreationException {
		Time currentTime;
		if(timeIndex >= times.size()) {
			TimeSpec timeSpec = new TimeSpec();
			currentTime = new Time(
				timeSpec,
				addElementView(newRangeStart, createDrawingModel(new ElementSpec.TimeDivider(
					timeIndex > 0 ? times.get(timeIndex-1).spec : null,
					timeSpec
				)))
			);
			times.add(timeIndex, currentTime);
			updatePosition(currentTime.view, positionAfter(newRangeStart-1), sheetElementY(currentTime.view));
		} else {
			currentTime = times.get(timeIndex); 
			int prevIndex = elementViews.indexOf(currentTime.view);
			if(prevIndex >= 0) {
				if(prevIndex < newRangeStart) {
					throw new RuntimeException(String.format(
						"time[%d].view should not be at %d that is lower that newRangeStart %d",
						timeIndex, prevIndex, newRangeStart
					));
				}
				elementViews.remove(prevIndex);
			}
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
		log.d("elementViews(#%d) { "+str.toString()+" }", elementViews.size());
	}
	private void debugTimes() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < times.size(); i++) {
			str.append(times.get(i).rangeStart+", ");
		}
		log.d("times(#%d) { "+str.toString()+" }", times.size());
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
	
	protected void buildNoteGroups(int startIndex, int endIndex) throws CreationException {
		super.buildNoteGroups(startIndex, endIndex, sheet, normalPaint, NOTE_DRAW_PADDING);
	}
	
	protected void buildJoinArcs(int startIndex, int endIndex) throws CreationException {
		super.buildJoinArcs(startIndex, endIndex, sheet, normalPaint, NOTE_DRAW_PADDING);
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
				log.v(
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

	private int insertElementAtIA(int insertIndex, ElementSpec spec, Point rebuildRange) throws CreationException {
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
		
		int lastEl = elementViews.size() - 1;
		/** actual index may have changed because of removed FAKE pauses */
		if(insertIndex > lastEl || !elementViews.get(insertIndex).equals(newElement)) {
			insertIndex = elementViews.indexOf(newElement);
		}
		int ngRebuildIndex = findPossibleNoteGroupStart(insertIndex);
		int jaRebuildIndex = findPossibleJoinArcStart(ngRebuildIndex);
		log.v("insertElement() jaRebuildIndex = %d, ngRebuildIndex = %d", jaRebuildIndex, ngRebuildIndex);
		clearJoinArcs(jaRebuildIndex, lastEl);
		clearNoteGroups(ngRebuildIndex, lastEl);
		buildNoteGroups(ngRebuildIndex, lastEl);
		buildJoinArcs(jaRebuildIndex, lastEl);
		
		updatePosition(newElement, inIA_noteViewX(newElement), sheetElementY(newElement));
		updatePositions(insertIndex+1, lastEl, visible2absX(visibleRectWidth-iaRightMargin+delta));
		if(rebuildRange != null) rebuildRange.set(jaRebuildIndex, lastEl);
		debugViews();
		return insertIndex;
	}
	
	/**
	 * @return index of start of possible NoteGroup that could span up to given index
	 */
	private int findPossibleNoteGroupStart(int index) {
		int i = index - 1;
		for(;i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
			if(!NotesGroup.GroupBuilder.canExtendGroup(elementSpec)) {
				break;
			}
		}
		return Math.max(i, 0);
	}
	
	/**
	 * @return index of start of possible JoinArc that could span up to given index
	 */
	private int findPossibleJoinArcStart(int index) {
		int i = index - 1;
		for(; i > 0; i--) {
			ElementSpec elementSpec = specAt(i);
			if(JoinArc.canEndJA(elementSpec) || JoinArc.canStrartJA(elementSpec) || !JoinArc.canSkipOver(elementSpec)) {
				break;
			}
			                                                                        
		}
		return Math.max(i, 0);		
	}
	
	private void insertNewTime(int newTimeIndex, int timeDividerIndex) throws CreationException {
		assert newTimeIndex > 0;
		int ngRebuildIndex = findPossibleNoteGroupStart(timeDividerIndex);
		int jaRebuildIndex = findPossibleJoinArcStart(ngRebuildIndex);
		int lastEl = elementViews.size() - 1;
		clearJoinArcs(jaRebuildIndex, lastEl);
		clearNoteGroups(ngRebuildIndex, lastEl);
		
		TimeSpec newTime = new TimeSpec();
		ElementSpec spec = new ElementSpec.TimeDivider(times.get(newTimeIndex-1).spec, newTime);
		SheetAlignedElementView newElement = addElementView(timeDividerIndex, createDrawingModel(spec));
		times.add(newTimeIndex, new Time(newTime, newElement));
		rebuildTimes(newTimeIndex-1);
		recreateTimeDivider(newTimeIndex+1);
		
		lastEl = elementViews.size() - 1;
		buildNoteGroups(ngRebuildIndex, lastEl);
		buildJoinArcs(jaRebuildIndex, lastEl);
		
		updatePosition(newElement, inIA_noteViewX(newElement), sheetElementY(newElement));
		updatePositions(timeDividerIndex+1, lastEl, visible2absX(visibleRectWidth-iaRightMargin+delta));
		postInsert(timeDividerIndex, new Point(jaRebuildIndex, lastEl));
	}	

	/**
	 * @return index of the rightmost time, which {@link Time#rangeStart} < insertIndex
	 */
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
		
		int ngRebuildIndex = findPossibleNoteGroupStart(elementIndex);
		int jaRebuildIndex = findPossibleJoinArcStart(ngRebuildIndex);
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
		
		log.v(
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
		int ngRebuildIndex = findPossibleNoteGroupStart(elementIndex);
		int jaRebuildIndex = findPossibleJoinArcStart(ngRebuildIndex);
		
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
		TimeStep currentTimeStep = getCurrentTimeStep(timeIndex);
		try {
			if(currentTimeStep == null) {
				insertNewTime(timeIndex+1, insertIndex);
				return;
			}
			int capToFill = timeCapacity(currentTimeStep, minPossibleValue);
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
			fillWithPauses.insertDivided(insertIndex, capToFill, false, minPossibleValue);
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
				if(activePointerId != event.getPointerId(event.getActionIndex()))
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
					currentAnchor = nearestAnchor(downCoords.y);
					ElementSpec.NormalNote newNoteSpec = elementSpecNN(new NoteSpec(currentNoteLength, currentAnchor));
					if(!willFitInTime(insertIndex, newNoteSpec)) {
						disrupt(R.string.EDIT_msg_inserterror_notetolong);
						downPointerId = INVALID_POINTER;
						return;
					}
					highlightAnchor(currentAnchor);
					try {
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
						insertIndex = insertElementAtIA(insertIndex, newNoteSpec, rebuildRange);
						rightToIA = insertIndex+1;
						elementViews.get(insertIndex).setPaint(noteHighlightPaint, NOTE_DRAW_PADDING);
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
				postInsertClean();
				return true;
			}
			cancel();
			return false;
		}

		private void postInsertClean() {
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
				log.v("iaTouchListener::cancel() insert reverted");
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
			NoteConstants.anchorIndex(visualConf.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
			),
			NoteConstants.anchorIndex(visualConf.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
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
			ElementSpec specAt = specAt(i);
			if(specAt.getType() != ElementType.FAKE_PAUSE) {
				capLeft -= specAt.timeValue(minPossibleValue);
			}
		}
		return spec.timeValue(minPossibleValue) <= capLeft;
	}
	
	private TimeStep getCurrentTimeStep(int timeIndex) {
		TimeStep result = null, curr;
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
	
	private InterceptsScaleGesture.OnScaleListener scaleListener = new InterceptsScaleGesture.OnScaleListener() {
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
			setTouchInputLocked(true);
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
			setTouchInputLocked(false);
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
				setTouchInputLocked(false);
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
		
		setTouchInputLocked(true);
	}
	
	/**
	 * @return index of the rightmost time, which {@link Time#rangeStart} <= insertIndex
	 */
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
			startAnimation(anim);
		}
		public void startHScrollAnimation(HorizontalScrollView hscrollView, int scrollDelta, long duration, Runnable onAnimationEndListener) {
			HScrollAnimation anim = new HScrollAnimation(hscrollView, hscrollView.getScrollX(), scrollDelta, duration, onAnimationEndListener);
			startAnimation(anim);
		}
	}
	
	protected void onContainerResize(int visibleRectWidth, int visibleRectHeight) {
		if(visibleRectWidth == this.visibleRectWidth && visibleRectHeight == this.visibleRectHeight)
			return;
		this.visibleRectWidth = visibleRectWidth;
		this.visibleRectHeight = visibleRectHeight;
		
		iaRightMargin = ((View) inputArea.getParent()).getWidth() - inputArea.getRight();
		
		float scale;
		if(!isScaleValid) {
			// calculate default scale so spaces/lines (from space -1 to space 5) fit visible height
			scale = ((float) (visibleRectHeight)) / ((float) (
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
			isScaleValid = true;
		} else {
			scale = sheetParams.getScale();
		}
		updateScaleFactor(scale, false);
		
		int linesHalf = sheetParams.anchorOffset(NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE), AnchorPart.MIDDLE);
		fixLine0VisibleY((visibleRectHeight/2) - linesHalf);
		
    	// scroll according to rightToIA
		final int startScrollX;
		if(rightToIA >= elementViews.size()) {
			startScrollX = declaredWidth(sheet);
		} else {
			startScrollX = middleAbsoluteX(elementViews.get(rightToIA)) - (moveLeftBorder() + rightToIAsavedPosition);
		}
		hscroll.post(new Runnable() {
		    @Override
		    public void run() {
				hscroll.scrollTo(startScrollX, 0);
				sheet.setVisibility(View.VISIBLE);
		    } 
		});
	}
	
	private void onFirstTimeDividerChanged() {
		int paddingLeft = 
		Math.max(
			(int) (notesAreaHorizontalPaddingFactor * sheetParams.getScale()) + middleX(elementViews.get(0)),
			// assure that when sheet is scrolled to start IA left edge matches start of area where notes are placed
			visibleRectWidth-inputAreaWidth-iaRightMargin + mTouchSlop - timeDividerSpacing(times.get(0), true)
		);
		lines.setPaddingLeft(paddingLeft);
		this.notesAreaX = paddingLeft;
	}
	
	private void fixLine0VisibleY(int visY) {
		((HackedScrollViewChild) vertscroll.getChildAt(0)).fixRulerVisibleY(visY - lines.getPaddingTop());
	}

	private void updateScaleFactor(float newScaleFactor) {
		updateScaleFactor(newScaleFactor, true);
	}
	private void updateScaleFactor(float newScaleFactor, boolean ignoreRightToIA) {
		log.d("newScaleFactor: %f", newScaleFactor);
		sheetParams.setScale(newScaleFactor);
		onScaleChanged();
		NOTE_DRAW_PADDING = (int) (noteShadow * sheetParams.getScale());
		noteHighlightPaint.setShadowLayer(NOTE_DRAW_PADDING, NOTE_DRAW_PADDING/2, NOTE_DRAW_PADDING, Color.BLACK);		
		fakePausePaint.setMaskFilter(new BlurMaskFilter(fakePauseEffectRadius*sheetParams.getScale(), Blur.OUTER));
		NOTE_DRAW_PADDING = (int) Math.max(fakePauseEffectRadius*sheetParams.getScale(), NOTE_DRAW_PADDING);
		NOTE_DRAW_PADDING = Math.max(MIN_DRAW_SPACING, NOTE_DRAW_PADDING);
		delta = (int) (sheetParams.getScale()*noteMinDistToIA);
		log.d("updateScaleFactor(%f): delta = %d", newScaleFactor, delta);
		// <!-- correct "5 lines" View to assure that min/maxSpaceAnchor is visible
		int minLinespaceTopOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(visualConf.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE), 
			AnchorPart.TOP_EDGE
		);
		int maxLinespaceBottomOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(visualConf.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE),
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
			if(!ignoreRightToIA && i == rightToIA) {
				x += moveDistance();
			}
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
		int lastEl = (timeIndex + 1 < times.size() ? times.get(timeIndex+1).rangeStart : elementViews.size()) - 1;
		time.spacingBase = computeTimeSpacingBase(time.rangeStart, lastEl, refreshSheetParams);
	}
	
	private void setVerticalScrollingLocked(boolean verticalScrollingLocked) {
		((ScrollingLockable) vertscroll).setScrollingLocked(verticalScrollingLocked);
	}
	
	private void setTouchInputLocked(boolean setLocked) {
		((TouchInputLockable) scaleGestureDetector).setTouchInputLocked(setLocked);
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
				elementIndex = insertElementAtIA(elementIndex, spec, rebuildRange);
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
			return getTimeIndex(elementIndex) >= 0;
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
			// TODO przerobic na updateElementSpec()
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
				recreateTimeDivider(timeIndex);
				recreateTimeDivider(timeIndex+1);
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
	
	@Override
	/**
	 * Result of prompt for new TimeStep for current Time
	 */
	public void onResult(TimeStep enteredValue) {
		int timeIndex = contextTimeIndex;
		if(timeIndex >= times.size()) {
			log.w("Tried to alter timestep of incorrect time %d", timeIndex);
		} else try {
			// TODO przerobić na updateElementSpec()
			Time time = times.get(timeIndex);
			time.spec.setTimeStep(enteredValue);
			int pinVisiblePositionX = abs2visibleX(viewStableX(time.view) + middleX(time.view));
			recreateTimeDivider(timeIndex);
			recreateTimeDivider(timeIndex+1);
			if(timeIndex == 0) {
				onFirstTimeDividerChanged();
			}
			int endIndex = elementViews.size()-1;
			clearJoinArcs(0, endIndex);
			clearNoteGroups(0, endIndex);
			rebuildTimes(Math.max(timeIndex-1, 0));
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
			skipOnStopCopy = true;
			sendCleanCopy();
			super.onBackPressed();
		}
	}

	protected void addOverlayView(ElementsOverlay overlay) {
		super.addOverlayView(overlay, sheet, normalPaint, NOTE_DRAW_PADDING);
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
	
	private ElementSpec.NormalNote elementSpecNN(NoteSpec spec) {
		return ScoreHelper.elementSpecNN(spec, visualConf);
	}

	private int afterElementSpacing(Time time, SheetAlignedElement sheetAlignedElement) {
		return afterElementSpacing(time.rangeStart, time.spacingBase, sheetAlignedElement);
	}
	
	private int timeDividerSpacing(Time time, boolean updateSheetParams) {
		return timeDividerSpacing(time.rangeStart, updateSheetParams);
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
	
	private void highlightAnchor(Integer anchorAbsIndex) {
		lines.highlightAnchor(anchorAbsIndex);
	}
	
	private int moveDistance() {
		return 2*delta+inputAreaWidth-mTouchSlop;
	}
	
	private static int declaredWidth(View view) {
		return view.getLayoutParams().width;
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

	/**
	 * Updates drawing model of TimeDivider that starts given time. Preserves absolute position of middleX. 
	 * @param timeIndex index of given time, may be invalid
	 */
	private void recreateTimeDivider(int timeIndex)
			throws CreationException {
		if(timeIndex >= 0 && timeIndex < times.size()) {
			Time time = times.get(timeIndex);
			int oldAbsMiddleX = viewStableX(time.view) + middleX(time.view);
			time.view.setModel(createDrawingModel(new ElementSpec.TimeDivider(
				timeIndex > 0 ? times.get(timeIndex-1).spec : null,
				time.spec
			)));
			updatePosition(
				time.view, 
				oldAbsMiddleX - middleX(time.view), 
				sheetElementY(time.view)
			);
		}
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