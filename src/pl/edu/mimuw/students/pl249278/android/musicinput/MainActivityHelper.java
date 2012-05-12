package pl.edu.mimuw.students.pl249278.android.musicinput;

import pl.edu.mimuw.students.pl249278.android.musicinput.MainActivity.ReceiverType;
import android.os.Parcel;
import android.os.Parcelable;

/** 
 * Gather all static classes used exclusively by {@link MainActivity}, 
 * that are not necessary for understanding the code,
 * for clarity purpose.
 */
public class MainActivityHelper {
	static class ReceiverState implements Parcelable {
		final ReceiverType type;
		String requestId;

		public ReceiverState(ReceiverType type, String requestId) {
			this.type = type;
			this.requestId = requestId;
		}
		
		public ReceiverState(Parcel in) {
			this(ReceiverType.values()[in.readInt()], in.readString());
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(type.ordinal());
			dest.writeString(requestId);
		}
		
		public static final Parcelable.Creator<ReceiverState> CREATOR = new Parcelable.Creator<ReceiverState>() {
			public ReceiverState createFromParcel(Parcel in) {
				return new ReceiverState(in);
			}
			
			public ReceiverState[] newArray(int size) {
				return new ReceiverState[size];
			}
		};
		
		@Override
		public int describeContents() {
			return 0;
		}
	}
	
	static class ByScoreIdRequest extends ReceiverState {
		long scoreId;
		
		public ByScoreIdRequest(ReceiverType type, long scoreId, String requestId) {
			super(type, requestId);
			this.scoreId = scoreId;
		}

		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeLong(scoreId);
		}
		
		private ByScoreIdRequest(Parcel in) {
			super(in);
			scoreId = in.readLong();
		}
		
		public static final Parcelable.Creator<ByScoreIdRequest> CREATOR = new Parcelable.Creator<ByScoreIdRequest>() {
			public ByScoreIdRequest createFromParcel(Parcel in) {
				return new ByScoreIdRequest(in);
			}
			
			public ByScoreIdRequest[] newArray(int size) {
				return new ByScoreIdRequest[size];
			}
		};
		
		public int describeContents() {
			return 0;
		}
	}	

	static class ExportMidiRequest extends ReceiverState {
		long scoreId;
		String filename;
		
		public ExportMidiRequest(long scoreId, String filename) {
			super(ReceiverType.SCORE_EXPORTED, null);
			this.scoreId = scoreId;
			this.filename = filename;
		}

		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeLong(scoreId);
			out.writeString(filename);
		}

		private ExportMidiRequest(Parcel in) {
			super(in);
			this.scoreId = in.readLong();
			this.filename = in.readString();
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
