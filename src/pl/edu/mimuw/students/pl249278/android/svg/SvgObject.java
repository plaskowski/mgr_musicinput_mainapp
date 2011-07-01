package pl.edu.mimuw.students.pl249278.android.svg;

import java.util.EnumMap;

public abstract class SvgObject {
	EnumMap<StyleAttribute, Object> style = new EnumMap<StyleAttribute, Object>(StyleAttribute.class);
}
