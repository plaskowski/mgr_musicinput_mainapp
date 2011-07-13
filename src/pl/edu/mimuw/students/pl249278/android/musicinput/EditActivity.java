package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.TOP_EDGE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.EnhancedSvgImage.InvalidMetaException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ModifiedScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteValueSpinner.OnValueChanged;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.OutlineDrawable;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor.OnScaleListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StaticNotationElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.TempoView;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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
	private static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	private static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
	protected static final int SPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);

	private static final int ANIM_TIME = 150;
	protected Paint noteHighlightPaint = new Paint();
	private int NOTE_DRAW_PADDING = 0;
	protected static final Paint normalPaint = new Paint();
	{
		normalPaint.setAntiAlias(true);
		noteHighlightPaint.setAntiAlias(true);
	}

	private static LogUtils log = new LogUtils(EditActivity.class);
	
	private Sheet5LinesView lines;
	private SheetParams sheetParams;
	private RelativeLayout sheet;
	private ArrayList<NoteSpec> model;
	private ArrayList<NoteView> noteViews = new ArrayList<NoteView>();
	private int inputAreaWidth;
	private View inputArea;
	private HorizontalScrollView hscroll;
	private ModifiedScrollView vertscroll;
	private ScaleGestureInterceptor scaleGestureDetector;
	private LayoutAnimator animator = new LayoutAnimator();
	private NoteView newNote;
	
	/**
	 * Index of note that is first on right side of InputArea,
	 * when there is no such note rightToIA = model.size() 
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

	// TODO bind this with widget
	protected int currentNoteLength = 0;
	private List<SheetElementView> staticElements = new ArrayList<SheetElementView>();
	private float noteMinDistToIA;
	private float minNoteSpacingFactor;
	private int minNoteValue;
	private float staticElementsSpacing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscreen);
		
		// TODO sheetParams comes from previous view
		sheetParams = new SheetParams(10, 100); // TODO read thickness from xml
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
		sheet.setOnTouchListener(iaTouchListener);
		
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
		NoteView popupIcon = (NoteView) findViewById(R.id.EDIT_info_popup_note);
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
		
		// setup static elements
		StaticNotationElementView el;
		el = new StaticNotationElementView(this);
		try {
			el.setImage(NotePartFactory.prepareEnhacedSvgImage(this, R.xml.key_violin));
			el.setPaint(normalPaint);
			sheet.addView(el);
		} catch (InvalidMetaException e1) {
			e1.printStackTrace();
			finish();
		} catch (LoadingSvgException e1) {
			e1.printStackTrace();
			finish();
		}
		this.staticElements .add(el);
		TempoView tempo = new TempoView(this);
		tempo.setLetters('3', '4');
		tempo.setPaint(normalPaint);
		sheet.addView(tempo);
		staticElements.add(tempo);
		
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
		
		noteShadow = Float.parseFloat(getResources().getString(R.string.noteShadow));
		noteMinDistToIA = Float.parseFloat(getResources().getString(R.string.minDistToIA));
		minNoteSpacingFactor = Float.parseFloat(getResources().getString(R.string.minNoteSpacing));
		minNoteValue = getResources().getInteger(R.integer.defaultMinNoteValue);
		staticElementsSpacing = Float.parseFloat(getResources().getString(R.string.staticElementsSpacing));
		
		model = new ArrayList<NoteSpec>();
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
		/*
//		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE)));
//		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE+1, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE)));
//		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE)));
//		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, LINE0_ABSINDEX));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINE)));
		*/
		try {
			for (NoteSpec noteSpec : model) {
				NoteView noteView = new NoteView(this, noteSpec.length(), noteSpec.positon());
				noteView.setPaint(normalPaint);
				noteViews.add(noteView);
				sheet.addView(noteView);
			}
		} catch (NoteDescriptionLoadingException e) {
			e.printStackTrace();
			finish();
		}
		
		((InterceptedHorizontalScrollView) hscroll).setListener(horizontalScrollListener);
		
		rightToIA = model.size();
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
	
	private View.OnTouchListener iaTouchListener = new OnTouchListener() {
		private static final int INVALID_POINTER = -1;
		private int activePointerId = INVALID_POINTER;
		private int currentAnchor;
		
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
					currentAnchor = nearestAnchor((int) event.getY());
					lines.highlightAnchor(currentAnchor);
					activePointerId = event.getPointerId(event.getActionIndex());
					if(newNote == null) {
						newNote = new NoteView(EditActivity.this);
					}
					try {
						newNote.setNoteSpec(EditActivity.this, currentNoteLength , currentAnchor);
					} catch (NoteDescriptionLoadingException e) {
						e.printStackTrace();
						finish();
						return false;
					}
					newNote.setSheetParams(sheetParams);
					newNote.setPaint(noteHighlightPaint);
					newNote.setPadding((int) NOTE_DRAW_PADDING);
					sheet.addView(newNote);
					updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
					vertscroll.setVerticalScrollingLocked(true);
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(activePointerId == INVALID_POINTER) 
					break;
				if(!insideIA((int) event.getX())) {
					cancel();
					activePointerId = INVALID_POINTER;
					break;
				}
				int newAnchor = nearestAnchor((int) event.getY());
				if(newAnchor != currentAnchor) {
					lines.highlightAnchor(newAnchor);
					try {
						newNote.setNoteSpec(EditActivity.this, currentNoteLength, newAnchor);
					} catch (NoteDescriptionLoadingException e) {
						e.printStackTrace();
						finish();
						return false;
					}
					updatePosition(newNote, inIA_noteViewX(newNote), sheetElementY(newNote));
					currentAnchor = newAnchor;
				}
				return true;
			case MotionEvent.ACTION_POINTER_1_UP:
				if(event.getPointerId(event.getActionIndex()) != activePointerId)
					break;
			case MotionEvent.ACTION_CANCEL:
				activePointerId = INVALID_POINTER;
				cancel();
				return true;
			case MotionEvent.ACTION_UP:
				if(activePointerId == INVALID_POINTER)
					break;
				activePointerId = INVALID_POINTER;
				insertNoteAndClean();
				return true;
			}
			return false;
		}
		
		private void insertNoteAndClean() {
			int index = rightToIA;
			model.add(index, new NoteSpec(currentNoteLength, currentAnchor));
			rightToIA++;
			NoteView noteView = newNote;
			newNote = null;
			noteView.setPaint(normalPaint);
			updatePosition(noteView, inIA_noteViewX(noteView), sheetElementY(noteView));
			noteViews.add(index, noteView);
			resizeSheetOnNoteInsert(index);
			
			int noteMiddle = leftMargin(noteView)+noteView.getBaseMiddleX();
			int destNoteMiddle;
			if(index == 0) {
				destNoteMiddle = notesAreaX;
			} else {
				NoteView prevNote = noteViews.get(index-1);
				destNoteMiddle = leftMargin(prevNote) + prevNote.getBaseMiddleX() + afterNoteSpacing(model.get(index-1));
			}
			long duration = 500;
			
			WaitManyRunOnce animationsEndListener = new WaitManyRunOnce(2+model.size()-rightToIA) {
				@Override
				protected void allFinished() {
					scaleGestureDetector.setTouchInputLocked(false);
				}
			}; 
			
			animator.startHScrollAnimation(
				hscroll, 
				(destNoteMiddle-hscroll.getScrollX())-((visibleRectWidth - inputAreaWidth - iaRightMargin - delta)), 
				duration, 
				animationsEndListener
			);
			animator.startRLAnimation(
				noteView, 
				destNoteMiddle-noteMiddle, 
				duration, 
				animationsEndListener
			);
			int newNoteSpacing = afterNoteSpacing(model.get(index)); 
			for(int i = rightToIA; i < model.size(); i++) {
				animator.startRLAnimation(noteViews.get(i), newNoteSpacing, duration, animationsEndListener);
			}
			
			lines.highlightAnchor(null);
			vertscroll.setVerticalScrollingLocked(false);
			scaleGestureDetector.setTouchInputLocked(true);
		}

		private int inIA_noteViewX(NoteView noteView) {
			return hscroll.getScrollX()+visibleRectWidth-iaRightMargin-inputAreaWidth/2-noteView.getBaseMiddleX();
		}
		
		/**
		 * Clear all artifacts introduced by touch inside IA box
		 */
		private void cancel() {
			vertscroll.setVerticalScrollingLocked(false);
			lines.highlightAnchor(null);
			sheet.removeView(newNote);
		}

		/**
		 * @param x in sheet view coordinates
		 * @return if point (x,?) is inside input area box
		 */
		private boolean insideIA(int x) {
			int pos = x-hscroll.getScrollX();
			return pos >= visibleRectWidth-inputAreaWidth-iaRightMargin && pos <= visibleRectWidth-iaRightMargin;
		}

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
			int indexDeltaBase = y - (line0Top + line0middle - delta/2);
			int indexDelta = indexDeltaBase/delta;
			return Math.max( 
				Math.min(
				LINE0_ABSINDEX + indexDelta + (indexDeltaBase < 0 ? -1 : 0),
				NoteConstants.anchorIndex(sheetParams.getMaxSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
				),
				NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE)
			);
		}
	};
	
	private OnScrollChangedListener horizontalScrollListener = new OnScrollChangedListener() {
		@Override
		public void onScrollChanged(int l, int oldl) {
//				LogUtils.info("scrollChange (%d, %d)", l, oldl);
			if(isScaling) return;
			if(model.isEmpty()) return;
			if(l < oldl && rightToIA-1 >= 0) {
				while(rightToIA-1 >= 0) {
					NoteView firstToLeft = noteViews.get(rightToIA-1);
					int x;
					LayoutAnimator.LayoutAnimation<?> anim = null;
					if((anim = animator.getAnimation(firstToLeft)) != null) {
						x = anim.destValue();
					} else {
						x = leftMargin(firstToLeft);
					}
					int middle = x - l + firstToLeft.getBaseMiddleX();
					if(middle > hscroll.getWidth()-inputAreaWidth - iaRightMargin - delta + mTouchSlop) {
						if(anim != null) {
							// reverse animation
							animator.stopAnimation(anim);
							int dx = anim.startValue()-leftMargin(firstToLeft);
//							log.i("Reverse animation dx: %d", dx);
							animator.startRLAnimation(firstToLeft, dx, ANIM_TIME/3);
						} else {
							animator.startRLAnimation(firstToLeft, 2*delta+inputAreaWidth, ANIM_TIME);
						}
						rightToIA--;
					} else {
						break;
					}
				}
			} else if(l > oldl && rightToIA < model.size()) {
				while(rightToIA < model.size()) {
					NoteView firstToRight = noteViews.get(rightToIA);
					LayoutAnimator.LayoutAnimation<?> anim = null;
					int x;
					if((anim = animator.getAnimation(firstToRight)) != null) {
						x = anim.destValue();
					} else {
						x = leftMargin(firstToRight);
					}
					int middle = x - l + firstToRight.getBaseMiddleX();
					if(middle < hscroll.getWidth() - iaRightMargin + delta - mTouchSlop) {
						if(anim != null) {
							// reverse animation
							animator.stopAnimation(anim);
							int dx = anim.startValue()-leftMargin(firstToRight);
//							log.i("Reverse animation %d--[%d]-->", leftMargin(firstToRight), dx);
							animator.startRLAnimation(firstToRight, dx, ANIM_TIME/3);
						} else {
							animator.startRLAnimation(firstToRight, -2*delta-inputAreaWidth, ANIM_TIME);
						}
						rightToIA++;
					} else {
						break;
					}
				}
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
			final int modelSize = model.size();
			if(modelSize == 0) {
				rightToIA = 0;
			} else {
				if(rightToIA < 0 || rightToIA >= modelSize) {
					rightToIA = searchRightToBackwards(modelSize-1, IAmiddle);
				} else {
					NoteView prevToRight = noteViews.get(rightToIA);
					if(middleVisibleX(prevToRight) <= IAmiddle) {
						rightToIA = searchRightTo(rightToIA, IAmiddle);
					} else {
						rightToIA = searchRightToBackwards(rightToIA, IAmiddle);
					}
				}
			}
			log.i("onScaleEnd(): new right: %d", rightToIA);
			
			// scroll sheet so left border of IA matches: 
			int leftToIAArea = visibleRectWidth -inputAreaWidth - iaRightMargin - delta + mTouchSlop;
			int destScrollX = rightToIA == 0
				// left border of notes area
				? notesAreaX - (visibleRectWidth - inputAreaWidth - iaRightMargin)
				// middle of leftToIA note + delta
				: middleX(noteViews.get(rightToIA-1)) - (leftToIAArea-mTouchSlop);
			animator.startHScrollAnimation(hscroll, destScrollX-hscroll.getScrollX(), 300, new Runnable() {
				@Override
				public void run() {
					if(rightToIA == modelSize) {
						scalingFinished();
					} else {
						// translate all notes that are on right of leftToIA area to make space for IA */
						Runnable listn = new WaitManyRunOnce(modelSize-rightToIA) {
							@Override
							protected void allFinished() {
								scalingFinished();
							}
						};
						for(int i = rightToIA; i < modelSize; i++) {
							NoteView view = noteViews.get(i);
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
		 * @return index of such note or model.size() if none
		 */
		private int searchRightTo(int index, int markerX) {
			int size = model.size();
			for(; index < size; index++) {
				NoteView view = noteViews.get(index);
				if(middleVisibleX(view) > markerX) {
					break;
				}
			}
			return index;
		}

		/**
		 * search (in reverse order) for note which middle is further on x-axis that markerX
		 * @param index search from this index to the beginning of noteViews
		 * @return index of such note or model.size() if none
		 */
		private int searchRightToBackwards(int index, int markerX) {
			int prevGood = model.size();
			for(; index >= 0; index--) {
				NoteView view = noteViews.get(index);
				if(middleVisibleX(view) > markerX) {
					prevGood = index;
				} else {
					break;
				}
			}
			return prevGood;
		}

		/**
		 * @return vertical position of note base middle inside visible rect
		 */
		public int middleVisibleX(NoteView view) {
			return middleX(view)-hscroll.getScrollX();
		}
		private int middleX(NoteView view) {
			return leftMargin(view)+view.getBaseMiddleX();
		}
		
		private void scalingFinished() {
			isScaling = false;
			scaleGestureDetector.setTouchInputLocked(false);
		}
	};

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
		
		public void startRLAnimation(NoteView view, int dx, int duration) {
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
		NOTE_DRAW_PADDING = (int) (noteShadow * sheetParams.getLinespacingThickness());
		noteHighlightPaint.setShadowLayer(NOTE_DRAW_PADDING, NOTE_DRAW_PADDING/2, NOTE_DRAW_PADDING, Color.BLACK);		
		lines.setParams(sheetParams);
		// TODO extract this to sheetParams
		int minLinespaceTopOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE), 
			AnchorPart.TOP_EDGE
		);
		line0Top = Math.abs(minLinespaceTopOffset);

		// scale static objects
		int staticEspacing = (int) (staticElementsSpacing*sheetParams.getLinespacingThickness());
		int staticElementsSpan = 0;
		for (SheetElementView el : staticElements) {
			el.setSheetParams(sheetParams);
			staticElementsSpan += staticEspacing + el.measureWidth();
		}
		staticElementsSpan += afterNoteSpacing(minNoteValue);
		
		
		int paddingLeft = Math.max(
			lines.getMinPadding(),
			// assure that when sheet is scrolled to start IA left edge matches start of area where notes are placed
			visibleRectWidth-inputAreaWidth-iaRightMargin - staticElementsSpan
		);
		lines.setNotesAreaLeftPadding(paddingLeft);
		
		// layout sheet static elements
		int elX = paddingLeft;
		for (SheetElementView el : staticElements) {
			elX += staticEspacing;
			updatePosition(el, elX, sheetElementY(el));
			elX += el.measureWidth();
		}
		
		this.notesAreaX = paddingLeft + staticElementsSpan;
		delta = (int) (sheetParams.getLinespacingThickness()*noteMinDistToIA);
		int length = model.size();
		
		int notesTotalSpacing = 0;
		int spacingAfter = 0;
		int maxNoteRightSideWidth = 0;
		int x = notesAreaX;
		for(int i = 0; i < length; i++) {
			NoteView v = noteViews.get(i);
			v.setSheetParams(sheetParams);
			int xpos = x-v.getBaseMiddleX();
			updatePosition(
				v, 
				xpos,
				sheetElementY(v)
			);
//			log.i("onScaleFactor() note[%d] at x: %d", i, xpos);
			spacingAfter = afterNoteSpacing(model.get(i));
			x += spacingAfter;
			notesTotalSpacing += spacingAfter;
			maxNoteRightSideWidth = Math.max(maxNoteRightSideWidth, v.measureWidth()-v.getBaseMiddleX());
		}
		notesTotalSpacing -= spacingAfter;
		
		// TODO calculate new sheet size
		final int sheetNewWidth =
			// space for all notes on normal places
			notesAreaX+notesTotalSpacing
			// space for last note shifted on right side of IA
			+ delta + inputAreaWidth + delta
			+ Math.max(
				// when sheet scrolled to end IA must reach shifted last note
				iaRightMargin - (delta - mTouchSlop) + 1,
				// space for right part of note
				maxNoteRightSideWidth
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
	
	/**
	 * Resize sheet accordingly to recently inserted note
	 * @param newNoteIndex index of inserted note
	 */
	private void resizeSheetOnNoteInsert(int newNoteIndex) {
		if(model.size() == 1) return;
		int delta;
		if(newNoteIndex < model.size()-1) {
			// add spacing of inserted note
			delta = afterNoteSpacing(model.get(newNoteIndex));
		} else {
			// add spacing of note that precedes inserted note
			delta = afterNoteSpacing(model.get(model.size()-2));
		}
		int newSheetWidth = declaredWidth(sheet)+delta;
		updateSize(sheet, newSheetWidth, null);
		updateSize(lines, newSheetWidth, null);
	}
	
	private Handler mHandler = new Handler();
	private Runnable mHideInfoPopupTask = new Runnable() {
	   public void run() {
		   findViewById(R.id.EDIT_info_popup).setVisibility(View.GONE);
	   }
	};
	private float noteShadow;
	
	protected void popupCurrentNoteLength() {
		View popup = findViewById(R.id.EDIT_info_popup);
		NoteView noteView = (NoteView) popup.findViewById(R.id.EDIT_info_popup_note);
		SheetParams params = new SheetParams(sheetParams);
		params.setScale(1);
		params.setScale(
			((float) getResources().getDimensionPixelSize(R.dimen.infoPopupIconHeight))
		/	params.anchorOffset(LINE4_ABSINDEX, BOTTOM_EDGE)
		);
		try {
			noteView.setNoteSpec(this, currentNoteLength, LINE4_ABSINDEX);
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

	private int afterNoteSpacing(NoteSpec note) {
		return afterNoteSpacing(note.length());
	}
	private int afterNoteSpacing(int noteLength) {
		return (int) (sheetParams.getLinespacingThickness()*minNoteSpacingFactor*Math.pow(1.3, minNoteValue-noteLength));
	}

	private int sheetElementY(SheetElementView v) {
		return line0Top + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE);
	}	
	
	private static int leftMargin(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
	}

	private static int declaredWidth(View view) {
		return view.getLayoutParams().width;
	}
	
	private static void updatePosition(View v, Integer left, Integer top) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		if(left != null) params.leftMargin = left;
		if(top != null) params.topMargin = top;
		v.setLayoutParams(params);
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