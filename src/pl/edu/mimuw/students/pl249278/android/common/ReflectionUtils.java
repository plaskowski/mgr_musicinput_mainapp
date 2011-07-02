
package pl.edu.mimuw.students.pl249278.android.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.util.Log;

public class ReflectionUtils {
	private static final int CONST_MODIFIERS = (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

	public static String findConst(Class<?> classObj, String constPrefix, Object value) {
		Field[] fields = classObj.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			if(field.getModifiers() != CONST_MODIFIERS || !field.getName().startsWith(constPrefix))
				continue;
			try {
				Object fieldValue = field.get(null);
				if(fieldValue.equals(value)) return field.getName();
			} catch (IllegalArgumentException e) {
				Log.e(ReflectionUtils.class.getName(), "", e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.e(ReflectionUtils.class.getName(), "", e);
			}
		}
		return "(NO SUCH CONST)";
	}
}
