package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PositonSpec;

public class NoteConstants {
	public static final int ANCHOR_TYPE_LINE = 0;
	public static final int ANCHOR_TYPE_LINESPACE = 1;
	
	public static final int LEN_FULLNOTE = 0;
	public static final int LEN_HALFNOTE = 1;
	public static final int LEN_QUATERNOTE = 2;
	public static final Integer LEN_EIGHTNOTE = 3;
	public static final Integer LEN_SIXTEENNOTE = 4;
	
	public static final int ORIENT_UP = 0;
	public static final int ORIENT_DOWN = 1;
	
	public static enum NoteModifier {
		DOT,
		SHARP, // krzyżyk
		FLAT, // bemol
		NATURAL // kasownik
	}
	
	/** 
	 * klucz umieszczany na pięciolinii celem określenia położenia jednego dźwięku 
	 */
	public static enum Clef {
		VIOLIN,
		ALTO,
		BASS
	}
	
	/**
	 * tonacja (zbiór znaków chromatycznych umieszczanych za kluczem)
	 */
	public static enum KeySignature {
		C_DUR(),
		G_DUR(S(LINE0_ABSINDEX)),
		D_DUR(S(LINE0_ABSINDEX), S(SPACE1_ABSINDEX)),
		A_DUR(S(LINE0_ABSINDEX), S(SPACE1_ABSINDEX), S(SPACEm1_ABSINDEX)),
		F_DUR(F(LINE2_ABSINDEX)),
		B_DUR(F(LINE2_ABSINDEX), F(SPACE0_ABSINDEX)),
		ES_DUR(F(LINE2_ABSINDEX), F(SPACE0_ABSINDEX), F(SPACE2_ABSINDEX))
		// TODO add rest
		;
		
		private KeySignature(Accidental... accidentals) {
			this.accidentals = accidentals;
		}
		
		public final Accidental[] accidentals;
		
		public static class Accidental {
			public final int anchor;
			public final NoteModifier accidental;
			public Accidental(int anchor, NoteModifier accidental) {
				this.anchor = anchor;
				this.accidental = accidental;
			}
		}

		private static Accidental S(int anchor) {
			return acc(anchor, NoteModifier.SHARP);
		}
		private static Accidental F(int anchor) {
			return acc(anchor, NoteModifier.FLAT);
		}
		private static Accidental acc(int anchor, NoteModifier accidental) {
			return new Accidental(anchor, accidental);
		}
	}
	
	public static final int LINE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
	public static final int LINE2_ABSINDEX = NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
	public static final int LINE4_ABSINDEX = NoteConstants.anchorIndex(4, NoteConstants.ANCHOR_TYPE_LINE);
	public static final int SPACEm1_ABSINDEX = NoteConstants.anchorIndex(-1, NoteConstants.ANCHOR_TYPE_LINESPACE);
	public static final int SPACE0_ABSINDEX = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINESPACE);
	public static final int SPACE1_ABSINDEX = NoteConstants.anchorIndex(1, NoteConstants.ANCHOR_TYPE_LINESPACE);
	public static final int SPACE2_ABSINDEX = NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINESPACE);
	public static final int MIN_STEM_SPAN = 7;
	
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
		return noteHeight < anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
	}

	public static int stemEnd(PositonSpec noteSpec, int orientation) {
		// FIXME real logic for discovering anchors
		return noteSpec.positon() + (orientation == ORIENT_UP ? -MIN_STEM_SPAN : MIN_STEM_SPAN);
	}

	public static boolean hasStem(NoteSpec spec) {
		return hasStem(spec.length());
	}
	public static boolean hasStem(int noteLength) {
		return noteLength != 0;
	}

	public static int defaultOrientation(NoteSpec spec) {
		return isUpsdown(spec.positon()) ? ORIENT_DOWN : ORIENT_UP;
	}
}
