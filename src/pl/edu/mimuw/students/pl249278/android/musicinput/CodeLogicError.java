package pl.edu.mimuw.students.pl249278.android.musicinput;

@SuppressWarnings("serial")
public class CodeLogicError extends Error {

	public CodeLogicError(String detailMessage) {
		super(detailMessage);
	}
	
	/**
	 * Thrown when we think we handled any possible value of enum, but it's not true
	 */
	public static <T extends Enum<T>> CodeLogicError unhandledEnumValue(T value) {
		return new CodeLogicError("Unhandled value of enum: "+value.getClass().getCanonicalName()+"."+value.name());
	}

}
