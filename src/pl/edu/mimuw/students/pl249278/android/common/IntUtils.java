package pl.edu.mimuw.students.pl249278.android.common;

public class IntUtils {

	public static int getFlag(int flags, int flag) {
		return (flags >> flag) & 1;
	}

	public static int setFlag(int flags, int flag, int flagVal) {
		return (flags & ~(1<<flag)) | ((flagVal & 1) << flag); 
	}

	/** 
	 * read positive integer (or 0) written in selected bit sequence
	 * @return 
	 */
	public static int getValue(int flags, int highestBitIndex, int lowestBitIndex) {
		return (flags >> lowestBitIndex) & ((1<<(highestBitIndex-lowestBitIndex+1))-1);
	}
	
	public static int putValue(int flags, int highestBitIndex, int lowestBitIndex, int value) {
		int mask = (1<<(highestBitIndex-lowestBitIndex+1))-1;
		return (flags & ~(mask << lowestBitIndex)) | ((value & mask) << lowestBitIndex);
	}
	
	public static int asFlagVal(boolean boolVal) {
		return boolVal ? 1 : 0;
	}
	public static boolean asBool(int flagVal) {
		return flagVal == 1;
	}
}
