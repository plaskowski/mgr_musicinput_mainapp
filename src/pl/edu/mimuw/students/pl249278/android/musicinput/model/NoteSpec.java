package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asBool;
import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asFlagVal;

import java.util.HashMap;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;

public class NoteSpec extends PauseSpec implements PositonSpec {
	private int postion;
	
	private static final int FLAG_JOINARC = FLAGS_AMOUNT;
	private static final int FLAG_GROUPED = FLAG_JOINARC+1;
	private static final int FLAG_TONEMODIFIER_L = FLAG_GROUPED+1;
	private static final int FLAG_TONEMODIFIER_H = FLAG_TONEMODIFIER_L+1;
	private static final int SELF_FLAGS_AMOUNT = FLAG_TONEMODIFIER_H+1;
	
	public static enum TOGGLE_FIELD {
		HAS_JOIN_ARC(FLAG_JOINARC),
		IS_GROUPED(FLAG_GROUPED);
		
		private int FIELD_FLAG;
		private TOGGLE_FIELD(int fieldFlag) {
			FIELD_FLAG = fieldFlag;
		}
	};
	
	/** mapping tone modifier flag value to enum value, 0 means no tone modifier is set */
	private static final NoteModifier[] TONE_MODIFIER_REVERSE_MAPPING = new NoteModifier[] {
		null,
		NoteModifier.FLAT,
		NoteModifier.NATURAL,
		NoteModifier.SHARP
	};
	private static final HashMap<NoteModifier, Integer> TONE_MODIFIER_MAPPING = new HashMap<NoteConstants.NoteModifier, Integer>();
	static {
		for(int i = 1; i < TONE_MODIFIER_REVERSE_MAPPING.length; i++) {
			TONE_MODIFIER_MAPPING.put(TONE_MODIFIER_REVERSE_MAPPING[i], i);
		}
	}
	
	public NoteSpec(int length, int postion) {
		super(length);
		this.postion = postion;
	}
	
	public NoteSpec(NoteSpec source, TOGGLE_FIELD fieldToToggle) {
		this(source);
		toggleFlag(fieldToToggle.FIELD_FLAG);
	}

	public NoteSpec(NoteSpec source, int currentAnchor) {
		this(source);
		this.postion = currentAnchor;
	}
	
	/** copy constructor */
	public NoteSpec(NoteSpec source) {
		super(source);
		putValue(
			SELF_FLAGS_AMOUNT-1, FLAGS_AMOUNT, 
			source.getValue(
				SELF_FLAGS_AMOUNT-1, FLAGS_AMOUNT
			)
		);
		this.postion = source.postion;
	}
	
	public void setHasJoinArc(boolean hasJoinArc) {
		setFlag(FLAG_JOINARC, asFlagVal(hasJoinArc));
	}
	public boolean hasJoinArc() {
		return asBool(getFlag(FLAG_JOINARC));
	}
	
	/**
	 * @return null if isn't set
	 */
	public NoteModifier getToneModifier() {
		int value = getValue(FLAG_TONEMODIFIER_H, FLAG_TONEMODIFIER_L);
		return TONE_MODIFIER_REVERSE_MAPPING[value];
	}
	
	public void clearToneModifier() {
		putValue(
			FLAG_TONEMODIFIER_H, FLAG_TONEMODIFIER_L,
			0
		);
	}
	public void setToneModifier(NoteModifier modifier) {
		putValue(
			FLAG_TONEMODIFIER_H, FLAG_TONEMODIFIER_L,
			TONE_MODIFIER_MAPPING.get(modifier)
		);
	}
	
	public int positon() {
		return postion;
	}

	public boolean isGrouped() {
		return asBool(getFlag(FLAG_GROUPED));
	}

	public void setIsGrouped(boolean isGrouped) {
		setFlag(FLAG_GROUPED, asFlagVal(isGrouped));
	}
	
}
