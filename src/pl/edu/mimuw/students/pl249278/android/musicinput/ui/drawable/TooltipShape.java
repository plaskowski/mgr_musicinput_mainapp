package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.IndicatorAware.IndicatorOrigin;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

class TooltipShape {
	private IndicatorOrigin indicatorOrigin = IndicatorOrigin.NONE;
	private int indicatorOriginX = 0;
	private int indicatorSize;
	
	public TooltipShape(int indicatorSize) {
		this.indicatorSize = indicatorSize;
	}
	
	public boolean getPadding(Rect padding) {
		switch(indicatorOrigin) {
		case TOP:
			padding.top = indicatorSize;
			break;
		case BOTTOM:
			padding.bottom = indicatorSize;
		}
		return true;
	}
	
	public void draw(Canvas canvas, Paint paint, float width, float height) {
		Path path = getPath(width, height);
		canvas.drawPath(path, paint);
	}

	private Path getPath(float width, float height) {
		int indLeftW = Math.min(indicatorSize/2, indicatorOriginX);
		int indRightW = (int) Math.min(indicatorSize/2, width - indicatorOriginX);
		
		Path path = new Path();
		if(indicatorOrigin == IndicatorOrigin.TOP) {
			path.rMoveTo(0, indicatorSize);
			path.rLineTo(indicatorOriginX-indLeftW, 0);
			path.rLineTo(indLeftW, -indicatorSize);
			path.rLineTo(indRightW, indicatorSize);
			path.lineTo(width, indicatorSize);
		} else {
			path.rLineTo(width, 0);
		}
		if(indicatorOrigin == IndicatorOrigin.BOTTOM) {
			path.rLineTo(0, height-indicatorSize);
			path.rLineTo(-(width-indicatorOriginX-indRightW), 0);
			path.rLineTo(-indRightW, indicatorSize);
			path.rLineTo(-indLeftW, -indicatorSize);
			path.rLineTo(-(indicatorOriginX-indLeftW), 0);
		} else {
			path.lineTo(width, height);
			path.rLineTo(-width, 0);
		}
		path.close();
		return path;
	}

	public IndicatorOrigin getIndicatorOrigin() {
		return indicatorOrigin;
	}
	
	public void setIndicatorOrigin(IndicatorOrigin indicatorOrigin) {
		this.indicatorOrigin = indicatorOrigin;
	}

	public void setIndicatorOriginX(int indicatorOriginX) {
		this.indicatorOriginX = indicatorOriginX;
	}

	public int getIndicatorOriginX() {
		return indicatorOriginX;
	}
	
}
