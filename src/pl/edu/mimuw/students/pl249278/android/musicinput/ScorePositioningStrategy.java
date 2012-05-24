package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import android.content.Context;
import android.content.res.Resources;

public class ScorePositioningStrategy {
	private float defaultSpacingBaseFactor;
	private int minPossibleValue;
	private float minDrawSpacingFactor;
	private float afterTimeDividerVisualSpacingFactor;
	protected float notesAreaHorizontalPaddingFactor;
	
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
		defaultSpacingBaseFactor = 	params.readParametrizedFactor(res.getString(R.string.defaultTimeSpacingBaseFactor));
		minPossibleValue = res.getInteger(R.integer.minNotePossibleValue) + 1;
		minDrawSpacingFactor = params.readParametrizedFactor(res.getString(R.string.minDrawSpacing));
		afterTimeDividerVisualSpacingFactor = params.readParametrizedFactor(res.getString(R.string.timeDividerDrawAfterSpacingFactor));
		notesAreaHorizontalPaddingFactor = params.readParametrizedFactor(res.getString(R.string.notesAreaHorizontalPadding));
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
		int currentTimeDividerIndex = 0;
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
				currentTimeDividerIndex = i;
				currentSpacingBase = computeTimeSpacingBase(env, i, lastTimeEl, sheetParams, updateSheetParams);
			}
			spacingAfter = afterElementSpacing(env, currentTimeDividerIndex, currentSpacingBase, model, sheetParams);
			int xpos = x-posEnv.middleX(i);
			posEnv.onPositionChanged(i, xpos);
		}
	}
	
	public int timeDividerSpacing(SpacingEnv env, int timeDividerIndex, SheetParams sheetParams, boolean updateSheetParams) {
		if(updateSheetParams) {
			env.updateSheetParams(timeDividerIndex, sheetParams);
		}
		SheetAlignedElement dividerModel = env.getModel(timeDividerIndex);
		int minSpacing = dividerModel.collisionRegionRight()-dividerModel.getMiddleX()+(int) (afterTimeDividerVisualSpacingFactor*sheetParams.getScale());
		if(env.isValid(timeDividerIndex + 1)) {			
			SheetAlignedElement firstElModel = env.getModel(timeDividerIndex + 1);
			if(updateSheetParams) {
				env.updateSheetParams(timeDividerIndex + 1, sheetParams);
			}
			minSpacing += firstElModel.getMiddleX()-firstElModel.collisionRegionLeft();
		}
		return minSpacing;
	}
	
	public int afterElementSpacing(SpacingEnv env, int timeDividerIndex, int timeSpacingBase, SheetAlignedElement sheetAlignedElement, SheetParams sheetParams) {
		ElementSpec elementSpec = sheetAlignedElement.getElementSpec();
		if(elementSpec.getType() == ElementType.TIMES_DIVIDER) {
			return timeDividerSpacing(env, timeDividerIndex, sheetParams, false);
		} else {
			return length2spacing(timeSpacingBase, elementSpec.spacingLength(minPossibleValue), minPossibleValue);
		}
	}

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
		int spacingBase = (int) (defaultSpacingBaseFactor * params.getScale()); // calculate default spacing base
		int baseLength = length(0, minPossibleValue);
		int minDrawSpacing = (int) (minDrawSpacingFactor * params.getScale());
		for(int i = firstElementIndex; i <= lastElementIndex; i++) { // for each element inside Time 
			SheetAlignedElement model = env.getModel(i);
			/** minimal visual spacing between 2 element's middles so that they don't collide */
			int minSpacing = model.collisionRegionRight()-model.getMiddleX() + minDrawSpacing;
			if(env.isValid(i+1)) {
				if(refreshSheetParams) {
					env.updateSheetParams(i+1, params);
				}
				SheetAlignedElement next = env.getModel(i+1);
				minSpacing += next.getMiddleX()-next.collisionRegionLeft();
			}
			spacingBase = (int) Math.max(
				spacingBase,
				minSpacing * baseLength / model.getElementSpec().spacingLength(minPossibleValue)
			);
		}
		return spacingBase;
	}
	
	private static int length2spacing(int spacingBase, double lengthInMU, int measureUnit) {
		int baseLength = length(0, measureUnit);
		return (int) (spacingBase * lengthInMU / baseLength);
	}

	public float getNotesAreaHorizontalPaddingFactor() {
		return notesAreaHorizontalPaddingFactor;
	}

}