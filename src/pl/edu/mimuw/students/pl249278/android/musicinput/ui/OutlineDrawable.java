package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Pair;

public class OutlineDrawable extends Drawable {
	List<Pair<Paint, Integer>> customPaints = new ArrayList<Pair<Paint,Integer>>();

	@Override
	public void draw(Canvas canvas) {
		Rect cbounds = getBounds();
		for(Pair<Paint, Integer> paint: customPaints) {
			int padding = paint.second;
			canvas.drawRect(cbounds.left+padding, cbounds.top+padding, cbounds.right-padding, cbounds.bottom-padding, paint.first);
		}
	}
	
	public void addPaint(Paint paint, Integer padding) {
		customPaints.add(new Pair<Paint, Integer>(paint, padding));
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		throw new UnsupportedOperationException();
	}

}
