package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePrimitives {
	public static class ParcelableString extends Template<String> {

		public ParcelableString(String value) {
			super(value);
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeString(value);
		}
	
		private ParcelableString(Parcel in) {
			this(in.readString());
		}
	
		public static final Parcelable.Creator<ParcelableString> CREATOR = new Parcelable.Creator<ParcelableString>() {
			public ParcelableString createFromParcel(Parcel in) {
				return new ParcelableString(in);
			}
	
			public ParcelableString[] newArray(int size) {
				return new ParcelableString[size];
			}
		};
	
	}
	
	public static class ParcelableLong extends Template<Long> {

		public ParcelableLong(Long value) {
			super(value);
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeLong(value);
		}
	
		private ParcelableLong(Parcel in) {
			this(in.readLong());
		}
	
		public static final Parcelable.Creator<ParcelableLong> CREATOR = new Parcelable.Creator<ParcelableLong>() {
			public ParcelableLong createFromParcel(Parcel in) {
				return new ParcelableLong(in);
			}
	
			public ParcelableLong[] newArray(int size) {
				return new ParcelableLong[size];
			}
		};
	
	}
	
	private static abstract class Template<V> implements Parcelable {
		public V value;

		public Template(V value) {
			this.value = value;
		}
		
		public int describeContents() {
			return 0;
		}
	}
}
