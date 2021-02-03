package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.ViewInflationContext;

public class ExtendedBackgroundStrategy extends ViewGroupStrategyBase {

	public ExtendedBackgroundStrategy(ViewGroupStrategy parent) {
		super(parent);
	}

	@Override
	public void initStrategy(ViewInflationContext viewInflationContext) {
		super.initStrategy(viewInflationContext);
		ExtendedResourcesFactory.loadExtendedBackground(internals().viewObject(), viewInflationContext);
	}

}
