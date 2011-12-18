package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawable;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.StyleResolver;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;

public class SvgCompoundDrawable extends CompoundDrawable {
	private SvgImage svgImage;
	
	public SvgCompoundDrawable(SvgImage svgImage) {
		this.svgImage = svgImage;
	}
	
	public SvgCompoundDrawable(StyleResolver resolver) {
		super(resolver);
		TypedArray values = resolver.obtainStyledAttributes(R.styleable.SvgDrawable);
		try {
			int xmlId = values.getResourceId(R.styleable.SvgDrawable_svgResource, 0);
			if(xmlId != 0) {
				this.svgImage = NotePartFactory.prepareSvgImage(resolver.getResources(), xmlId);
			}
		} catch (LoadingSvgException e) {
			throw new RuntimeException(e);
		} finally {
			values.recycle();
		}
	}

	@Override
	protected void draw(Canvas canvas, Paint paint, float width, float height) {
		float Xscale = width/svgImage.getWidth();
		float Yscale = height/svgImage.getHeight();
		if(Xscale == Yscale) {
			SvgRenderer.drawSvgImage(canvas, svgImage, Xscale, paint);
		} else {
			canvas.save();
			if(Xscale < Yscale) {
				canvas.translate(0, (height-Xscale*svgImage.getHeight())/2f);
			} else {
				canvas.translate((width-Yscale*svgImage.getWidth())/2f, 0);
			}
			SvgRenderer.drawSvgImage(canvas, svgImage, Math.min(Xscale, Yscale), paint);
			canvas.restore();
		}
	}
}