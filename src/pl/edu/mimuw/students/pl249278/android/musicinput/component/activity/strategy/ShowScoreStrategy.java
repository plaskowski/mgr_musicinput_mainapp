package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ScoreHelper.middleX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart.TOP_EDGE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateMargins;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LayoutParamsHelper.updateSize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
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
import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class ShowScoreStrategy extends Activity {
	private static final LogUtils log = new LogUtils(ShowScoreStrategy.class);
	protected List<SheetAlignedElementView> elementViews = new ArrayList<SheetAlignedElementView>();
	protected ArrayList<SheetElementView<SheetElement>> overlaysViews = new ArrayList<SheetElementView<SheetElement>>();
	protected SheetParams sheetParams;
	
	private float defaultSpacingBaseFactor;
	protected int minPossibleValue;
	private float minDrawSpacingFactor;
	protected float notesAreaHorizontalPaddingFactor;
	private float afterTimeDividerVisualSpacingFactor;
	protected int MIN_DRAW_SPACING;
	
	protected Sheet5LinesView lines;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sheetParams = new SheetParams(
			getResources().getInteger(R.integer.lineThickness),
			getResources().getInteger(R.integer.linespaceThickness)
		);
		defaultSpacingBaseFactor = readParametrizedFactor(R.string.defaultTimeSpacingBaseFactor);
		minPossibleValue = getResources().getInteger(R.integer.minNotePossibleValue) + 1;
		minDrawSpacingFactor = readParametrizedFactor(R.string.minDrawSpacing);
		afterTimeDividerVisualSpacingFactor = readParametrizedFactor(R.string.timeDividerDrawAfterSpacingFactor);
		notesAreaHorizontalPaddingFactor = readParametrizedFactor(R.string.notesAreaHorizontalPadding);
	}
	
	/** Updates {@link #MIN_DRAW_SPACING} */
	protected void onScaleChanged() {
		MIN_DRAW_SPACING = (int) (minDrawSpacingFactor*sheetParams.getScale());
	}
	
	protected float readParametrizedFactor(int stringResId) {
		return sheetParams.readParametrizedFactor(getResources().getString(stringResId));
	}
	
	protected int computeTimeSpacingBase(int timeDividerIndex, int lastTimeElementIndex, boolean refreshSheetParams) {
		if(refreshSheetParams) {
			elementViews.get(timeDividerIndex).setSheetParams(sheetParams); // update left TimeDivider
		}
		int spacingBase = (int) (defaultSpacingBaseFactor * sheetParams.getScale()); // calculate default spacing base
		int baseLength = length(0, minPossibleValue);
		int firstEl = timeDividerIndex+1;
		if(refreshSheetParams && firstEl < elementViews.size()) { // update first element of Time if present
			elementViews.get(firstEl).setSheetParams(sheetParams); 
		}
		for(int i = firstEl; i <= lastTimeElementIndex; i++) { // for each element inside Time 
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
			spacingBase = (int) Math.max(
				spacingBase,
				minSpacing * baseLength / el.model().getElementSpec().spacingLength(minPossibleValue)
			);
		}
		return spacingBase;
	}
	
	protected int timeDividerSpacing(int timeDividerIndex, boolean updateSheetParams) {
		SheetAlignedElementView v = elementViews.get(timeDividerIndex);
		if(updateSheetParams) v.setSheetParams(sheetParams);
		int minSpacing = v.model().collisionRegionRight()-v.model().getMiddleX()+(int) (afterTimeDividerVisualSpacingFactor*sheetParams.getScale());
		if(timeDividerIndex + 1 < elementViews.size()) {
			SheetAlignedElementView firstTimeEl = elementViews.get(timeDividerIndex + 1);
			if(updateSheetParams) firstTimeEl.setSheetParams(sheetParams);
			minSpacing += firstTimeEl.model().getMiddleX()-firstTimeEl.model().collisionRegionLeft();
		}
		return minSpacing;
	}
	
	protected int afterElementSpacing(int timeDividerIndex, int timeSpacingBase, SheetAlignedElement sheetAlignedElement) {
		ElementSpec elementSpec = sheetAlignedElement.getElementSpec();
		if(elementSpec.getType() == ElementType.TIMES_DIVIDER) {
			return timeDividerSpacing(timeDividerIndex, false);
		} else {
			return length2spacing(timeSpacingBase, elementSpec.spacingLength(minPossibleValue), minPossibleValue);
		}
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
			if(arcStart == null && elementI <= endIndex && JoinArc.canStrartJA(spec)) {
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
		parent.addView(elementView);
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
	
	private void updateOverlayPosition(ElementsOverlay overlay, SheetElementView<SheetElement> ovView) {
		int left = overlay.left()-ovView.getPaddingLeft();
		int top = line0Top() + overlay.top()-ovView.getPaddingTop();
		updatePosition(ovView, left, top);
	}	
	
	protected static int length2spacing(int spacingBase, double lengthInMU, int measureUnit) {
		int baseLength = length(0, measureUnit);
		return (int) (spacingBase * lengthInMU / baseLength);
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
	
	protected int line0Top() {
		return top(lines) + lines.getPaddingTop();
	}
	
	protected static int left(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
	}
	protected static int top(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
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
	
	protected void updatePosition(View v, Integer left, Integer top) {
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
	
	protected void dispatchPositionChanged(ElementsOverlay overlay, SheetAlignedElementView view) {
		overlay.positionChanged(
			view.model(), 
			left(view) + view.getPaddingLeft(), 
			top(view) + view.getPaddingTop() - line0Top()
		);
	}
	
}
