package pl.edu.mimuw.students.pl249278.android.common;

public class ParcelUtils {
	public static <T extends Enum<T>> T stringToEnum(Class<T> enumClass, String stringRep) {
		if(stringRep == null)
			return null;
		try {
			return Enum.valueOf(enumClass, stringRep);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public static String enumToString(Enum<?> value) {
		if(value == null) return null;
		else return value.name();
	}
}
