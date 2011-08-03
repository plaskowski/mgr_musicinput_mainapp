package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import java.util.HashMap;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;

public class NoteSpec extends PauseSpec implements PositonSpec {
	private int postion;
	
	public static final int ORIENT_UP = 0;
	public static final int ORIENT_DOWN = 1;
	
	private static final int FLAG_ORIENT = FLAGS_AMOUNT;
	private static final int FLAG_JOINARC = FLAG_ORIENT+1;
	private static final int FLAG_GROUPED = FLAG_JOINARC+1;
	private static final int FLAG_TONEMODIFIER_L = FLAG_GROUPED+1;
	private static final int FLAG_TONEMODIFIER_H = FLAG_TONEMODIFIER_L+1;
	
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
	
	/**
	 * Assumes orientation := ORIENT_UP
	 */
	public NoteSpec(int length, int postion) {
		this(length, postion, NoteConstants.isUpsdown(postion) ? ORIENT_DOWN : ORIENT_UP);
	}
	public NoteSpec(int length, int postion, int orientation) {
		super(length);
		this.postion = postion;
		setOrientation(orientation);
	}
	
	public void setOrientation(int orientation) {
		setFlag(FLAG_ORIENT, orientation);
	}
	public int getOrientation() {
		 int flag = getFlag(FLAG_ORIENT);
		return flag;
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
	
}
