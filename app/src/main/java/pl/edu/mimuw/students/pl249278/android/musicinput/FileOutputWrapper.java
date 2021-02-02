package pl.edu.mimuw.students.pl249278.android.musicinput;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import pl.edu.mimuw.students.pl249278.io.RandomAccessDataOutput;

public class FileOutputWrapper extends DataOutputStream implements RandomAccessDataOutput {
	private FileOutputStream stream;

	public FileOutputWrapper(FileOutputStream stream) {
		super(stream);
		this.stream = stream;
	}

	@Override
	public long getPosition() throws IOException {
		return stream.getChannel().position();
	}

	@Override
	public void seek(long position) throws IOException {
		stream.getChannel().position(position);
	}	
	
}
