package pl.edu.mimuw.students.pl249278.android.musicinput.model;

public class NoteSpec {
	private int length;
	private int postion;
	
	public NoteSpec(int length, int postion) {
		this.length = length;
		this.postion = postion;
	}

	public int length() {
		return length;
	}

	public int positon() {
		return postion;
	}
	
}
