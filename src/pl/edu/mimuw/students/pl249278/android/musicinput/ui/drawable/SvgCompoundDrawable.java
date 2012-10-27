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
	private float iconMaxHeight = -1;
	private int intrinsicWidth = -1, intrinsicHeight = -1;
	
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
			iconMaxHeight = values.getDimension(R.styleable.SvgDrawable_iconMaxHeight, -1);
			intrinsicWidth = values.getDimensionPixelSize(R.styleable.SvgDrawable_intrinsicWidth, -1);
			intrinsicHeight = values.getDimensionPixelSize(R.styleable.SvgDrawable_intrinsicHeight, -1);
		} catch (LoadingSvgException e) {
			throw new RuntimeException(e);
		} finally {
			values.recycle();
		}
	}

	@Override
	protected void draw(Canvas canvas, Paint paint, float width, float height) {
		float Xscale = width/svgImage.getWidth();
		float drawingHeight = height;
		if(iconMaxHeight > 0) {
			drawingHeight = iconMaxHeight;
		}
		float Yscale = drawingHeight/svgImage.getHeight();
		float scale = Math.min(Xscale, Yscale);
		canvas.save();
		// center drawing inside available area
		canvas.translate(
			(width-scale*svgImage.getWidth())/2f,
			(height-scale*svgImage.getHeight())/2f
		);
		SvgRenderer.drawSvgImage(canvas, svgImage, scale, paint);
		canvas.restore();
	}
	
	@Override
	public int getIntrinsicWidth() {
		return intrinsicWidth != -1 ? intrinsicWidth : (int) svgImage.getWidth();
	}
	
	@Override
	public int getIntrinsicHeight() {
		return intrinsicHeight != -1 ? intrinsicHeight : (int) svgImage.getHeight();
	}
}