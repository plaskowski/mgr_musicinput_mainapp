package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

public class NoteConstants {
	public static final int ANCHOR_TYPE_LINE = 0;
	public static final int ANCHOR_TYPE_LINESPACE = 1;
	
	public static final int LEN_FULLNOTE = 0;
	public static final int LEN_HALFNOTE = 1;
	public static final int LEN_QUATERNOTE = 2;
	
	public static int anchorType(int anchorIndex) {
		return anchorIndex%2;
	}

	public static int anchorIndex(int withinTypeIndex, int type) {
		return type == ANCHOR_TYPE_LINE ? withinTypeIndex * 2 : withinTypeIndex * 2 +1 ;
	}
}
