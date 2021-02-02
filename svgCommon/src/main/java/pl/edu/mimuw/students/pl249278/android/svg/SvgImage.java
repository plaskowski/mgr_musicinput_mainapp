package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.ArrayList;

public class SvgImage {
	protected float width, height;
	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public ArrayList<SvgObject> objects;
	
	public SvgImage(float width, float height, ArrayList<SvgObject> objects) {
		this.width = width;
		this.height = height;
		this.objects = objects != null ? objects : new ArrayList<SvgObject>();
	}
}
