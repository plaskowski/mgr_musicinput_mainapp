package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.waw.echo.eclipse.codegenerator.annotation.JoinClasses;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
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
			LockScrollingStrategy.class,
			LazyScrollToStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ScrollView_LockableScroll_LazyScrolling"
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
	@JoinClasses(
		superClass = FrameLayout.class,
		ancestors = {
			InterceptScaleGestureStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.FrameLayout_InterceptScale"
	)
	static Class<? extends View> CUSTOM9;
	@JoinClasses(
		superClass = HorizontalScrollView.class,
		ancestors = {
			DetectTouchInterceptionStrategy.class,
			LazyScrollToStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.HorizontalScrollView_ObserveInterceptTouch_LazyScrolling"
	)
	static Class<? extends View> CUSTOM10;
	@JoinClasses(
		superClass = ImageView.class,
		ancestors = {
			ExtendedImageStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ImageView_ExtendedImage"
	)
	static Class<? extends View> CUSTOM11; 
	@JoinClasses(
		superClass = HorizontalScrollView.class,
		ancestors = {
			NoteValueSpinner.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner_Horizontal"
	)
	static Class<? extends View> CUSTOM12; 
	@JoinClasses(
		superClass = ScrollView.class,
		ancestors = {
			NoteValueSpinner.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner_Vertical"
	)
	static Class<? extends View> CUSTOM13;
}

/** a stub class, lets a strategy call super.onLayout() */
class DummyViewGroup extends ViewGroup {

	public DummyViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		throw new RuntimeException("Should not be reached");
	}

	public DummyViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		throw new RuntimeException("Should not be reached");
	}

	public DummyViewGroup(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		throw new RuntimeException("Should not be reached");
	}
	
}
