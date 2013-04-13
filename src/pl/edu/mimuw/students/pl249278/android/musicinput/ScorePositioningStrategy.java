package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.TimeDivider;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NoteStemAndFlag;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;

public class ScorePositioningStrategy {
	private int minPossibleValue;
	private float minDrawSpacingFactor;
	private float afterTimeDividerVisualSpacingFactor;
	protected float notesAreaHorizontalPaddingFactor;
	private float minSpacingBaseFactor;
	
	public static interface PositioningEnv {
		int middleX(int index);
		void notesAreaPaddingLeftChanged(int paddingLeft);
		void onPositionChanged(int index, int x);
		void preVisit(int index);
	}
	
	public static interface SpacingEnv {
		boolean isValid(int index);
		SheetAlignedElement getModel(int index);
		void updateSheetParams(int index, SheetParams sheetParams);
	}
	
	public ScorePositioningStrategy(Context ctx) {
		Resources res = ctx.getResources();
		SheetParams params = new SheetParams(
			res.getInteger(R.integer.lineThickness),
			res.getInteger(R.integer.linespaceThickness));
		minPossibleValue = res.getInteger(R.integer.minNotePossibleValue) + 1;
		minDrawSpacingFactor = params.readParametrizedFactor(res.getString(R.string.minDrawSpacing));
		afterTimeDividerVisualSpacingFactor = params.readParametrizedFactor(res.getString(R.string.timeDividerDrawAfterSpacingFactor));
		notesAreaHorizontalPaddingFactor = params.readParametrizedFactor(res.getString(R.string.notesAreaHorizontalPadding));
		minSpacingBaseFactor = params.readParametrizedFactor(res.getString(R.string.minSpacingBaseFactor));
	}
	
	public void calculatePositions(PositioningEnv posEnv, SpacingEnv env, SheetParams sheetParams, boolean updateSheetParams) {
		if(updateSheetParams) {
			env.updateSheetParams(0, sheetParams);
		}
		int paddingLeft = (int) (notesAreaHorizontalPaddingFactor * sheetParams.getScale())
		 + posEnv.middleX(0);
		posEnv.notesAreaPaddingLeftChanged(paddingLeft);
		
		int spacingAfter = paddingLeft;
		int x = 0;
		int currentSpacingBase = 0;
		for(int i = 0; env.isValid(i); i++) {
			x += spacingAfter;
			posEnv.preVisit(i);
			SheetAlignedElement model = env.getModel(i);
			if(model.getElementSpec().getType() == ElementType.TIMES_DIVIDER || i == 0) {
				int lastTimeEl = i+1;
				for(; env.isValid(lastTimeEl); lastTimeEl++) {
					if(env.getModel(lastTimeEl).getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
						break;
					}
				}
				lastTimeEl--;
				currentSpacingBase = computeTimeSpacingBase(env, i, lastTimeEl, sheetParams, updateSheetParams);
			}
			spacingAfter = afterElementSpacing(env, currentSpacingBase, i, sheetParams);
			int xpos = x-posEnv.middleX(i);
			posEnv.onPositionChanged(i, xpos);
		}
	}
	
	/**
	 * @param elementIndex index of {@link TimeDivider} or it's predecessor
	 * @return spacing fitted for space between element and TimeDivider
	 */
	public int timeDividerSpacing(SpacingEnv env, int elementIndex, SheetParams sheetParams, boolean updateSheetParams) {
		if(updateSheetParams) {
			env.updateSheetParams(elementIndex, sheetParams);
		}
		SheetAlignedElement dividerModel = env.getModel(elementIndex);
		int minSpacing = dividerModel.collisionRegionRight()-dividerModel.getMiddleX()+(int) (afterTimeDividerVisualSpacingFactor*sheetParams.getScale());
		if(env.isValid(elementIndex + 1)) {			
			SheetAlignedElement firstElModel = env.getModel(elementIndex + 1);
			if(updateSheetParams) {
				env.updateSheetParams(elementIndex + 1, sheetParams);
			}
			minSpacing += firstElModel.getMiddleX()-firstElModel.collisionRegionLeft();
		}
		return minSpacing;
	}
	
	public int afterElementSpacing(SpacingEnv env, int timeSpacingBase, int elementIndex, SheetParams sheetParams) {
		SheetAlignedElement elementModel = env.getModel(elementIndex);
		ElementSpec elementSpec = elementModel.getElementSpec();
		if(elementSpec.getType() == ElementType.TIMES_DIVIDER) {
			return timeDividerSpacing(env, elementIndex, sheetParams, false);
		} else {
			int spacing = length2spacing(timeSpacingBase, elementSpec.spacingLength(minPossibleValue), minPossibleValue);
			if(env.isValid(elementIndex + 1) && env.getModel(elementIndex + 1).getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
				spacing = Math.max(spacing, timeDividerSpacing(env, elementIndex, sheetParams, false));
			}
			return spacing;
		}
	}
	
	private ArrayList<Rect> areas = new ArrayList<Rect>(), nextAreas = new ArrayList<Rect>(), rectsPool = new ArrayList<Rect>();
	
	private static final int SPACING_BASE_UNIT = 3;

	public int computeTimeSpacingBase(SpacingEnv env, int firstElementIndex, int lastElementIndex, SheetParams params, boolean refreshSheetParams) {
		if(env.getModel(firstElementIndex).getElementSpec().getType() == ElementType.TIMES_DIVIDER) {
			if(refreshSheetParams) {
				env.updateSheetParams(firstElementIndex, params); // update left TimeDivider
			}
			firstElementIndex++;
		}
		if(refreshSheetParams && env.isValid(firstElementIndex)) { // update first element of Time if present
			env.updateSheetParams(firstElementIndex, params);
		}
		int resultSpacingBase = 0;
		int baseLength = length(SPACING_BASE_UNIT, minPossibleValue);
		int minDrawSpacing = (int) (minDrawSpacingFactor * params.getScale());
		// prepare areas of first element
		if(firstElementIndex <= lastElementIndex) {
			SheetAlignedElement model = env.getModel(firstElementIndex);
			getAreas(model, areas, rectsPool);
		}
		for(int i = firstElementIndex; i <= lastElementIndex; i++) { // for each element inside Time 
			SheetAlignedElement model = env.getModel(i);
			/** minimal visual spacing between 2 element's middles so that they don't collide */
			int minSpacing;
			int middleX = model.getMiddleX();
			if(env.isValid(i+1)) {
				if(refreshSheetParams) {
					env.updateSheetParams(i+1, params);
				}
				SheetAlignedElement next = env.getModel(i+1);
				int nextMiddleX = next.getMiddleX();
				getAreas(next, nextAreas, rectsPool);
				/* 
				 * Obliczam o ile muszę odsunąć zestaw obszarów nextAreas w prawo 
				 * aby był minDrawSpacing odstęp między nimi a zestawem areas
				 */
				int areasTotal = areas.size(), nextAreasTotal = nextAreas.size();
				int maxRight = Integer.MIN_VALUE;
				for(int areaIndex = 0; areaIndex < areasTotal; areaIndex++) {
					Rect area = areas.get(areaIndex);
					area.inset(-minDrawSpacing, -minDrawSpacing);
					area.offset(nextMiddleX - middleX, 0);
					maxRight = Math.max(area.right, maxRight);
				}
				int totalMoveDist = 0;
				/** Zapewniam, iż laseczka następnej nuty będzie nie cześniej niż koniec czegokolwiek z aktualnej nuty */
				int stemMiddle = next.getHorizontalOffset(NoteStemAndFlag.HLINE_STEM_MIDDLE);
				if(stemMiddle != SheetAlignedElement.HLINE_UNSPECIFIED) {
					int moveDueToStem = maxRight - stemMiddle;
					if(moveDueToStem > 0) {
						totalMoveDist += moveDueToStem;
						translate(areas, -moveDueToStem, 0);
					}
				}
				int requiredMoveDist;
				do {
					requiredMoveDist = 0;
					for(int areaIndex = 0; areaIndex < areasTotal; areaIndex++) {
						Rect area = areas.get(areaIndex);
						for(int nextAreaIndex = 0; nextAreaIndex < nextAreasTotal; nextAreaIndex++) {
							Rect nextArea = nextAreas.get(nextAreaIndex);
							if(Rect.intersects(area, nextArea)) {
								int moveDist = area.right - nextArea.left;
								if(moveDist > requiredMoveDist) {
									requiredMoveDist = moveDist;
								}
							}
						}
					}
					translate(areas, -requiredMoveDist, 0);
					totalMoveDist += requiredMoveDist;
				} while(requiredMoveDist > 0);
				minSpacing = Math.max(
					totalMoveDist,
					minDrawSpacing);
				clear(areas, rectsPool);
				// switch references to save areas of next for next iteration
				ArrayList<Rect> temp = areas;
				areas = nextAreas;
				nextAreas = temp;				
			} else {
				int maxRight = 0;
				int areasTotal = areas.size();
				for(int areaIndex = 0; areaIndex < areasTotal; areaIndex++) {
					Rect area = areas.get(areaIndex);
					maxRight = Math.max(area.right, maxRight);
				}
				minSpacing = Math.max(
					maxRight - middleX + minDrawSpacing,
					minDrawSpacing
				);
			}
			int elemSpacingLength = model.getElementSpec().spacingLength(minPossibleValue);
			int spacingBase;
			if(elemSpacingLength <= baseLength) {
				spacingBase = minSpacing * baseLength / elemSpacingLength;				
			} else {
				double times = log2(elemSpacingLength/baseLength);
				spacingBase = (int) (minSpacing * 2 / (2 + times));
			}
			resultSpacingBase = Math.max(spacingBase, resultSpacingBase);
		}
		return Math.max(resultSpacingBase, (int) (
			params.getScale() * minSpacingBaseFactor));
	}

	private static void getAreas(SheetAlignedElement model, ArrayList<Rect> areas, ArrayList<Rect> rectsPool) {
		clear(areas, rectsPool);
		model.getCollisionRegions(areas, rectsPool);
		translate(areas, 0, model.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE));
	}

	private static void clear(ArrayList<Rect> areas, ArrayList<Rect> rectsPool) {
		rectsPool.addAll(areas);
		areas.clear();
	}
	
	private static void translate(ArrayList<Rect> rects, int dx, int dy) {
		int total = rects.size();
		for(int i = 0; i < total; i++) {
			rects.get(i).offset(dx, dy);
		}
	}
	
	private static int length2spacing(int spacingBase, double lengthInMU, int measureUnit) {
		int baseLength = length(SPACING_BASE_UNIT, measureUnit);
		if(lengthInMU <= baseLength) {
			return (int) (spacingBase * lengthInMU / baseLength);
		} else {
			return (int) ((1 + log2(lengthInMU/baseLength) / 2) * spacingBase);
		}
	}

	public float getNotesAreaHorizontalPaddingFactor() {
		return notesAreaHorizontalPaddingFactor;
	}
	
	private static final double LOG_2 = Math.log(2);
	
	private static double log2(double x) {
		return Math.log(x)/LOG_2;
	}

}