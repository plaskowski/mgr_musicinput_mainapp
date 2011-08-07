package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.LengthSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PositonSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;

public abstract class ElementSpec {
	public static enum ElementType {
		NOTE,
		PAUSE,
		FAKE_PAUSE,
		SPECIAL_SIGN,
		TIMES_DIVIDER
	};
	
	public abstract int timeValue(int metricUnit);
	
	private ElementType type;
	protected ElementSpec(ElementType type) {
		super();
		this.type = type;
	}
	public final ElementType getType() {
		return type;
	}
	
	public LengthSpec lengthSpec() {
		throw new UnsupportedOperationException();
	}
	public PositonSpec positonSpec() {
		throw new UnsupportedOperationException();
	}
	
	private static class ElementWithLength<T extends LengthSpec> extends ElementSpec {
		T spec;
		public ElementWithLength(ElementType type, T spec) {
			super(type);
			this.spec = spec;
		}
		@Override
		public int timeValue(int metricUnit) {
			return (int) overallLength(spec, metricUnit);
		}
		@Override
		public LengthSpec lengthSpec() {
			return spec;
		}
	}
	
	public static class NormalNote extends ElementWithLength<NoteSpec> {
		private int flags;
		private static final int FLAG_ORIENT = 0;
		private static final int FLAG_NOSTEM = FLAG_ORIENT+1;
		
		public NormalNote(NoteSpec spec) {
			this(spec, NoteConstants.defaultOrientation(spec));
		}
		public NormalNote(NoteSpec spec, int orientation) {
			super(ElementType.NOTE, spec);
			setOrientation(orientation);
			setNoStem(false);
		}
		@Override
		public PositonSpec positonSpec() {
			return spec;
		}
		public NoteSpec noteSpec() {
			return spec;
		}
		public void setOrientation(int orientation) {
			flags = IntUtils.setFlag(flags, FLAG_ORIENT, orientation);
		}
		public int getOrientation() {
			return IntUtils.getFlag(flags, FLAG_ORIENT);
		}
		public void setNoStem(boolean hasNoStem) {
			flags = IntUtils.setFlag(flags, FLAG_NOSTEM, hasNoStem ? 1 : 0);
		}
		public boolean hasNoStem() {
			return IntUtils.getFlag(flags, FLAG_NOSTEM) == 1;
		}
	}
	
	public static class Pause extends ElementWithLength<PauseSpec> {
		public Pause(PauseSpec spec) {
			super(ElementType.PAUSE, spec);
		}
	}
	
	public static class FakePause extends ElementSpec {
		private int timeValue;
		private int measureUnit;
		public FakePause(int timeValue, int measureUnit) {
			super(ElementType.FAKE_PAUSE);
			this.timeValue = timeValue;
			this.measureUnit = measureUnit;
		}
		@Override
		public int timeValue(int metricUnit) {
			return length(this.measureUnit, metricUnit) * timeValue;
		}
	}
	
	public static class SpecialSign extends ElementSpec implements PositonSpec {
		private int position, length;
		
		public SpecialSign(int position, int length) {
			super(ElementType.SPECIAL_SIGN);
			this.position = position;
			this.length = length;
		}

		@Override
		public int timeValue(int metricUnit) {
			return length(length, metricUnit);
		}
		
		@Override
		public PositonSpec positonSpec() {
			return this;
		}

		@Override
		public int positon() {
			return position;
		}
	}
	
	public static class TimeDivider extends ElementSpec {
		TimeSpec leftTime, rightTime;

		public TimeDivider(TimeSpec leftTime, TimeSpec rightTime) {
			super(ElementType.TIMES_DIVIDER);
			this.leftTime = leftTime;
			this.rightTime = rightTime;
		}

		@Override
		public int timeValue(int metricUnit) {
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static double overallLength(LengthSpec lengthSpec, int measureUnit) {
		int result = length(lengthSpec.length(), measureUnit);
		return result * (2-Math.pow(0.5f, lengthSpec.dotExtension()));
	}
	public static int length(int specLength, int measureUnit) {
		return 1 << (measureUnit-specLength);
	}
}
