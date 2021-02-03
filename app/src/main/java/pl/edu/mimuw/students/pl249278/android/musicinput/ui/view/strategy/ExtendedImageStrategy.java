package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.widget.ImageView;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class ExtendedImageStrategy extends ViewGroupStrategyBase {

	public ExtendedImageStrategy(ViewGroupStrategy parent) {
		super(parent);
		checkThatViewImplements(ImageView.class);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);
		ExtendedResourcesFactory.loadExtendedImage((ImageView) internals().viewObject(), viewInflationContext);
	}

}
