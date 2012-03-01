package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import android.os.Parcel;
import android.os.Parcelable;
import pl.edu.mimuw.students.pl249278.android.common.ParcelUtils;

public class ScoreVisualizationConfig implements Parcelable {
	private DisplayMode displayMode;
	private int minSpaceAnchor;
	private int maxSpaceAnchor;
	
	public ScoreVisualizationConfig(DisplayMode displayMode,
			int minSpaceAnchor, int maxSpaceAnchor) {
		this.displayMode = displayMode;
		this.minSpaceAnchor = minSpaceAnchor;
		this.maxSpaceAnchor = maxSpaceAnchor;
	}

	public static enum DisplayMode {
		NORMAL,
		UPPER_VOICE,
		LOWER_VOICE
	}
	
	@Override
	public int describeContents() {
        return 0;
    }

	private ScoreVisualizationConfig(Parcel in) {
		displayMode = ParcelUtils.stringToEnum(DisplayMode.class, in.readString());
		minSpaceAnchor = in.readInt();
		maxSpaceAnchor = in.readInt();
    }
    
	@Override
    public void writeToParcel(Parcel out, int flags) {
    	out.writeString(ParcelUtils.enumToString(displayMode));
    	out.writeInt(minSpaceAnchor);
    	out.writeInt(maxSpaceAnchor);
    }

    public static final Parcelable.Creator<ScoreVisualizationConfig> CREATOR
            = new Parcelable.Creator<ScoreVisualizationConfig>() {
        public ScoreVisualizationConfig createFromParcel(Parcel in) {
            return new ScoreVisualizationConfig(in);
        }

        public ScoreVisualizationConfig[] newArray(int size) {
            return new ScoreVisualizationConfig[size];
        }
    };

	public DisplayMode getDisplayMode() {
		return displayMode;
	}

	public int getMinSpaceAnchor() {
		return minSpaceAnchor;
	}

	public int getMaxSpaceAnchor() {
		return maxSpaceAnchor;
	}

	public void setDisplayMode(DisplayMode displayMode) {
		this.displayMode = displayMode;
	}

	public void setMinSpaceAnchor(int minSpaceAnchor) {
		this.minSpaceAnchor = minSpaceAnchor;
	}

	public void setMaxSpaceAnchor(int maxSpaceAnchor) {
		this.maxSpaceAnchor = maxSpaceAnchor;
	}

}
