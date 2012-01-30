package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import java.util.EnumSet;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;

public class TimeSpec {
	TimeStep timeStep;
	Clef clef;
	KeySignature keySignature;
	EnumSet<AdditionalMark> marks;
	public static enum AdditionalMark {
		BEGIN_REPEAT,
		END_REPEAT
	}
	
	public static class TimeStep {
		public static final TimeStep commonTime = new TimeStep(4, NoteConstants.LEN_QUATERNOTE);
		public static final TimeStep cutCommonTime = new TimeStep(2, NoteConstants.LEN_HALFNOTE);
		
		private int baseMultiplier, tempoBaseLength;

		public TimeStep(int baseMultiplier, int tempoBaseLength) {
			super();
			this.baseMultiplier = baseMultiplier;
			this.tempoBaseLength = tempoBaseLength;
		}

		public int getBaseMultiplier() {
			return baseMultiplier;
		}

		public int getTempoBaseLength() {
			return tempoBaseLength;
		}
		
	}
	
	public TimeSpec(TimeStep timeStep, Clef clef, KeySignature keySignature) {
		this(timeStep, clef, keySignature, null);
	}
	public TimeSpec(TimeStep timeStep, Clef clef, KeySignature keySignature, EnumSet<AdditionalMark> marks) {
		this.timeStep = timeStep;
		this.clef = clef;
		this.keySignature = keySignature;
		this.marks = marks != null ? EnumSet.copyOf(marks) : EnumSet.noneOf(AdditionalMark.class);
	}

	public TimeSpec() {
		this(null, null, null, null);
	}

	public TimeStep getTimeStep() {
		return timeStep;
	}

	public Clef getClef() {
		return clef;
	}

	public void setTimeStep(TimeStep timeStep) {
		this.timeStep = timeStep;
	}

	public void setClef(Clef clef) {
		this.clef = clef;
	}

	public KeySignature getKeySignature() {
		return keySignature;
	}

	public void setKeySignature(KeySignature keySignature) {
		this.keySignature = keySignature;
	}
	
	public boolean addMark(AdditionalMark mark) {
		return marks.add(mark);
	}
	
	public boolean hasMark(AdditionalMark mark) {
		return marks.contains(mark);
	}
	public boolean removeMark(AdditionalMark mark) {
		return marks.remove(mark);
	}
	
}
