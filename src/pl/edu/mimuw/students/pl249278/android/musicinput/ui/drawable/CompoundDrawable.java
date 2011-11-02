package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public abstract class CompoundDrawable extends TranscluentDrawable {
	private List<PaintSetup> paints = new ArrayList<PaintSetup>(2);
	/** (-extrems.left, -extrems.top) are coordinates for drawing the base */
	protected RectF offsetExtremum = new RectF(0, 0, 0, 0);
	
	protected void addPaintSetup(Paint paint, float offsetX, float offsetY, float drawRadius) {
		addPaintSetup(new PaintSetup(
			paint, new PointF(offsetX, offsetY), drawRadius
		));
	}
	public void addPaintSetup(PaintSetup ps) {
		paints.add(ps);
		offsetExtremum.left = min(ps.offsetToBase.x - ps.drawRadius, offsetExtremum.left);
		offsetExtremum.top = min(ps.offsetToBase.y - ps.drawRadius, offsetExtremum.top);
		offsetExtremum.right = max(ps.offsetToBase.x + ps.drawRadius, offsetExtremum.right);
		offsetExtremum.bottom = max(ps.offsetToBase.y + ps.drawRadius, offsetExtremum.bottom);
	}
	
	@Override
	public boolean getPadding(Rect padding) {
		padding.set(
			round(-offsetExtremum.left),
			round(-offsetExtremum.top),
			round(offsetExtremum.right),
			round(offsetExtremum.bottom)
		);
		return true;
	}
	
	private int round(float value) {
		return (int) (value+0.5f);
	}
	
	@Override
	public void draw(Canvas canvas) {
		// actual size of drawing outline
		float width = getBounds().width() + offsetExtremum.left - offsetExtremum.right;
		float height = getBounds().height() + offsetExtremum.top - offsetExtremum.bottom;
		
		canvas.save();
		PointF trDelta = new PointF(offsetExtremum.left, offsetExtremum.top);
		for(int i = 0; i < paints.size(); i++) {
			PaintSetup ps = paints.get(i);
			canvas.translate(
				ps.offsetToBase.x - trDelta.x,
				ps.offsetToBase.y - trDelta.y
			);
			trDelta.set(ps.offsetToBase);
			this.draw(canvas, ps.paint, width, height);
		}
		canvas.restore();
	}

	protected abstract void draw(Canvas canvas, Paint paint, float width, float height);

}
