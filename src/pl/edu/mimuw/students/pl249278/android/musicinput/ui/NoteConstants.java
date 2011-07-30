package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;

public class NoteConstants {
	public static final int ANCHOR_TYPE_LINE = 0;
	public static final int ANCHOR_TYPE_LINESPACE = 1;
	
	public static final int LEN_FULLNOTE = 0;
	public static final int LEN_HALFNOTE = 1;
	public static final int LEN_QUATERNOTE = 2;
	
	public static enum NoteModifier {
		DOT,
		SHARP, // krzyżyk
		FLAT, // bemol
		NATURAL // kasownik
	}
	
	public static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
	
	public static int anchorType(int anchorIndex) {
		return Math.abs(anchorIndex%2);
	}

	public static int anchorIndex(int withinTypeIndex, int type) {
		return type == ANCHOR_TYPE_LINE ? withinTypeIndex * 2 : withinTypeIndex * 2 +1 ;
	}
	
	public static int anchorTypedIndex(int absIndex) {
		return absIndex < 0 ? (absIndex-1)/2 : absIndex/2;
	}

	public static boolean isUpsdown(int noteHeight) {
		return noteHeight <= anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
	}

	public static int stemEnd(NoteSpec noteSpec) {
		// FIXME real logic for discovering anchors
		return noteSpec.positon() + (noteSpec.getOrientation() == NoteSpec.ORIENT_UP ? -7 : 7);
	}

	public static boolean hasStem(NoteSpec spec) {
		return hasStem(spec.length());
	}
	public static boolean hasStem(int noteLength) {
		return noteLength != 0;
	}
}
