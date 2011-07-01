package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.ArrayList;

public class SvgImage {
	float width, height;
	//TODO remove public modifier
	public ArrayList<SvgObject> objects;
	
	public SvgImage() {
		objects = new ArrayList<SvgObject>();
	}
}
