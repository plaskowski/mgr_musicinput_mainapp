package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.waw.echo.eclipse.codegenerator.annotation.JoinClasses;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

class GenDeclarations {
	@JoinClasses(
		superClass = LinearLayout.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinearLayout_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM1; // = LinearLayout_ExtendedBackground.class; 
	@JoinClasses(
		superClass = LinearLayout.class,
		ancestors = {
			InterceptTouchEventStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinearLayout_InterceptTouch"
	)
	static Class<? extends View> CUSTOM2; // = LinearLayout_InterceptTouch.class; 
	@JoinClasses(
		superClass = ScrollView.class,
		ancestors = {
			LockScrollingStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ScrollView_LockableScroll"
	)
	static Class<? extends View> CUSTOM3; // = ScrollView_LockableScroll.class; 
	@JoinClasses(
		superClass = View.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.View_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM4; // = View_ExtendedBackground.class; 
	@JoinClasses(
		superClass = ImageButton.class,
		ancestors = {
			ExtendedImageStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ImageButton_ExtendedImage"
	)
	static Class<? extends View> CUSTOM5; // = ImageButton_ExtendedImage.class; 
	@JoinClasses(
		superClass = HorizontalScrollView.class,
		ancestors = {
			InterceptOnScrollChangedStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.HorizontalScrollView_InterceptOnScrollChanged"
	)
	static Class<? extends View> CUSTOM6; // = HorizontalScrollView_InterceptOnScrollChanged.class;
	@JoinClasses(
		superClass = RelativeLayout.class,
		ancestors = {
			CorrectTopMarginStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.RelativeLayout_CorrectTopMargin"
	)
	static Class<? extends View> CUSTOM7; // = RelativeLayout_CorrectTopMargin.class;
	@JoinClasses(
		superClass = RelativeLayout.class,
		ancestors = {
			InterceptScaleGestureStrategy.class,
			LockTouchInputStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.RelativeLayout_InterceptScale_LockTouchInput"
	)
	static Class<? extends View> CUSTOM8; //  = RelativeLayout_InterceptScale_LockTouchInput.class;
}
