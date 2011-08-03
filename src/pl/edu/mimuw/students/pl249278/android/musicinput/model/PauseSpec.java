package pl.edu.mimuw.students.pl249278.android.musicinput.model;

public class PauseSpec implements LengthSpec {
	private int length;
	private int flags;
	protected static final int FLAG_DOT = 0;
	protected static final int FLAGS_AMOUNT = FLAG_DOT+1;
	
	public PauseSpec(int length) {
		this.length = length;
	}
	
	protected int getFlag(int flag) {
		return (flags >> flag) & 1;
	}
	protected void setFlag(int flag, int flagVal) {
		flags = (flags & ~(1<<flag)) | ((flagVal & 1) << flag); 
	}
	
	/** 
	 * read positive integer (or 0) written in selected bit sequence
	 * @return 
	 */
	protected int getValue(int highestBitIndex, int lowestBitIndex) {
		return (flags >> lowestBitIndex) & ((1<<(highestBitIndex-lowestBitIndex+1))-1);
	}
	
	protected void putValue(int highestBitIndex, int lowestBitIndex, int value) {
		int mask = (1<<(highestBitIndex-lowestBitIndex+1))-1;
		flags = (flags & ~(mask << lowestBitIndex)) | ((value & mask) << lowestBitIndex);
	}	

	public int length() {
		return length;
	}

	@Override
	public int dotExtension() {
		return getFlag(FLAG_DOT);
	}
	
}
