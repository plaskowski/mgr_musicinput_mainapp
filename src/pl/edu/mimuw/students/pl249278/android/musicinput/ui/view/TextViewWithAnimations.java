package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class TextViewWithAnimations extends TextView {

	public TextViewWithAnimations(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public TextViewWithAnimations(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewWithAnimations(Context context) {
		super(context);
	}
	
	private void updateAnimationsState() {
		boolean running = getVisibility() == View.VISIBLE && hasWindowFocus();
		Drawable[] drawables = getCompoundDrawables();
		for (int i = 0; i < drawables.length; i++) {
			Drawable drawable = drawables[i];
			if(drawable != null && drawable instanceof AnimationDrawable) {
				AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
				if(running) {
					animationDrawable.start();
				} else {
					animationDrawable.stop();
				}
			}
		}
	}
	
	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		updateAnimationsState();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		updateAnimationsState();
	}
}
