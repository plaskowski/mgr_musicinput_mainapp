package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.BOTTOM_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart.TOP_EDGE;

import java.util.ArrayList;
import java.util.Iterator;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.InterceptedHorizontalScrollView.OnScrollChangedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ScaleGestureInterceptor.OnScaleListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.app.Activity;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;

public class EditActivity extends Activity {
	private static final int ANIM_TIME = 150;

	private static LogUtils log = new LogUtils(EditActivity.class);
	
	private Sheet5LinesView lines;
	private SheetParams sheetParams;
	private RelativeLayout sheet;
	private ArrayList<NoteSpec> model;
	private ArrayList<NoteView> noteViews = new ArrayList<NoteView>();
	private int inputAreaWidth;
	private View inputArea;
	private HorizontalScrollView hscroll;
	private ScrollView vertscroll;
	private ScaleGestureInterceptor scaleGestureDetector;
	/**
	 * Index of note that is first on right side of InputArea,
	 * when there is no such note rightToIA = model.size() 
	 */
	private int rightToIA;
	private int iaRightMargin;
	private int delta;
	private int mTouchSlop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscreen);
		
		// TODO sheetParams comes from previous view
		sheetParams = new SheetParams(10, 40); // TODO read thickness from xml
		sheetParams.setMinSpaceAnchor(getResources().getInteger(R.integer.minSpaceDefault));
		sheetParams.setMaxSpaceAnchor(getResources().getInteger(R.integer.maxSpaceDefault));
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		scaleGestureDetector = (ScaleGestureInterceptor) findViewById(R.id.EDIT_scale_detector);
		hscroll = (HorizontalScrollView) findViewById(R.id.EDIT_outer_hscrollview);
		vertscroll = (ScrollView) findViewById(R.id.EDIT_vertscrollview);
		sheet = (RelativeLayout) findViewById(R.id.EDIT_sheet_container);
		lines = new Sheet5LinesView(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			LayoutParams.FILL_PARENT,
			100
		);
		sheet.addView(lines, params);
		scaleGestureDetector.setOnScaleListener(scaleListener);
		
		this.inputArea = findViewById(R.id.EDIT_inputArea);
		this.inputAreaWidth = getResources().getDimensionPixelSize(R.dimen.inputAreaWidth);
		ViewConfiguration configuration = ViewConfiguration.get(this);
        this.mTouchSlop = configuration.getScaledTouchSlop();
		
		model = new ArrayList<NoteSpec>();
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(3, NoteConstants.ANCHOR_TYPE_LINE)));
		model.add(new NoteSpec(NoteConstants.LEN_QUATERNOTE, NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE)));
		model.add(new NoteSpec(NoteConstants.LEN_HALFNOTE, NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE)));
		
		try {
			for (NoteSpec noteSpec : model) {
				NoteView noteView = new NoteView(this, noteSpec.length(), noteSpec.positon());
				noteView.setPaint(paint);
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
	
	private LayoutAnimator animator = new LayoutAnimator();

	private OnScrollChangedListener horizontalScrollListener = new OnScrollChangedListener() {
		@Override
		public void onScrollChanged(int l, int oldl) {
			// TODO ignore if scaling in progress
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
							log.i("Reverse animation dx: %d", dx);
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
							log.i("Reverse animation %d--[%d]-->", leftMargin(firstToRight), dx);
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

	protected boolean isScaling = false;
	
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
			/* TODO
			 * - lock touch input for whole SGInterceptor tree
			 * - scroll (with my modified animator) sheet to position so the note with xmiddle to left of IA.left is at distance delta
			 * - translate all visible next notes to make IA a space
			 * - isScaling := false; 
			 * - unlock touch input 
			 * - update rightToIA value
			 */
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
				? notesAreaX - visibleRectWidth - inputAreaWidth - iaRightMargin
				// middle of leftToIA note + delta
				: middleX(noteViews.get(rightToIA-1)) - (leftToIAArea-mTouchSlop);
			animator.startHScrollAnimation(hscroll, destScrollX-hscroll.getScrollX(), 500, new Runnable() {
				@Override
				public void run() {
					if(rightToIA == modelSize) {
						scalingFinished();
					} else {
						// translate all notes that are on right of leftToIA area to make space for IA */
						WaitForAllAnimationListener listn = new WaitForAllAnimationListener(modelSize-rightToIA);
						for(int i = rightToIA; i < modelSize; i++) {
							NoteView view = noteViews.get(i);
							log.i("onScaleEnd() shifts note[%d] from x: %d", i, leftMargin(view)); 
							animator.startRLAnimation(view, inputAreaWidth+2*delta, 1000, listn);
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
		
		class WaitForAllAnimationListener implements Runnable {
			private int amount;
			public WaitForAllAnimationListener(int amount) {
				this.amount = amount;
			}

			@Override
			public void run() {
				amount--;
				if(amount == 0) {
					scalingFinished();
				}
			}

		}
		
		private void scalingFinished() {
			isScaling = false;
			// TODO unlock touch input
		}
	};

	private int visibleRectWidth;
	private int visibleRectHeight;
	private int notesAreaX;

	private int line0Top;
	
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
			RelativeLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
			RLAnimation anim = new RLAnimation(view, params.leftMargin, dx, duration);
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
		RelativeLayout.LayoutParams params = (LayoutParams) inputArea.getLayoutParams();
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
				hscroll.scrollTo(sheet.getLayoutParams().width, 0);
				vertscroll.scrollTo(0,
					line0Top + sheetParams.anchorOffset(NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE), AnchorPart.TOP_EDGE)
				);
		    } 
		});
	}

	private void updateScaleFactor(float newScaleFactor) {
		sheetParams.setScale(newScaleFactor);
		
		lines.setParams(sheetParams);
		// TODO scale and layout static objects and calculate notesStaticOffset
		int notesStaticOffset = 100;
		
		int paddingLeft = Math.max(
			lines.getMinPadding(),
			// assure that when sheet is scrolled to start IA left edge matches start of area where notes are placed
			visibleRectWidth-inputAreaWidth-iaRightMargin - notesStaticOffset
		);
		lines.setNotesAreaLeftPadding(paddingLeft);
		this.notesAreaX = paddingLeft + notesStaticOffset;
		
		// TODO extract this to sheetParams
		delta = (int) (70*newScaleFactor);
		// TODO extract this to sheetParams
		int noteSpacingBase = (int) (300*sheetParams.getScale());
		int length = model.size();
		int minLinespaceTopOffset = sheetParams.anchorOffset(
			NoteConstants.anchorIndex(sheetParams.getMinSpaceAnchor(), NoteConstants.ANCHOR_TYPE_LINESPACE), 
			AnchorPart.TOP_EDGE
		);
		line0Top = Math.abs(minLinespaceTopOffset);
		
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
				line0Top + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE)
			);
			log.i("onScaleFactor() note[%d] at x: %d", i, xpos);
			spacingAfter = noteSpacingBase >> model.get(i).length();
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
		
		updatePosition(lines, 0, line0Top);
		updateSize(
			lines, 
			sheetNewWidth, 
			sheetParams.anchorOffset(NoteConstants.anchorIndex(4, ANCHOR_TYPE_LINE), BOTTOM_EDGE)
		);
		
	}	
	
	private static int leftMargin(View view) {
		return ((LayoutParams) view.getLayoutParams()).leftMargin;
	}

	private static void updatePosition(View v, Integer left, Integer top) {
		RelativeLayout.LayoutParams params = (LayoutParams) v.getLayoutParams();
		if(left != null) params.leftMargin = left;
		if(top != null) params.topMargin = top;
		v.setLayoutParams(params);
	}
	
	private static void updateSize(View v, int width, int height) {
		android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
		params.width = width;
		params.height = height;
		v.setLayoutParams(params);
	}	
}