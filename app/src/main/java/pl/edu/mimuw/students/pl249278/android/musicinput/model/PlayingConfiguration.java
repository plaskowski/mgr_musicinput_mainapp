package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;

public class PlayingConfiguration implements Parcelable {
	private int tempo;
	private boolean playMetronome;
	private boolean prependEmptyBar;
	private boolean loop;
	
	public PlayingConfiguration(int tempo, boolean playMetronome,
			boolean prependEmptyBar, boolean loop) {
		this.tempo = tempo;
		this.playMetronome = playMetronome;
		this.prependEmptyBar = prependEmptyBar;
		this.loop = loop;
	}
	public int getTempo() {
		return tempo;
	}
	public boolean isPlayMetronome() {
		return playMetronome;
	}
	public boolean isPrependEmptyBar() {
		return prependEmptyBar;
	}
	public boolean isLoop() {
		return loop;
	}
	public void setLoop(boolean loop) {
		this.loop = loop;
	}	
	public void setTempo(int tempo) {
		this.tempo = tempo;
	}
	public void setPlayMetronome(boolean playMetronome) {
		this.playMetronome = playMetronome;
	}
	public void setPrependEmptyBar(boolean prependEmptyBar) {
		this.prependEmptyBar = prependEmptyBar;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(tempo);
		out.writeInt(IntUtils.asFlagVal(playMetronome));
		out.writeInt(IntUtils.asFlagVal(prependEmptyBar));
		out.writeInt(IntUtils.asFlagVal(loop));
	}

	private PlayingConfiguration(Parcel in) {
		this(
			in.readInt(),
			IntUtils.asBool(in.readInt()),
			IntUtils.asBool(in.readInt()),
			IntUtils.asBool(in.readInt())
		);
	}

	/** copy constructor */
	public PlayingConfiguration(PlayingConfiguration source) {
		this(source.tempo, source.playMetronome, source.prependEmptyBar, source.loop);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlayingConfiguration that = (PlayingConfiguration) o;
		return tempo == that.tempo &&
				playMetronome == that.playMetronome &&
				prependEmptyBar == that.prependEmptyBar &&
				loop == that.loop;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(tempo, playMetronome, prependEmptyBar, loop);
	}

	public static final Parcelable.Creator<PlayingConfiguration> CREATOR = new Parcelable.Creator<PlayingConfiguration>() {
		public PlayingConfiguration createFromParcel(Parcel in) {
			return new PlayingConfiguration(in);
		}

		public PlayingConfiguration[] newArray(int size) {
			return new PlayingConfiguration[size];
		}
	};

	public int describeContents() {
		return 0;
	}


}
