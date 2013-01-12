package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.SPACEm1_ABSINDEX;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.common.PaintBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy.PositioningEnv;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy.SpacingEnv;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentElem;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig.DisplayMode;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PrepareResourceTask;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PrepareResourceTask.OnLoadedListener;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.WorkerThread;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.JoinArc;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup.GroupBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

public class ScoreThumbnailDrawable extends Drawable implements Drawable.Callback {
	private WorkerThread worker;
	private int currentAlpha = 255;
	private ColorFilter currentColorFilter = null;
	private DrawingSetup setup = null;
	private Task pendingTask = null;
	private TaskArgs args = new TaskArgs();
	private Paint paint;
	private Drawable loadingIcon;
	private ColorStateList color;
	private static ConstantState common;
	
	private static class ConstantState {
		final Handler uiHandler;

		private ConstantState(Context ctx) {
			uiHandler = new Handler();
		}
	}
	
	public ScoreThumbnailDrawable(Context appContext, WorkerThread worker, ColorStateList color) {
		if(common == null) {
			common = new ConstantState(appContext);
		}
		this.worker = worker;
		this.args.ctx = appContext;
		this.color = color;
	}

	public void setModel(List<ScoreContentElem> score, DisplayMode displayMode) {
		setup = null;
		pendingTask = null;
		args.score = score;
		args.displayMode = displayMode;
		invalidateSelf();
	}

	@Override
	public void draw(Canvas canvas) {
		final Rect bounds = getBounds();
		if(setup != null && setup.width == bounds.width() && setup.height == bounds.height()) {
			if(paint == null) {
				paint = PaintBuilder.init().style(Style.FILL).antialias(true).build();
			}
			paint.setColor(color.getColorForState(getState(), 0));
			paint.setAlpha(currentAlpha);
			paint.setColorFilter(currentColorFilter);
			drawElements(setup.elements, setup.elX, setup.elY, canvas, paint);
			drawElements(setup.overlays, setup.overlayX, setup.overlayY, canvas, paint);
			// draw 5 lines
			float width = bounds.width(), left = bounds.left, top = bounds.top;
			for(int i = 0; i < 5; i++) {
				int anchorIndex = NoteConstants.anchorIndex(i, NoteConstants.ANCHOR_TYPE_LINE);
				canvas.drawRect(
					left + setup.linesPaddingLeft, 
					top + setup.line0Top + setup.params.anchorOffset(anchorIndex, AnchorPart.TOP_EDGE),
					left + width, 
					top + setup.line0Top + setup.params.anchorOffset(anchorIndex, AnchorPart.BOTTOM_EDGE),
					paint
				);
			}
		} else {
			if(pendingTask == null && args.score != null) {
				args.width = bounds.width();
				args.height = bounds.height();
				pendingTask = new Task(args, new OnLoadedListener<TaskArgs, DrawingSetup>() {
					@Override
					public void onLoaded(PrepareResourceTask<TaskArgs, DrawingSetup> task,
							TaskArgs key, DrawingSetup value) {
						if(task == pendingTask) {
							setup = value;
							pendingTask = null;
							setLoadingIcon(null);
							invalidateSelf();
						}
					}
					@Override
					public Handler optionalHandler() {
						return common.uiHandler;
					}
				});
				worker.post(pendingTask);
			}
			// draw "loading" icon
			if(loadingIcon != null) {
				loadingIcon.getIntrinsicWidth();
				int hdiff = (bounds.width()-loadingIcon.getIntrinsicWidth())/2;
				int vdiff = (bounds.height()-loadingIcon.getIntrinsicHeight())/2;
				loadingIcon.setBounds(
					bounds.left + hdiff,
					bounds.top + vdiff,
					bounds.right - hdiff,
					bounds.bottom - vdiff);
				loadingIcon.draw(canvas);
				if(loadingIcon instanceof AnimationDrawable) {
					AnimationDrawable animated = (AnimationDrawable) loadingIcon;
					if(!animated.isRunning()) {
						animated.start();
					}
				}
			}
		}
	}

	private <T extends SheetElement> void drawElements(ArrayList<T> elements, int[] x, int[] y, Canvas canvas, Paint paint) {
		canvas.save();
		Rect bounds = getBounds();
		canvas.clipRect(bounds);
		canvas.translate(bounds.left, bounds.top);
		int ox = 0, oy = 0;
		int total = elements.size();
		for(int i = 0; i < total; i++) {
			SheetElement el = elements.get(i);
			int cx = x[i], cy = y[i];
			canvas.translate(cx-ox, cy-oy);
			el.onDraw(canvas, paint);
			ox = cx;
			oy = cy;
		}
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		currentAlpha = alpha;
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		currentColorFilter = cf;
		invalidateSelf();
	}
	
	@Override
	public boolean isStateful() {
		return color.isStateful();
	}
	
	@Override
	public boolean setState(int[] stateSet) {
		invalidateSelf();
		return super.setState(stateSet);
	}
	
	private static class TaskArgs {
		Context ctx;
		int width, height;
		List<ScoreContentElem> score;
		DisplayMode displayMode;
	}
	
	private static class DrawingSetup {
		int width, height;
		SheetParams params;
		ArrayList<SheetAlignedElement> elements = new ArrayList<SheetAlignedElement>();
		ArrayList<ElementsOverlay> overlays = new ArrayList<ElementsOverlay>();
		int[] elX, elY, overlayX, overlayY;
		public int line0Top, linesPaddingLeft;
		
		DrawingSetup(int width, int height, SheetParams params) {
			this.width = width;
			this.height = height;
			this.params = params;
		}
	}
	
	private static class Task extends PrepareResourceTask<TaskArgs, DrawingSetup> {

		public Task(TaskArgs name, PrepareResourceTask.OnLoadedListener<TaskArgs, DrawingSetup> listener) {
			super(name, listener);
		}
		
		private static class OverlayDef<T extends ElementsOverlay> {
			T overlay;
			int startIndex, endIndex;
			
			public OverlayDef(T overlay, int startIndex,
					int endIndex) {
				this.overlay = overlay;
				this.startIndex = startIndex;
				this.endIndex = endIndex;
			}
		}

		@Override
		public DrawingSetup prepareValue(TaskArgs args) {
			try {
				return mPrepareValue(args);
			} catch (CreationException e) {
				Log.w(LogUtils.COMMON_TAG, e);
				return new DrawingSetup(args.width, args.height, null);
			}
		}
		
		private DrawingSetup mPrepareValue(TaskArgs args) throws CreationException {
			SheetParams params = new SheetParams(
					args.ctx.getResources().getInteger(R.integer.lineThickness),
					args.ctx.getResources().getInteger(R.integer.linespaceThickness));
			final DrawingSetup setup = new DrawingSetup(args.width, args.height, params);
			// <!-- Calculate scale factor
			int height = params.anchorOffset(NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINESPACE), AnchorPart.BOTTOM_EDGE)
			- params.anchorOffset(SPACEm1_ABSINDEX, AnchorPart.TOP_EDGE);
			params.setScale(args.height / ((float) height));
			// assure that line will be at least 1px thick
			if(params.getLineThickness() < 1) {
				params.setScale(1/((float) params.getLineFactor()));
			}
			
			// <!-- BUILD DRAWING MODEL
			TimeSpec prevTime = null;
			GroupBuilder builder = null;
			OverlayDef<JoinArc> joinArc = null;
			int accuWidth = 0;
			final List<SheetAlignedElement> drawings = setup.elements;
			List<OverlayDef<NotesGroup>> groups = new LinkedList<OverlayDef<NotesGroup>>();
			List<OverlayDef<JoinArc>> arcs = new LinkedList<OverlayDef<JoinArc>>();
			Iterator<ScoreContentElem> it = args.score.iterator();
			int i = 0;
			for(; it.hasNext() && (accuWidth < args.width || joinArc != null || builder != null); i++) {
				ScoreContentElem elem = it.next();
				ElementSpec spec;
				if(elem instanceof NoteSpec) {
					spec = ScoreHelper.elementSpecNN((NoteSpec) elem, args.displayMode);
				} else if(elem instanceof PauseSpec) {
					spec = new ElementSpec.Pause((PauseSpec) elem);
				} else if(elem instanceof TimeSpec) {
					TimeSpec timeSpec = (TimeSpec) elem;
					spec = new ElementSpec.TimeDivider(prevTime, timeSpec);
					prevTime = timeSpec;
				} else {
					throw new RuntimeException("Unsupported ScoreContentElem type: "+elem);
				}
				SheetAlignedElement drModel = DrawingModelFactory.createDrawingModel(args.ctx, spec);
				drModel.setSheetParams(params);
				drawings.add(drModel);
				if(builder == null && GroupBuilder.canStartGroup(spec)) {
					builder = new GroupBuilder(spec);
				} else if(builder != null && !builder.tryExtend(spec)) {
					// check if the group that was accumulates is valid
					if(builder.isValid()) {
						groups.add(new OverlayDef<NotesGroup>(builder.build(), i-builder.total(), i-1));
					}
					builder = null;
				}
				// try end or extend (by skipping over) JoinArc
				if(joinArc != null) {
					if(JoinArc.canEndJA(spec)) { 
						joinArc.endIndex = i;
						arcs.add(joinArc);
						joinArc = null;
					} else if(!JoinArc.canSkipOver(spec)) {
						joinArc = null;
					}
				}
				// try start new JoinArc
				if(joinArc == null && JoinArc.canStartJA(spec)) {
					joinArc = new OverlayDef<JoinArc>(null, i, 0);
				}
				accuWidth += drModel.measureWidth();
			}
			if(builder != null && builder.isValid()) {
				groups.add(new OverlayDef<NotesGroup>(builder.build(), i-builder.total(), i-1));
			}
			// rebuild drawing models for positions modified by NoteGroup
			for (OverlayDef<NotesGroup> overlayDef : groups) {
				overlayDef.overlay.setSheetParams(params);
				for(int elIndex = overlayDef.startIndex; elIndex <= overlayDef.endIndex; elIndex++) {
					SheetAlignedElement prev = drawings.get(elIndex);
					SheetAlignedElement modifiedModel = DrawingModelFactory.createDrawingModel(args.ctx, prev.getElementSpec());
					modifiedModel.setSheetParams(params);
					drawings.set(elIndex, modifiedModel);
					overlayDef.overlay.wrapNext(modifiedModel);
				}
			}
			// build JoinArc overlays
			for(OverlayDef<JoinArc> overlayDef: arcs) {
				overlayDef.overlay = new JoinArc(drawings.get(overlayDef.startIndex));
				overlayDef.overlay.setRightElement(drawings.get(overlayDef.endIndex));
				overlayDef.overlay.setSheetParams(params);
			}
			// calculate each element position
			final int line0Top = setup.line0Top = args.height/2 - params.anchorOffset(NoteConstants.LINE2_ABSINDEX, AnchorPart.MIDDLE);
			int elementsCount = drawings.size();
			setup.elX = new int[elementsCount];
			setup.elY = new int[elementsCount];
			PositioningStrategy strategy = new PositioningStrategy(args.ctx, setup);
			SimplePositioningEnv posEnv = new SimplePositioningEnv() {
				@Override
				public void onPositionChanged(int index, int x) {
					setup.elX[index] = x;
					setup.elY[index] = line0Top + drawings.get(index).getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
				}
				@Override
				public int middleX(int index) {
					return drawings.get(index).getMiddleX();
				}
			};
			strategy.calculatePositions(posEnv, strategy, params, false);
			setup.linesPaddingLeft = posEnv.currentPaddingLeft;
			// update overlays positions
			setup.overlayX = new int[arcs.size()+groups.size()];
			setup.overlayY = new int[arcs.size()+groups.size()];
			int ovIndex = 0;
			for (OverlayDef<JoinArc> overlayDef : arcs) {
				overlayDef.overlay.positionChanged(
					drawings.get(overlayDef.startIndex), 
					setup.elX[overlayDef.startIndex], setup.elY[overlayDef.startIndex]
				);
				overlayDef.overlay.positionChanged(
					drawings.get(overlayDef.endIndex), 
					setup.elX[overlayDef.endIndex], setup.elY[overlayDef.endIndex]
				);
				insertPositionedOverlay(setup, ovIndex++, overlayDef.overlay);
			}
			for (OverlayDef<NotesGroup> overlayDef : groups) {
				for(int elIndex = overlayDef.startIndex; elIndex <= overlayDef.endIndex; elIndex++) {
					overlayDef.overlay.positionChanged(
						drawings.get(elIndex),
						setup.elX[elIndex],
						setup.elY[elIndex]
					);
				}
				insertPositionedOverlay(setup, ovIndex++, overlayDef.overlay);
			}
			return setup;
		}

		private static void insertPositionedOverlay(DrawingSetup setup, int ovIndex, ElementsOverlay overlay) {
			setup.overlays.add(ovIndex, overlay);
			setup.overlayX[ovIndex] = overlay.left();
			setup.overlayY[ovIndex] = overlay.top();
		}
		
		private static class PositioningStrategy extends ScorePositioningStrategy implements SpacingEnv {
			private DrawingSetup setup;
			
			private PositioningStrategy(Context ctx, DrawingSetup setup) {
				super(ctx);
				this.setup = setup;
			}
			
			@Override
			public void updateSheetParams(int elementIndex, SheetParams sheetParams) {					
			}
			@Override
			public boolean isValid(int index) {
				return index >= 0 && index < setup.elements.size();
			}
			@Override
			public SheetAlignedElement getModel(int index) {
				return setup.elements.get(index);
			}
		};
		
		/**
		 * Stores value provided {@link PositioningEnv#notesAreaPaddingLeftChanged(int)} in field
		 */
		private static abstract class SimplePositioningEnv implements PositioningEnv {
			int currentPaddingLeft;

			@Override
			public void notesAreaPaddingLeftChanged(int paddingLeft) {
				currentPaddingLeft = paddingLeft;
			}
			
			@Override
			public void preVisit(int index) {
			}			
		}
	}

	public void setLoadingIcon(Drawable loadingIcon) {
		if(this.loadingIcon != null) {
			this.loadingIcon.setCallback(null);
		}
		this.loadingIcon = loadingIcon;
		if(this.loadingIcon != null)
			loadingIcon.setCallback(this);
	}

	@Override
	public void invalidateDrawable(Drawable who) {
		invalidateSelf();
	}

	@Override
	public void scheduleDrawable(Drawable who, Runnable what, long when) {
		scheduleSelf(what, when);
	}

	@Override
	public void unscheduleDrawable(Drawable who, Runnable what) {
		unscheduleSelf(what);
	}

}
