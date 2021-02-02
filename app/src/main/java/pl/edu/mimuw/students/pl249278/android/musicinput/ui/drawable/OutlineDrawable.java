package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import android.graphics.Canvas;
import android.graphics.Paint;

public class OutlineDrawable extends CompoundDrawable {
	
	public OutlineDrawable() {
	}

	public OutlineDrawable(StyleResolver resolver) {
		super(resolver);
	}

	@Override
	protected void draw(Canvas canvas, Paint paint, float width, float height) {
		canvas.drawRect(0, 0, width, height, paint);
	}

}
