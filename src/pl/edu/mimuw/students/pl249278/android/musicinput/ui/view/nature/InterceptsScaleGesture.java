package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import android.graphics.PointF;

public interface InterceptsScaleGesture {

	public interface OnScaleListener {
		void onScaleBegin();
		void onScale(float scaleFactor, PointF focusPoint);
		void onScaleEnd();
	}
	
	public void setOnScaleListener(OnScaleListener onScaleListener);

}
