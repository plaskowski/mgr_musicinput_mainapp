
package pl.edu.mimuw.students.pl249278.android.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

public class ReflectionUtils {
	private static LogUtils log = new LogUtils(ReflectionUtils.class);

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
		return findConstName(constWrapperClass, constNamePrefix, constValue, "(NO SUCH CONST)");
	}
	
	public static String findConstName(Class<?> constWrapperClass, String constNamePrefix, Object constValue, String defaultValue) {
		for (Field field: findConsts(constWrapperClass, constNamePrefix)) {
			try {
				Object fieldValue = field.get(null);
				if(fieldValue.equals(constValue)) return field.getName();
			} catch (IllegalArgumentException e) {
				log.e("", e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				log.e("", e);
			}
		}
		return defaultValue;
	}
}
