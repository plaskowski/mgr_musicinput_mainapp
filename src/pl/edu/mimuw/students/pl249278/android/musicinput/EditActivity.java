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
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

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
	private View hscroll;
	private int rightToIA;
	private int iaRightMargin;
	private int delta;
	private int mTouchSlop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editscreen);
		
		sheetParams = new SheetParams(10, 40);
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		hscroll = findViewById(R.id.EDIT_outer_hscrollview);
		sheet = (RelativeLayout) findViewById(R.id.EDIT_sheet_container);
		lines = new Sheet5LinesView(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			LayoutParams.FILL_PARENT,
			100
		);
		sheet.addView(lines, params);
		
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
		
		((InterceptedHorizontalScrollView) hscroll).setListener(new OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int l, int oldl) {
//				LogUtils.info("scrollChange (%d, %d)", l, oldl);
				if(model.isEmpty()) return;
				if(l < oldl && rightToIA-1 >= 0) {
					while(rightToIA-1 >= 0) {
						NoteView firstToLeft = noteViews.get(rightToIA-1);
						int x;
						RelativeLayoutAnimator.RLAnimation anim = null;
						if((anim = animator.getAnimation(firstToLeft)) != null) {
							x = anim.destX();
						} else {
							x = leftMargin(firstToLeft);
						}
						int middle = x - l + firstToLeft.getBaseMiddleX();
						if(middle > hscroll.getWidth()-inputAreaWidth - iaRightMargin - delta + mTouchSlop) {
							if(anim != null) {
								// reverse animation
								animator.stopAnimation(anim);
								int dx = anim.startX()-leftMargin(firstToLeft);
								log.i("Reverse animation dx: %d", dx);
								animator.startAnimation(firstToLeft, dx, ANIM_TIME/3);
							} else {
								animator.startAnimation(firstToLeft, 2*delta+inputAreaWidth, ANIM_TIME);
							}
							rightToIA--;
						} else {
							break;
						}
					}
				} else if(l > oldl && rightToIA < model.size()) {
					while(rightToIA < model.size()) {
						NoteView firstToRight = noteViews.get(rightToIA);
						RelativeLayoutAnimator.RLAnimation anim = null;
						int x;
						if((anim = animator.getAnimation(firstToRight)) != null) {
							x = anim.destX();
						} else {
							x = leftMargin(firstToRight);
						}
						int middle = x - l + firstToRight.getBaseMiddleX();
						if(middle < hscroll.getWidth() - iaRightMargin + delta - mTouchSlop) {
							if(anim != null) {
								// reverse animation
								animator.stopAnimation(anim);
								int dx = anim.startX()-leftMargin(firstToRight);
								log.i("Reverse animation %d--[%d]-->", leftMargin(firstToRight), dx);
								animator.startAnimation(firstToRight, dx, ANIM_TIME/3);
							} else {
								animator.startAnimation(firstToRight, -2*delta-inputAreaWidth, ANIM_TIME);
							}
							rightToIA++;
						} else {
							break;
						}
					}
				}
			}
		});
		
		rightToIA = model.size();
	}
	
	private RelativeLayoutAnimator animator = new RelativeLayoutAnimator();
	
	private static class RelativeLayoutAnimator implements Runnable {
		private static Interpolator interpolator = new AccelerateDecelerateInterpolator();

		private static class RLAnimation {
			private int start_x;
			private int delta;
			private long duration;
			private long elapsed = 0;
			private View view;
			
			public void apply() {
				// apply animation
				float currentDelta = (interpolator.getInterpolation(((float) elapsed)/duration))*delta;
				updatePosition(view, start_x + (int) currentDelta, null);
			}
			public int startX() {
				return start_x;
			}
			public int destX() {
				return start_x+delta;
			}
			public boolean isFinished() {
				return elapsed == duration;
			}
		}
		
		private ArrayList<RLAnimation> animations = new ArrayList<RelativeLayoutAnimator.RLAnimation>();
		private Handler mHandler = new Handler();
		private boolean mIsRunning = false;
		private long lastticktime;
		
		public void startAnimation(View view, int dx, long duration) {
			RelativeLayout.LayoutParams params = (LayoutParams) view.getLayoutParams();
			RLAnimation anim = new RLAnimation();
			anim.view = view;
			anim.start_x = params.leftMargin;
			anim.delta = dx;
			anim.duration = duration;
			animations.add(anim);
			log.i("startAnimation(): %d --[%d]--> %d, dur: %d", anim.start_x, dx, anim.start_x+dx, duration);
			if(!mIsRunning) {
				lastticktime = System.currentTimeMillis(); 
				mHandler.post(this);
				mIsRunning = true;
			}
		}
		
		public void stopAnimation(RLAnimation anim) {
			animations.remove(anim);
			if(animations.isEmpty()) {
				mHandler.removeCallbacks(this);
				mIsRunning = false;
			}
		}

		public RLAnimation getAnimation(View view) {
			for (RLAnimation anim : animations) {
				if(anim.view == view) return anim;
			}
			return null;
		}

		@Override
		public void run() {
			long currTime = System.currentTimeMillis();
			long tick = currTime-lastticktime;
			lastticktime = currTime;
			for (Iterator<RLAnimation> it = animations.iterator(); it.hasNext();) {
				RLAnimation anim = (RLAnimation) it.next();
				anim.elapsed = Math.min(anim.elapsed+tick, anim.duration);
				anim.apply();
				if(anim.isFinished()) {
					it.remove();
				}
			}
			mIsRunning = !animations.isEmpty();
			if(mIsRunning) {
				mHandler.post(this);
			}
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		OnGlobalLayoutListener listener = new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				hscroll.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				LogUtils.info("onGlobalLayout() >> HSCROLL %dx%d", hscroll.getWidth(), hscroll.getHeight());
				measureSheetParams(hscroll.getWidth(), hscroll.getHeight());
			}
		};
		hscroll.getViewTreeObserver().addOnGlobalLayoutListener(listener);
	}
	
	protected void measureSheetParams(int visibleRectWidth, int visibleRectHeight) {
		// calculate default scale so spaces/lines (from space -1 to space 5) fit visible height
		float scale = ((float) (visibleRectHeight)) / ((float) (
			sheetParams.getLineFactor() * 5 + sheetParams.getLinespacingFactor() * 6
		));
		sheetParams.setScale(scale);
		
		LogUtils.info("EditActivity::measureSheetParams(width: %d, height: %d) computed scale = %f",
			visibleRectWidth, visibleRectHeight,
			scale
		);
		
		int line0y = Math.abs(
			sheetParams.anchorOffset(
				NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE), AnchorPart.TOP_EDGE
			)
		);
		
		// position IA
		RelativeLayout.LayoutParams params = (LayoutParams) inputArea.getLayoutParams();
		iaRightMargin = params.rightMargin = Math.min(
			getResources().getDimensionPixelSize(R.dimen.inputAreaMaxRightMargin),
			(int) (visibleRectWidth*0.4 - inputAreaWidth)
		);
		inputArea.setLayoutParams(params);
		
		int notesStaticOffset = visibleRectWidth-inputAreaWidth-iaRightMargin;
		delta = (int) (70*scale);
		int x = notesStaticOffset;
		// TODO extract this to sheetParams
		int noteSpacingBase = (int) (300*sheetParams.getScale());
		
		int notesTotalSpacing = 0;
		int spacingAfter = 0;
		int maxNoteRightSideWidth = 0;
		int length = model.size();
		for(int i = 0; i < length; i++) {
			NoteView v = noteViews.get(i);
			v.setSheetParams(sheetParams);
			updatePosition(
				v, 
				x - v.getBaseMiddleX(),
				line0y + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE)
			);
			spacingAfter = noteSpacingBase >> model.get(i).length();
			x += spacingAfter;
			notesTotalSpacing += spacingAfter;
			maxNoteRightSideWidth = Math.max(maxNoteRightSideWidth, v.measureWidth()-v.getBaseMiddleX());
		}
		notesTotalSpacing -= spacingAfter;
		
		// TODO calculate new sheet size
		final int sheetNewWidth = notesStaticOffset+notesTotalSpacing+delta+inputAreaWidth+Math.max(
			iaRightMargin,
			delta+maxNoteRightSideWidth
		);
		updateSize(sheet, sheetNewWidth, visibleRectHeight);
		// TODO calculate sheet start scroll position
		hscroll.post(new Runnable() {
		    @Override
		    public void run() {
				hscroll.scrollTo(sheetNewWidth, 0);
		    } 
		});
		
		lines.setParams(sheetParams);
		updatePosition(lines, 0, line0y);
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