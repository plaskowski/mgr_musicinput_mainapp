package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asBool;
import static pl.edu.mimuw.students.pl249278.android.common.IntUtils.asFlagVal;

public class PauseSpec implements LengthSpec {
	private int length;
	private int flags;
	private static final int FLAG_DOT_L = 0;
	private static final int FLAG_DOT_H = FLAG_DOT_L+3;
	protected static final int FLAGS_AMOUNT = FLAG_DOT_H+1;
	
	public PauseSpec(int length) {
		this.length = length;
	}
	
	public PauseSpec(PauseSpec source) {
		this.length = source.length;
		int highestBit = FLAGS_AMOUNT-1;
		// copy all bits that are used by PauseSpec
		putValue(
			highestBit, 0,
			IntUtils.getValue(source.flags, highestBit, 0)
		);
	}

	protected int getFlag(int flag) {
		return IntUtils.getFlag(flags, flag);
	}
	protected void setFlag(int flag, int flagVal) {
		flags = IntUtils.setFlag(flags, flag, flagVal);
	}
	protected void toggleFlag(int flag) {
		this.setFlag(flag, asFlagVal(!asBool(getFlag(flag))));
	}
	
	/** 
	 * read positive integer (or 0) written in selected bit sequence
	 */
	protected int getValue(int highestBitIndex, int lowestBitIndex) {
		return IntUtils.getValue(flags, highestBitIndex, lowestBitIndex);
	}
	
	protected void putValue(int highestBitIndex, int lowestBitIndex, int value) {
		flags = IntUtils.putValue(flags, highestBitIndex, lowestBitIndex, value);
	}	

	public int length() {
		return length;
	}

	@Override
	public int dotExtension() {
		return getValue(FLAG_DOT_H, FLAG_DOT_L);
	}

	public void setDotExtension(int dotExt) {
		putValue(FLAG_DOT_H, FLAG_DOT_L, dotExt);
	}
	
}
