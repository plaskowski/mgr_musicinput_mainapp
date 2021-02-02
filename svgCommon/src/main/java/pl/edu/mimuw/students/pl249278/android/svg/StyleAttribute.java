package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.HashMap;

public enum StyleAttribute {
	FILL (ValueType.PAINT),
	FILL_OPACITY (ValueType.OPACITY_VALUE),
	STROKE (ValueType.PAINT),
	STROKE_OPACITY (ValueType.OPACITY_VALUE);
	
	public ValueType type;
	
	private StyleAttribute(ValueType type) {
		this.type = type;
	}

	public static HashMap<String, StyleAttribute> textLabels;
	static {
		textLabels = new HashMap<String, StyleAttribute>();
		StyleAttribute[] values = StyleAttribute.values();
		for (int i = 0; i < values.length; i++) {
			StyleAttribute attr = values[i];
			String label = attr.name().toLowerCase().replace('_', '-');
			textLabels.put(label, attr);
		}
	}
	
	public static enum ValueType {
		PAINT,
		OPACITY_VALUE
		;
		
		public static final int PAINT_NONE = -1;
	}
}
