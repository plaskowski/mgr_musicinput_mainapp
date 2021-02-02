package pl.edu.mimuw.students.pl249278.android.musicinput.model;

@SuppressWarnings("serial")
public class SerializationException extends Exception {

	public SerializationException(String detailMessage) {
		super(detailMessage);
	}

	public SerializationException(String detailMesage, Throwable e) {
		super(detailMesage, e);
	}
	
}
