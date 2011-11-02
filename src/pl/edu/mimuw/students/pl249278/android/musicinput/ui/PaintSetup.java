package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.graphics.Paint;
import android.graphics.PointF;

public class PaintSetup {
	public Paint paint;
	public PointF offsetToBase;
	public float drawRadius;
	
	public PaintSetup(Paint paint, PointF offsetToBase, float drawRadius) {
		this.paint = paint;
		this.offsetToBase = offsetToBase;
		this.drawRadius = drawRadius;
	}
}