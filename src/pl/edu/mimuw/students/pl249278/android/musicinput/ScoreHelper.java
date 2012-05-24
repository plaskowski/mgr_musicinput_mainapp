package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.length;
import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.DrawingModelFactory.CreationException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.SheetAlignedElementView;

public class ScoreHelper {
	public static int timeCapacity(TimeStep timeStep, int measureBaseUnit) {
		if(timeStep == null)
			return Integer.MAX_VALUE;
		else 
			return length(timeStep.getTempoBaseLength(), measureBaseUnit)*timeStep.getBaseMultiplier();
	}
	
	public static int noteOrientation(NoteSpec spec, ScoreVisualizationConfig.DisplayMode mode) {
		int orientation;
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
		return orientation;
	}
	
	public static ElementSpec.NormalNote elementSpecNN(NoteSpec spec, ScoreVisualizationConfig.DisplayMode mode) {
		return new ElementSpec.NormalNote(spec, noteOrientation(spec, mode));
	}

	public static abstract class InsertDivided extends DivideLengthStrategy {
		private int insertIndex;
		private int total;

		public void insertDivided(int insertIndex, int capToFill, boolean multipleDots, int minPossibleLength) throws CreationException {
			this.insertIndex = insertIndex;
			this.total = 0;
			divide(capToFill, multipleDots, minPossibleLength);
		}
		
		@Override
		protected void handle(int baseLength, int dotExt) throws CreationException {
			handle(insertIndex+(total++), baseLength, dotExt);
		}
		
		protected abstract void handle(int atIndex, int baseLength, int dotExt) throws CreationException;

		public int getTotal() {
			return total;
		}
	}
	
	public static abstract class DivideLengthStrategy {
		public void divide(int capToFill, boolean multipleDots, int minPossibleLength) throws CreationException {
			for(int pLength = 0; pLength <= minPossibleLength; pLength++) {
				int bitIndex = (minPossibleLength-pLength);
				if(IntUtils.getFlag(capToFill, bitIndex) == 1) {
					int baseLength = pLength;
					int dotExt = 0;
					for(pLength = pLength+1; pLength <= minPossibleLength; pLength++) {
						bitIndex = (minPossibleLength-pLength);
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
	
	/**
	 * @return SheetAlignedElement horizontal middle in view coordinates
	 */
	public static int middleX(SheetAlignedElementView view) {
		return view.getPaddingLeft()+view.model().getMiddleX();
	}
}
