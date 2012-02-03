
package pl.edu.mimuw.students.pl249278.android.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

import android.util.Log;

public class ReflectionUtils {
	public static Collection<Field> findConsts(Class<?> constWrapperClass, String constNamePrefix) {
		Field[] fields = constWrapperClass.getDeclaredFields();
		Collection<Field> result = new LinkedList<Field>();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			int modifiers = field.getModifiers();
			if(field.getName().startsWith(constNamePrefix) && Modifier.isStatic(modifiers) 
			&& !Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)
			&& Modifier.isFinal(modifiers) && field.getDeclaringClass().equals(constWrapperClass)) {
				result.add(field);
			}
		}
		return result;
	}

	public static String findConstName(Class<?> constWrapperClass, String constNamePrefix, Object constValue) {
		for (Field field: findConsts(constWrapperClass, constNamePrefix)) {
			try {
				Object fieldValue = field.get(null);
				if(fieldValue.equals(constValue)) return field.getName();
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
