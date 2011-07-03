package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

public class NoteConstants {
	public static final int ANCHOR_TYPE_LINE = 0;
	public static final int ANCHOR_TYPE_LINESPACE = 1;
	
	public static int anchorType(int anchorIndex) {
		return anchorIndex%2;
	}
}
