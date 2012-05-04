package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import pl.waw.echo.eclipse.codegenerator.annotation.JoinClasses;
import android.support.v4.app.FragmentActivity;

class GenDeclarations {
	@JoinClasses(
		superClass = FragmentActivity.class,
		ancestors = {
			ErrorDialogStrategy.class,
			ShowScoreStrategy.class,
			InitialProgressDialogStrategy.class,
			ManagedReceiverStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_ProgressDialog_ShowScore_ManagedReceiver"
	)
	static Class<? extends FragmentActivity> CUSTOM1; // = LinearLayout_ExtendedBackground.class; 
	@JoinClasses(
		superClass = FragmentActivity.class,
		ancestors = {
			ErrorDialogStrategy.class,
			TipDialogStrategy.class,
			InitialProgressDialogStrategy.class,
			ManagedReceiverStrategy.class
		},
		outputClass = "pl.edu.mimuw.students.pl249278.android.musicinput.ui.component.activity.FragmentActivity_ErrorDialog_TipDialog_ProgressDialog_ManagedReceiver"
	)
	static Class<? extends FragmentActivity> CUSTOM2; // = LinearLayout_ExtendedBackground.class; 
}
