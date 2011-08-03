package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.Key;

public class TimeSpec {
	TimeStep timeStep;
	Key key;
	// TODO add tonacja
	
	// TODO add contructor
	
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
	
	public TimeSpec(TimeStep timeStep, Key key) {
		this.timeStep = timeStep;
		this.key = key;
	}

	public TimeSpec() {
		this(null, null);
	}

	public TimeStep getTimeStep() {
		return timeStep;
	}

	public Key getKey() {
		return key;
	}

	public void setTimeStep(TimeStep timeStep) {
		this.timeStep = timeStep;
	}

	public void setKey(Key key) {
		this.key = key;
	}
}
