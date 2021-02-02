package pl.edu.mimuw.students.pl249278.android.svg;

public class ArrayFloatList {
	private float[] data;
	private int index;

	public ArrayFloatList(int initialCapacity) {
		data = new float[initialCapacity];
	}

	public void add(float value) {
		if(index == data.length) {
			// enlarge storage
			float[] newData = new float[data.length << 1];
			System.arraycopy(data, 0, newData, 0, index);
			data = newData;
		}
		data[index] = value;
		index++;
	}

	public void clear() {
		index = 0;
	}

	public float[] toFloatArray() {
		float[] result = new float[index];
		System.arraycopy(data, 0, result, 0, index);
		return result;
	}

}
