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

	public NoteSpec(NoteSpec source, int currentAnchor) {
		super(source);
		this.postion = currentAnchor;
		for(int i = 0; i < THIS_FLAGS.length; i++) {
			int flag = THIS_FLAGS[i];
			setFlag(flag, source.getFlag(flag));
		}
	}
	
	public void setHasJoinArc(boolean hasJoinArc) {
		setFlag(FLAG_JOINARC, hasJoinArc ? 1 : 0);
	}
	public boolean hasJoinArc() {
		return getFlag(FLAG_JOINARC) == 1;
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
		setFlag(FLAG_DOT, hasDot ? 1 : 0);
	}
	public boolean hasDot() {
		return dotExtension() > 0;
	}

	public boolean isGrouped() {
		return getFlag(FLAG_GROUPED) == 1;
	}

	public void setIsGrouped(boolean isGrouped) {
		setFlag(FLAG_GROUPED, isGrouped ? 1 : 0);
	}
	
}
