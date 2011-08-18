package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;

public class PauseSpec implements LengthSpec {
	private int length;
	private int flags;
	protected static final int FLAG_DOT = 0;
	protected static final int FLAGS_AMOUNT = FLAG_DOT+1;
	
	public PauseSpec(int length) {
		this.length = length;
	}
	
	public PauseSpec(PauseSpec source) {
		this.length = source.length;
		setFlag(FLAG_DOT, source.getFlag(FLAG_DOT));
	}

	protected int getFlag(int flag) {
		return IntUtils.getFlag(flags, flag);
	}
	protected void setFlag(int flag, int flagVal) {
		flags = IntUtils.setFlag(flags, flag, flagVal);
	}
	
	/** 
	 * read positive integer (or 0) written in selected bit sequence
	 * @return 
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
		return getFlag(FLAG_DOT);
	}
	
}
