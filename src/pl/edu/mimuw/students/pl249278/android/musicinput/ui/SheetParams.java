package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;
	
public class SheetParams implements SheetVisualParams {
	
	private int lineFactor;
	private int linespacingFactor;
	private float scale = 1;
	private int minSpaceAnchor;
	private int maxSpaceAnchor;
		
	private TimeSpec.TimeStep timeStep;
	private NoteConstants.Clef clef;
	private NoteConstants.KeySignature keySignature;
	private DisplayMode displayMode = DisplayMode.NORMAL;
		
	public SheetParams(int lineFactor, int linespacingFactor) {
		super();
		this.lineFactor = lineFactor;
		this.linespacingFactor = linespacingFactor;
	}

	/** Copy constructor */
	public SheetParams(SheetParams sheetParams) {
		this.lineFactor = sheetParams.lineFactor;
		this.linespacingFactor = sheetParams.linespacingFactor;
	}

	public float readParametrizedFactor(String factorStringRep) {
		String rawValue = factorStringRep;
		float factor = Float.parseFloat(rawValue.substring(0, rawValue.length()-1));
		char c = rawValue.charAt(rawValue.length()-1);
		if(c == 'l') {
			return factor*this.getLineFactor();
		} else if(c == 's') {
			return factor*this.getLinespacingFactor();
		}
		throw new UnsupportedOperationException();
	}
	
	private static int line0absIndex = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
		
	/* (non-Javadoc)
	 * @see pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetVisualParams#anchorOffset(int, pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart)
	 */
	@Override
	public int anchorOffset(int anchorAbsIndex, AnchorPart part) {
		int lineThickness = (int) (lineFactor * scale);
		int linespacingThickness = (int) (linespacingFactor * scale);
		int anchorSize = NoteConstants.anchorType(anchorAbsIndex) == NoteConstants.ANCHOR_TYPE_LINE ? lineThickness : linespacingThickness;
			
		int dist = anchorAbsIndex-line0absIndex;
		int linesBetweenSize = ((dist+1)/2)*lineThickness;
		int spacesetweenSize = (dist/2)*linespacingThickness;

		int partDiff = 0;
		switch(part) {
		case BOTTOM_EDGE:
			partDiff = anchorSize;
			break;
		case MIDDLE:
			partDiff = anchorSize/2;
			break;
		}
			
		return (linesBetweenSize + spacesetweenSize) + (partDiff - (dist < 0 ? anchorSize : 0));
	}	

	public float getScale() {
		return scale;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getLineFactor() {
		return lineFactor;
	}

	public int getLinespacingFactor() {
		return linespacingFactor;
	}

	/* (non-Javadoc)
	 * @see pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetVisualParams#getLineThickness()
	 */
	@Override
	public int getLineThickness() {
		return (int) (lineFactor*scale);
	}

	/* (non-Javadoc)
	 * @see pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetVisualParams#getLinespacingThickness()
	 */
	@Override
	public int getLinespacingThickness() {
		return (int) (linespacingFactor*scale);
	}

	public int getMinSpaceAnchor() {
		return minSpaceAnchor;
	}

	public void setMinSpaceAnchor(int minSpaceAnchor) {
		this.minSpaceAnchor = minSpaceAnchor;
	}

	public int getMaxSpaceAnchor() {
		return maxSpaceAnchor;
	}

	public void setMaxSpaceAnchor(int maxSpaceAnchor) {
		this.maxSpaceAnchor = maxSpaceAnchor;
	}

	public NoteConstants.Clef getClef() {
		return clef;
	}

	public void setClef(NoteConstants.Clef clef) {
		this.clef = clef;
	}
		
	public TimeSpec.TimeStep getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(TimeSpec.TimeStep timeStep) {
		this.timeStep = timeStep;
	}

	public NoteConstants.KeySignature getKeySignature() {
		return keySignature;
	}

	public void setKeySignature(NoteConstants.KeySignature keySignature) {
		this.keySignature = keySignature;
	}

	/* (non-Javadoc)
	 * @see pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetVisualParams#getDisplayMode()
	 */
	@Override
	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
	}

}
