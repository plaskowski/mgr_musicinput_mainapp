package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity;

import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.pow2;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.middleX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart.TOP_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateSizeDirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.ScorePositioningStrategy.SpacingEnv;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementsOverlay.Observer;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.JoinArc;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotesGroup.GroupBuilder;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.Sheet5LinesView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetElementView;

import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.HorizontalScrollView;
import android.widget.AbsoluteLayout.LayoutParams;

import androidx.fragment.app.FragmentActivity;

@SuppressWarnings("deprecation")
public class ShowScoreActivity extends FragmentActivity {
	private static final LogUtils log = new LogUtils(ShowScoreActivity.class);
	protected List<SheetAlignedElementView> elementViews = new ArrayList<SheetAlignedElementView>();
	protected ArrayList<SheetElementView<SheetElement>> overlaysViews = new ArrayList<SheetElementView<SheetElement>>();
	protected SheetParams sheetParams;

	protected int minPossibleValue;
	protected ScorePositioningStrategy positioningStrategy;

	protected HorizontalScrollView hscroll;
	protected Sheet5LinesView lines;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sheetParams = new SheetParams(
			getResources().getInteger(R.integer.lineThickness),
			getResources().getInteger(R.integer.linespaceThickness)
		);
		positioningStrategy = new ScorePositioningStrategy(this.getApplicationContext());
		minPossibleValue = getResources().getInteger(R.integer.minNotePossibleValue) + 1;
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		hscroll = (HorizontalScrollView) findViewById(R.id.ShowScore_horizontal_scrollview);
	}

	/** Does nothing */
	protected void onScaleChanged() {
	}

	protected float readParametrizedFactor(int stringResId) {
		return sheetParams.readParametrizedFactor(getResources().getString(stringResId));
	}

	protected SpacingEnv spacingEnv = new SpacingEnv() {
		@Override
		public void updateSheetParams(int index, SheetParams sheetParams) {
			elementViews.get(index).setSheetParams(sheetParams);
		}
		@Override
		public boolean isValid(int index) {
			return index >= 0 && index < elementViews.size();
		}

		@Override
		public SheetAlignedElement getModel(int index) {
			return elementViews.get(index).model();
		}
	};

	protected int computeTimeSpacingBase(int timeDividerIndex, int lastTimeElementIndex, boolean refreshSheetParams) {
		return positioningStrategy.computeTimeSpacingBase(spacingEnv, timeDividerIndex, lastTimeElementIndex, sheetParams, refreshSheetParams);
	}

	protected int timeDividerSpacing(int timeDividerIndex, boolean updateSheetParams) {
		return positioningStrategy.timeDividerSpacing(spacingEnv, timeDividerIndex, sheetParams, updateSheetParams);
	}

	protected int afterElementSpacing(int timeSpacingBase, int elementIndex) {
		return positioningStrategy.afterElementSpacing(spacingEnv, timeSpacingBase, elementIndex, sheetParams);
	}

	/**
	 * build greedily NotesGroups starting from startIndex
	 * @param endIndex maximal index of first element of NotesGroup
	 * @param instantRedraw do we want to reposition views immediately after changing their drawing model
	 */
	protected void buildNoteGroups(int startIndex, int endIndex, ViewGroup parent, Paint paint, float drawRadius) throws CreationException {
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
					addOverlayView(group, parent, paint, drawRadius);
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
				updateYPosition(view, sheetElementY(view));
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
	protected void buildJoinArcs(int startIndex, int endIndex, ViewGroup parent, Paint paint, float drawRadius) throws CreationException {
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
					addOverlayView(arc, parent, paint, drawRadius);
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
			if(arcStart == null && elementI <= endIndex && JoinArc.canStartJA(spec)) {
				arcStart = view;
				if(elementI == extendedEndIndex) {
					extendedEndIndex = Math.min(extendedEndIndex+1, lastPossibleEl);
				}
			}
		}
	}

	protected void addOverlayView(final ElementsOverlay overlay, ViewGroup parent, Paint paint, float drawRadius) {
		SheetElementView<SheetElement> elementView;
		elementView = new SheetElementView<SheetElement>(this, overlay);
		elementView.setPaint(paint, drawRadius);
		elementView.setSheetParams(sheetParams);
		overlay.setTag(elementView);
		overlaysViews.add(elementView);
		parent.addView(elementView, 1);
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
				updateSizeDirect(ovView, ovView.measureWidth(), ovView.measureHeight());
			}
		});
	}

	private void updateOverlayPosition(ElementsOverlay overlay, SheetElementView<SheetElement> ovView) {
		int left = overlay.left()-ovView.getPaddingLeft();
		int top = line0Top() + overlay.top()-ovView.getPaddingTop();
		updatePosition(ovView, left, top);
	}

	private ArrayList<Rect> areas = new ArrayList<Rect>();
	private ArrayList<Rect> rectsPool = new ArrayList<Rect>();

	/**
	 * Finds which element (if any) was targeted by given DOWN event
	 * @param event DOWN event
	 * @return index in elementViews or -1
	 */
	protected int findPressedElementIndex(MotionEvent event) {
		// find leftmost element that is visible
		int i = 0;
		for(; i < elementViews.size(); i++) {
			View elView = elementViews.get(i);
			if(abs2visibleX(elView.getRight()) >= 0) {
				break;
			}
		}
		int minHitArea = getResources().getDimensionPixelSize(R.dimen.minHitArea);
		int touchX = (int) event.getX();
		int touchY = (int) event.getY();
		int minDist = Integer.MAX_VALUE;
		int minDistIndex = -1;
		for(; i < elementViews.size(); i++) {
			SheetAlignedElementView elView = elementViews.get(i);
			if(abs2visibleX(elView.getLeft()) > hscroll.getWidth()) {
				break;
			}
			rectsPool.addAll(areas);
			areas.clear();
			elView.model().getCollisionRegions(areas, rectsPool);
			int pL = elView.getPaddingLeft(), pT = elView.getPaddingTop();
			for(int arI = 0; arI < areas.size(); arI++) {
				Rect area = areas.get(arI);
				area.offset(left(elView) + pL, top(elView) + pT);
				// assure that it's size is at least hitArea x hitArea
				area.inset(
					Math.min(0, (area.width() - minHitArea)/2),
					Math.min(0, (area.height() - minHitArea)/2)
				);
				if(area.contains(touchX, touchY)) {
					// touch fits in hitArea
					int dist = pow2(touchX - area.centerX())
					 + pow2(touchY - area.centerY());
					if(dist < minDist) {
						minDist = dist;
						minDistIndex = i;
					}
				}
			}
		}
		return minDistIndex;
	}

	protected int sheetElementY(SheetElementView<?> v) {
		return line0Top() + v.getOffsetToAnchor(NoteConstants.anchorIndex(0, ANCHOR_TYPE_LINE), TOP_EDGE);
	}

	/**
	 * @return view's SheetAlignedElement horizontal middle in "sheet" view coordinates
	 */
	protected static int middleAbsoluteX(SheetAlignedElementView view) {
		return left(view)+middleX(view);
	}

	protected int abs2visibleX(int absoluteX) {
		return absoluteX - hscroll.getScrollX();
	}

	protected int line0Top() {
		return top(lines) + lines.getPaddingTop();
	}

	protected static int left(View view) {
		return ((AbsoluteLayout.LayoutParams) view.getLayoutParams()).x;
	}
	protected static int top(View view) {
		return ((AbsoluteLayout.LayoutParams) view.getLayoutParams()).y;
	}

	protected ElementSpec specAt(int elementIndex) {
		return elementViews.get(elementIndex).model().getElementSpec();
	}

	protected SheetAlignedElement createDrawingModel(ElementSpec elementSpec) throws CreationException {
		return DrawingModelFactory.createDrawingModel(this, elementSpec);
	}

	protected Map<SheetAlignedElementView, Set<ElementsOverlay>> bindMap = new HashMap<SheetAlignedElementView, Set<ElementsOverlay>>();
	protected void bind(ElementsOverlay overlay, SheetAlignedElementView view) {
		if(bindMap.get(view) == null) {
			bindMap.put(view, new LinkedHashSet<ElementsOverlay>());
		}
		bindMap.get(view).add(overlay);
		dispatchPositionChanged(overlay, view);
	}

	protected void updateXPosition(View v, int left) {
		AbsoluteLayout.LayoutParams params = (LayoutParams) v.getLayoutParams();
		params.x = left;
		onPositionUpdated(v, params);
	}

	protected void updateYPosition(View v, int top) {
		AbsoluteLayout.LayoutParams params = (LayoutParams) v.getLayoutParams();
		params.y = top;
		onPositionUpdated(v, params);
	}

	protected void updatePosition(View v, int left, int top) {
		AbsoluteLayout.LayoutParams params = (LayoutParams) v.getLayoutParams();
		params.x = left;
		params.y = top;
		onPositionUpdated(v, params);
	}

	private void onPositionUpdated(View v, AbsoluteLayout.LayoutParams params) {
		v.setLayoutParams(params);
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

	protected void dispatchPositionChanged(ElementsOverlay overlay, SheetAlignedElementView view) {
		overlay.positionChanged(
			view.model(),
			left(view) + view.getPaddingLeft(),
			top(view) + view.getPaddingTop() - line0Top()
		);
	}

}
