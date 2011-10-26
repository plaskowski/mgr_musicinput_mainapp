package pl.edu.mimuw.students.pl249278.android.common;

import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Shader;

public class PaintBuilder {
	Paint template;

	private PaintBuilder(Paint result) {
		this.template = result;
	}

	public static PaintBuilder init() {
		return new PaintBuilder(new Paint());
	}
	
	public Paint build() {
		return new Paint(template);
	}
	
	@Override
	public PaintBuilder clone() {
		return new PaintBuilder(new Paint(this.template));
	}
	
	public PaintBuilder antialias(boolean aa) {
		template.setAntiAlias(aa);
		return this;
	}
	
	public PaintBuilder style(Style style) {
		template.setStyle(style);
		return this;
	}
	
	public PaintBuilder color(int color) {
		template.setColor(color);
		return this;
	}
	
	public PaintBuilder strokeWidth(float width) {
		template.setStrokeWidth(width);
		return this;
	}
	
	public PaintBuilder pathEffect(PathEffect pathEffect) {
		template.setPathEffect(pathEffect);
		return this;
	}

	public PaintBuilder shader(Shader shader) {
		template.setShader(shader);
		return this;
	}
}
