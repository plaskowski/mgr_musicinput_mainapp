package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.ExtendedResourcesFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.PaintSetup;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetAlignedElement;
import android.content.Context;
import android.content.res.TypedArray;
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
		loadPaint(ExtendedResourcesFactory.styleResolver(context, attrs, defStyle));
	}

	private void loadPaint(StyleResolver styleResolver) {
		TypedArray values = styleResolver.obtainStyledAttributes(R.styleable.SheetAlignedElementView);
		try {
			int paintId = values.getResourceId(R.styleable.SheetAlignedElementView_paint, -1);
			if(paintId != -1) {
				PaintSetup paint = ExtendedResourcesFactory.createPaintSetup(styleResolver, paintId);
				setPaint(paint.paint, paint.drawRadius);
			}
		} finally {
			values.recycle();
		}
	}

	public SheetAlignedElementView(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadPaint(ExtendedResourcesFactory.styleResolver(context, attrs));
	}
}
	
