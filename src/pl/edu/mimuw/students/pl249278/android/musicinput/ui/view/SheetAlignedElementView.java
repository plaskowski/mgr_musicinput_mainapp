package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import android.content.Context;
import android.util.AttributeSet;

public class SheetAlignedElementView extends SheetElementView<SheetAlignedElement> {
	
	public SheetAlignedElementView(Context context, SheetAlignedElement model) {
		super(context, model);
		model.setTag(this);
	}
	
	@Override
	public void setModel(SheetAlignedElement model) {
		super.setModel(model);
		model.setTag(this);
	}
	
	public SheetAlignedElementView(Context context) {
		super(context);
	}

	public SheetAlignedElementView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SheetAlignedElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}
	
