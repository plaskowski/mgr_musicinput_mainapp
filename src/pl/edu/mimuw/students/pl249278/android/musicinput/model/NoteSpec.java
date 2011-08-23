package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;

import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;

public class NoteSpec extends PauseSpec implements PositonSpec {
	private int postion;
	
	private static final int FLAG_JOINARC = FLAGS_AMOUNT;
	private static final int FLAG_GROUPED = FLAG_JOINARC+1;
	private static final int FLAG_TONEMODIFIER_L = FLAG_GROUPED+1;
	private static final int FLAG_TONEMODIFIER_H = FLAG_TONEMODIFIER_L+1;
	private static final int[] THIS_FLAGS;
	
	public static enum TOGGLE_FIELD {
		HAS_JOIN_ARC(FLAG_JOINARC),
		IS_GROUPED(FLAG_GROUPED);
		
		private int FIELD_FLAG;
		private TOGGLE_FIELD(int fieldFlag) {
			FIELD_FLAG = fieldFlag;
		}
	};
	
	static {
		Collection<Field> allNoteFlags = ReflectionUtils.findConsts(NoteSpec.class, "FLAG_");
		THIS_FLAGS = new int[allNoteFlags.size()];
		int index = 0;
		for(Field flagField: allNoteFlags) {
			try {
				THIS_FLAGS[index++] = (Integer) flagField.get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
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
		int flag = fieldToToggle.FIELD_FLAG;
		this.setFlag(flag, asFlagVal(!asBool(getFlag(flag))));
	}

	public NoteSpec(NoteSpec source, int currentAnchor) {
		this(source);
		this.postion = currentAnchor;
	}
	public NoteSpec(NoteSpec source) {
		super(source);
		for(int i = 0; i < THIS_FLAGS.length; i++) {
			int flag = THIS_FLAGS[i];
			setFlag(flag, source.getFlag(flag));
		}
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
	public void setHasDot(boolean hasDot) {
		setFlag(FLAG_DOT, asFlagVal(hasDot));
	}
	public boolean hasDot() {
		return dotExtension() > 0;
	}

	public boolean isGrouped() {
		return asBool(getFlag(FLAG_GROUPED));
	}

	public void setIsGrouped(boolean isGrouped) {
		setFlag(FLAG_GROUPED, asFlagVal(isGrouped));
	}
	
	private static int asFlagVal(boolean boolVal) {
		return boolVal ? 1 : 0;
	}
	private static boolean asBool(int flagVal) {
		return flagVal == 1;
	}
}
