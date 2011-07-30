package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;

public class NoteSpec {
	private int length;
	private int postion;
	
	public static final int ORIENT_UP = 0;
	public static final int ORIENT_DOWN = 1;
	
	private int flags;
	private static final int FLAG_ORIENT = 0;
	
	/**
	 * Assumes orientation := ORIENT_UP
	 */
	public NoteSpec(int length, int postion) {
		this(length, postion, NoteConstants.isUpsdown(postion) ? ORIENT_DOWN : ORIENT_UP);
	}
	public NoteSpec(int length, int postion, int orientation) {
		this.length = length;
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
	
	private int getFlag(int flag) {
		return (flags >> flag) & 1;
	}
	private void setFlag(int flag, int flagVal) {
		flags = (flags & ~(1<<flag)) | ((flagVal & 1) << flag); 
	}

	public int length() {
		return length;
	}

	public int positon() {
		return postion;
	}
	
}
