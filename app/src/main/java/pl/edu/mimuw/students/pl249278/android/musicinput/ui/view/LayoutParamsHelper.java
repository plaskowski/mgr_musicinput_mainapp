package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class LayoutParamsHelper {

	public static void updateSize(View v, Integer width, Integer height) {
		LayoutParams params = v.getLayoutParams();
		if(width != null) params.width = width;
		if(height != null) params.height = height;
		v.setLayoutParams(params);
	}
	
	public static void updateSizeDirect(View v, int width, int height) {
		LayoutParams params = v.getLayoutParams();
		params.width = width;
		params.height = height;
		v.setLayoutParams(params);
	}
	
	public static ViewGroup.MarginLayoutParams updateMargins(View v, Integer left, Integer top) {
		ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		if(left != null) params.leftMargin = left;
		if(top != null) params.topMargin = top;
		v.setLayoutParams(params);
		return params;
	}
	
	public static int topMargin(View view) {
		return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
	}

	public static void setVerticalPadding(View view, int top, int bottom) {
		view.setPadding(view.getPaddingLeft(), top, view.getPaddingRight(), bottom);
	}
	
}
