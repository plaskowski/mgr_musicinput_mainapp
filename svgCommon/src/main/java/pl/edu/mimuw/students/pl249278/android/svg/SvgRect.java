package pl.edu.mimuw.students.pl249278.android.svg;


public class SvgRect extends SvgObject {
	float x,y,width,hegiht;
	
	public SvgRect(float x, float y, float width, float hegiht) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.hegiht = hegiht;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHegiht() {
		return hegiht;
	}

	@Override
	public void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}
}
