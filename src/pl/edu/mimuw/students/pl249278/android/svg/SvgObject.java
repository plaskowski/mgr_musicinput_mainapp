package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.EnumMap;

public abstract class SvgObject {
	EnumMap<StyleAttribute, Object> style = new EnumMap<StyleAttribute, Object>(StyleAttribute.class);
	
	public Object getStyleProperty(StyleAttribute attr, Object defaultVal) {
		Object result = style.get(attr);
		return result != null ? result : defaultVal;
	}
	
	public float getFloatProperty(StyleAttribute attr, float defaultVal) {
		Object result = style.get(attr);
		return result != null ? (Float) result : defaultVal;
	}
	
	public int getIntProperty(StyleAttribute attr, int defaultVal) {
		Object result = style.get(attr);
		return result != null ? (Integer) result : defaultVal;
	}
}
