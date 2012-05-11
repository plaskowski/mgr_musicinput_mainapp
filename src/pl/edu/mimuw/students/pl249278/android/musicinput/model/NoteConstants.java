package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier.*;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.DiatonicScalePitch.*;

public class NoteConstants {
	public static final int ANCHOR_TYPE_LINE = 0;
	public static final int ANCHOR_TYPE_LINESPACE = 1;
	
	public static final int LEN_FULLNOTE = 0;
	public static final int LEN_HALFNOTE = 1;
	public static final int LEN_QUATERNOTE = 2;
	public static final Integer LEN_EIGHTNOTE = 3;
	public static final Integer LEN_SIXTEENNOTE = 4;
	
	/** stem is drawn above the head */
	public static final int ORIENT_UP = 0;
	/** stem is drawn below the head */
	public static final int ORIENT_DOWN = 1;
	
	/** 
	 * TODO call this rather an Accidental
	 */
	public static enum NoteModifier {
		SHARP, // krzyżyk
		FLAT, // bemol
		NATURAL // kasownik
	}
	
	/** 
	 * klucz umieszczany na pięciolinii celem określenia położenia jednego dźwięku 
	 */
	public static enum Clef {
		VIOLIN(new DiatonicPitch(DiatonicScalePitch.G, MIDDLE_C_OCTAVE), anchorIndex(3, ANCHOR_TYPE_LINE)),
		ALTO(new DiatonicPitch(DiatonicScalePitch.C, MIDDLE_C_OCTAVE), anchorIndex(2, ANCHOR_TYPE_LINE)),
		BASS(new DiatonicPitch(DiatonicScalePitch.F, MIDDLE_C_OCTAVE-1), anchorIndex(1, ANCHOR_TYPE_LINE));
		
		private Clef(DiatonicPitch diatonicNote, int anhorIndex) {
			this.diatonicNote = diatonicNote;
			this.anhorIndex = anhorIndex;
		}
		/**
		 * Which note it marks on staff
		 */
		public final DiatonicPitch diatonicNote;
		/**
		 * At what place on staff note is marked
		 */
		public final int anhorIndex;
	}
	
	/**
	 * tonacja (zbiór znaków chromatycznych umieszczanych za kluczem)
	 */
	public static enum KeySignature {
		CIS_DUR(SHARP, F, C, G, D, A, E, H),
		FIS_DUR(SHARP, F, C, G, D, A, E),
		H_DUR(SHARP, F, C, G, D, A),
		E_DUR(SHARP, F, C, G, D),
		A_DUR(SHARP, F, C, G),
		D_DUR(SHARP, F, C),
		G_DUR(SHARP, F),
		C_DUR(NoteModifier.NATURAL),
		D_MOLL(FLAT, H),
		G_MOLL(FLAT, H, E),
		C_MOLL(FLAT, H, E, A),
		F_MOLL(FLAT, H, E, A, D),
		B_MOLL(FLAT, H, E, A, D, G),
		ES_MOLL(FLAT, H, E, A, D, G, C),
		AS_MOLL(FLAT, H, E, A, D, G, C, F)
		;
		
		private KeySignature(NoteModifier modifier, DiatonicScalePitch... pitches) {
			this.modifier = modifier;
			this.pitches = pitches;
		}
		
		public final NoteModifier modifier;
		public final DiatonicScalePitch[] pitches;
	}
	
	/** scientific index of octave that is started by "middle C" on piano 88 keys keyboard */
	private static final int MIDDLE_C_OCTAVE = 4;
	
	public static class DiatonicPitch {
		public final DiatonicScalePitch basePitch;
		/** as in scientific notation */
		public final int octaveIndex;
		
		public DiatonicPitch(DiatonicScalePitch basePitch, int octaveIndex) {
			this.basePitch = basePitch;
			this.octaveIndex = octaveIndex;
		}
	}
	
	public static class Pitch {
		public final ChromaticScalePitch basePitch;
		public final int octaveIndex;
		
		public Pitch(ChromaticScalePitch basePitch, int octaveIndex) {
			this.basePitch = basePitch;
			this.octaveIndex = octaveIndex;
		}
	}
	
	public static enum DiatonicScalePitch {
		C (ChromaticScalePitch.C), 
		D (ChromaticScalePitch.D),
		E (ChromaticScalePitch.E), 
		F (ChromaticScalePitch.F), 
		G (ChromaticScalePitch.G), 
		A (ChromaticScalePitch.A), 
		H (ChromaticScalePitch.H);
		
		private DiatonicScalePitch(ChromaticScalePitch chromatic) {
			this.chromatic = chromatic;
		}
		public final ChromaticScalePitch chromatic;
	}
	
	public static enum ChromaticScalePitch {
		C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP, A, A_SHARP, H
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

	public static int defaultOrientation(int noteHeight) {
		return noteHeight < anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE) ? ORIENT_DOWN : ORIENT_UP;
	}

	public static int stemEnd(PositonSpec noteSpec, int orientation) {
		// FIXME real logic for discovering anchors
		return noteSpec.positon() + (orientation == ORIENT_UP ? -MIN_STEM_SPAN : MIN_STEM_SPAN);
	}

	public static boolean hasStem(int noteLength) {
		return noteLength != 0;
	}
}
