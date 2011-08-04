package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.KeySignature;

public class TimeSpec {
	TimeStep timeStep;
	Clef clef;
	KeySignature keySignature;
	
	public static class TimeStep {
		int baseMultiplier, tempoBaseLength;

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
		this.timeStep = timeStep;
		this.clef = clef;
		this.keySignature = keySignature;
	}

	public TimeSpec() {
		this(null, null, null);
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
	
}
