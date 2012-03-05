package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Score {
	public static final long NO_ID = -1L;
	
	private long id = NO_ID;
	private long originalId = NO_ID;
	private String title;
	private long creationUtcStamp, modificationUtcStamp;
	private String rawContent = null;
	private List<ScoreContentElem> content = null;
	
    public Score(String title, List<ScoreContentElem> content) {
		super();
		this.title = title;
		this.content = content;
	}

	private Score(Parcel in) {
		id = in.readLong();
		originalId = in.readLong();
    	title = in.readString();
    	creationUtcStamp = in.readLong();
    	modificationUtcStamp = in.readLong();
    	rawContent = in.readString();
    }
    
	
	public Score(long id, long originalId, String title, String rawContent, long creationUtcStamp,
			long modificationUtcStamp) {
		this.id = id;
		this.originalId = originalId;
		this.title = title;
		this.rawContent = rawContent;
		this.creationUtcStamp = creationUtcStamp;
		this.modificationUtcStamp = modificationUtcStamp;
	}

	private void writeToParcel(Parcel out, int flags) throws SerializationException {
		out.writeLong(id);
		out.writeLong(originalId);
		out.writeString(title);
		out.writeLong(creationUtcStamp);
		out.writeLong(modificationUtcStamp);
        out.writeString(getRawContent());
    }
    
    public Parcelable prepareParcelable() throws SerializationException {
    	// try detect any possible serialization errors here
    	getRawContent();
    	return new ParcelableScore(this);
    }

    public String getRawContent() throws SerializationException {
    	if(rawContent == null) {
    		rawContent = ScoreContentFactory.serialize(content);
    	}
    	return rawContent;
	}

    public List<ScoreContentElem> getContent() throws SerializationException {
    	if(content == null) {
    		content = ScoreContentFactory.deserialize(rawContent);
    	}
    	return content;
    }

    /** suppress serialization errors */
    public static class ParcelableScore implements Parcelable {
	    private Score source;
	    
	    private ParcelableScore(Score source) {
			this.source = source;
		}
	
		public int describeContents() {
	        return 0;
	    }
	
	    public void writeToParcel(Parcel out, int flags) {
	    	try {
				source.writeToParcel(out, flags);
			} catch (SerializationException e) {
				Log.e(LogUtils.COMMON_TAG, "suppress serialization error", e);
				throw new RuntimeException(e);
			}
	    }
	
	    public static final Parcelable.Creator<ParcelableScore> CREATOR
	            = new Parcelable.Creator<ParcelableScore>() {
	        public ParcelableScore createFromParcel(Parcel in) {
	            return new ParcelableScore(new Score(in));
	        }
	
	        public ParcelableScore[] newArray(int size) {
	            return new ParcelableScore[size];
	        }
	    };

		public Score getSource() {
			return source;
		}
    }

	public String getTitle() {
		return title;
	}

	public void setContent(List<ScoreContentElem> content) {
		this.content = content;
		this.rawContent = null;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getOriginalId() {
		return originalId;
	}

	public void setOriginalId(long originalId) {
		this.originalId = originalId;
	}
}
