package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.graphics.Rect;


public interface IndicatorAware {
	public static enum IndicatorOrigin {
		TOP,
		BOTTOM,
		NONE
	};
	
	public void setIndicatorOrigin(IndicatorOrigin origin);
	public void setOriginX(int indicatorOriginX);
	public void getOriginPostionMargin(Rect margins);
	public IndicatorOrigin getIndicatorOrigin();
}