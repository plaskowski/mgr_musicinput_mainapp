package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.echo.android.view.PagedScrollView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinedSheetElementView;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.TextViewWithAnimations;
import pl.waw.echo.eclipse.codegenerator.annotation.JoinClasses;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

class ViewGenDeclarations {
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
		superClass = AbsoluteLayout.class,
		ancestors = {
			CorrectTopPositionStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.AbsoluteLayout_CorrectTopPosition"
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
		superClass = pl.echo.android.view.PagedHorizontalScrollView.class,
		ancestors = {
			NoteValueSpinner.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner_Horizontal"
	)
	static Class<? extends View> CUSTOM12; 
	@JoinClasses(
		superClass = PagedScrollView.class,
		ancestors = {
			NoteValueSpinner.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.NoteValueSpinner_Vertical"
	)
	static Class<? extends View> CUSTOM13;
	@JoinClasses(
		superClass = TextView.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.TextView_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM14;
	@JoinClasses(
		superClass = TextViewWithAnimations.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.TextView_WithAnimations_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM15;
	@JoinClasses(
		superClass = LinearLayout.class,
		ancestors = {
			BringToFrontStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinearLayout_FrontChild"
	)
	static Class<? extends View> CUSTOM16; 
	@JoinClasses(
		superClass = LinedSheetElementView.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinedSheetElementView_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM17; 
	@JoinClasses(
		superClass = TableRow.class,
		ancestors = {
			ExtendedBackgroundStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.TableRow_ExtendedBackground"
	)
	static Class<? extends View> CUSTOM18;
	@JoinClasses(
		superClass = LinearLayout.class,
		ancestors = {
			CacheDrawStrategy.class,
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.LinearLayout_CacheDraw"
	)
	static Class<? extends View> CUSTOM19;
	@JoinClasses(
		superClass = View.class,
		ancestors = {
			ExtendedBackgroundStrategy.class,
			CacheDrawStrategy.class,
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.View_ExtendedBackground_CacheDraw"
	)
	static Class<? extends View> CUSTOM20;
	@JoinClasses(
		superClass = AbsoluteLayout.class,
		ancestors = {
			CorrectTopPositionStrategy.class,
			StaveHighlightStrategy.class,
			BarLineHighlightStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.AbsoluteLayout_CorrectTopPosition_StaveHighlighter_BarLineHighlighter"
	)
	static Class<? extends View> CUSTOM21;	
}