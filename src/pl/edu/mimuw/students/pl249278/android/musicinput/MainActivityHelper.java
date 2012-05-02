package pl.edu.mimuw.students.pl249278.android.musicinput;

import android.os.Parcel;
import android.os.Parcelable;

/** 
 * Gather all static classes used exclusively by {@link MainActivity}, 
 * that are not necessary for understanding the code,
 * for clarity purpose.
 */
public class MainActivityHelper {
	static class DuplicateRequest implements Parcelable {
		long scoreId;
		String requestId;
		
		public DuplicateRequest(long scoreId, String requestId) {
			this.scoreId = scoreId;
			this.requestId = requestId;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeLong(scoreId);
			out.writeString(requestId);
		}
		
		private DuplicateRequest(Parcel in) {
			scoreId = in.readLong();
			requestId = in.readString();
		}
		
		public static final Parcelable.Creator<DuplicateRequest> CREATOR = new Parcelable.Creator<DuplicateRequest>() {
			public DuplicateRequest createFromParcel(Parcel in) {
				return new DuplicateRequest(in);
			}
			
			public DuplicateRequest[] newArray(int size) {
				return new DuplicateRequest[size];
			}
		};
		
		public int describeContents() {
			return 0;
		}
	}	

	static class ExportMidiRequest implements Parcelable {
		long scoreId;
		String filename;
		String requestId;
		
		public ExportMidiRequest(long scoreId, String filename) {
			this.scoreId = scoreId;
			this.filename = filename;
		}

		public void writeToParcel(Parcel out, int flags) {
			out.writeLong(scoreId);
			out.writeString(filename);
			out.writeString(requestId);
		}

		private ExportMidiRequest(Parcel in) {
			scoreId = in.readLong();
			filename = in.readString();
			requestId = in.readString();
		}

		public static final Parcelable.Creator<ExportMidiRequest> CREATOR = new Parcelable.Creator<ExportMidiRequest>() {
			public ExportMidiRequest createFromParcel(Parcel in) {
				return new ExportMidiRequest(in);
			}

			public ExportMidiRequest[] newArray(int size) {
				return new ExportMidiRequest[size];
			}
		};

		public int describeContents() {
			return 0;
		}


	}

}
