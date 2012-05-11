package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.common.Macros.ifNotNull;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LEN_QUATERNOTE;
import static pl.edu.mimuw.students.pl249278.midi.MidiTrack.CHANNELEVENT_TYPE_NOTE_OFF;
import static pl.edu.mimuw.students.pl249278.midi.MidiTrack.CHANNELEVENT_TYPE_NOTE_ON;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.IntUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ChromaticScalePitch;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.DiatonicPitch;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.DiatonicScalePitch;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Pitch;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PauseSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.PlayingConfiguration;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentElem;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec;
import pl.edu.mimuw.students.pl249278.midi.MidiFile;
import pl.edu.mimuw.students.pl249278.midi.MidiFile.Type;
import pl.edu.mimuw.students.pl249278.midi.MidiFormatException;
import pl.edu.mimuw.students.pl249278.midi.MidiTrack;

public class MidiBuilder {
	/** default time step for playing metronome purpose */
	private static final TimeStep DEFAULT_TIME_STEP = new TimeStep(4, NoteConstants.LEN_QUATERNOTE);
	private static final int MIN_TICKS_PER_BEAT = 16;
	private static final byte NOTE_VELOCITY = 100;
	/** shift of index between enumeration of octaves between scientific notation and MIDI standard */
	private static final int SCIENTIFIC_TO_MIDI_OCTAVE = 1;
	
	public static int initialDelay(List<ScoreContentElem> score, int measureUnitLength, PlayingConfiguration conf) {
		if(conf.isPrependEmptyBar()) {
			TimeStep introTS = DEFAULT_TIME_STEP;
			ScoreIterator it = new ScoreIterator(score);
			ScoreContentElem firstElem = it.previewNext();
			if(firstElem != null && firstElem instanceof TimeSpec) {
				introTS = ifNotNull(((TimeSpec) firstElem).getTimeStep(), introTS);
			}
			return introTS.getBaseMultiplier() * ElementSpec.length(introTS.getTempoBaseLength(), measureUnitLength);
		} else {
			return 0;
		}
	}
	
	public static MidiFile build(List<ScoreContentElem> score, int measureUnitLength, PlayingConfiguration conf) throws MidiFormatException {
		MidiBuilder midiBuilder = new MidiBuilder();
		return midiBuilder.mBuild(score, measureUnitLength, conf);
	}

	private MidiTrack musicTrack;
	private byte musicChannel;
	private byte postponedNoteValue = -1;
	private int postponedNoteDelay = 0;
	private int postponedNoteDuration = 0;
	
	private MidiFile mBuild(List<ScoreContentElem> score, int measureUnitLength, PlayingConfiguration conf) throws MidiFormatException {
		int deltaticksPerBeat;
		if(measureUnitLength > LEN_QUATERNOTE  && 1 << (measureUnitLength - LEN_QUATERNOTE) > MIN_TICKS_PER_BEAT) {
			deltaticksPerBeat = 1 << (measureUnitLength - LEN_QUATERNOTE);
		} else {
			deltaticksPerBeat = MIN_TICKS_PER_BEAT;
		}
		int tickLength = IntUtils.log2(deltaticksPerBeat) + LEN_QUATERNOTE;
		MidiFile result = new MidiFile(
			conf.isPlayMetronome() ? Type.MULTIPLE_SYNCHRONOUS_TRACKS : Type.SINGLE_TRACK, 
			(short) deltaticksPerBeat
		);
		
		if(conf.isPlayMetronome()) {
			MidiTrack metronomeTrack = new MidiTrack();
			metronomeTrack.setTempoInBPM(conf.getTempo());
			result.addTrack(metronomeTrack);
			/** channel 10 (enumerating from 1) is reserved for percussion sets */
			byte channel = 9;
			/** value 37 matches "side stick" patch */
			byte metronomeNoteValue = 37;
			ScoreIterator iterator = new ScoreIterator(score);
			if(conf.isPrependEmptyBar()) {
				ScoreContentElem first = iterator.previewNext();
				if(first == null || !(first instanceof TimeSpec)) {
					throw new MidiFormatException("First element of Score content isn't a TimeSpec");
				}
				TimeStep timeStep = ((TimeSpec) first).getTimeStep();
				if(timeStep == null) {
					timeStep = DEFAULT_TIME_STEP;
				}
				int deltaTime = ElementSpec.length(timeStep.getTempoBaseLength(), tickLength);
				for(int i = 0; i < timeStep.getBaseMultiplier(); i++) {
					metronomeTrack.appendChannelEvent(0, CHANNELEVENT_TYPE_NOTE_ON, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
					metronomeTrack.appendChannelEvent(deltaTime, CHANNELEVENT_TYPE_NOTE_OFF, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
				}
			}
			/** total time length of notes/rests in currently parsed bar [in ticks] */
			int currentBarDuration = 0;
			TimeStep currentBarTimeStep = DEFAULT_TIME_STEP;
			/** delay before current bar start [in ticks] */
			int delay = 0;
			for (;iterator.hasNext();) {
				ScoreContentElem elem = (ScoreContentElem) iterator.next();
				if(elem instanceof NoteSpec) {
					NoteSpec note = (NoteSpec) elem;
					currentBarDuration += ElementSpec.overallLength(note, tickLength);
				} else if(elem instanceof PauseSpec) {
					PauseSpec pause = (PauseSpec) elem;
					currentBarDuration += ElementSpec.overallLength(pause, tickLength);
				} else if(elem instanceof TimeSpec) {
					// handle recently closed bar first
					int tempoUnit = ElementSpec.length(currentBarTimeStep.getTempoBaseLength(), tickLength);
					for(; currentBarDuration >= tempoUnit; currentBarDuration -= tempoUnit) {
						metronomeTrack.appendChannelEvent(delay, CHANNELEVENT_TYPE_NOTE_ON, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
						delay = 0;
						metronomeTrack.appendChannelEvent(tempoUnit, CHANNELEVENT_TYPE_NOTE_OFF, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
					}
					delay = currentBarDuration;
					// setup for new bar
					TimeSpec time = (TimeSpec) elem;
					currentBarDuration = 0;
					currentBarTimeStep = ifNotNull(time.getTimeStep(), currentBarTimeStep);
				}
			}
			// handle last bar
			int tempoUnit = ElementSpec.length(currentBarTimeStep.getTempoBaseLength(), tickLength);
			for(; currentBarDuration >= tempoUnit; currentBarDuration -= tempoUnit) {
				metronomeTrack.appendChannelEvent(delay, CHANNELEVENT_TYPE_NOTE_ON, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
				delay = 0;
				metronomeTrack.appendChannelEvent(tempoUnit, CHANNELEVENT_TYPE_NOTE_OFF, channel, (byte) metronomeNoteValue, NOTE_VELOCITY);
			}
		}
		
		musicTrack = new MidiTrack();
		musicTrack.setTempoInBPM(conf.getTempo());
		result.addTrack(musicTrack);
		musicChannel = 1;
		Clef clef = null;
		/** delay (in delta-time ticks) before playing next note */
		int delay = initialDelay(score, tickLength, conf);
		EnumMap<DiatonicScalePitch, NoteModifier> barAccidentals = new EnumMap<DiatonicScalePitch, NoteModifier>(DiatonicScalePitch.class);
		EnumMap<DiatonicScalePitch, NoteModifier> keySignAccidentals = new EnumMap<DiatonicScalePitch, NoteModifier>(DiatonicScalePitch.class);
		for (Iterator<ScoreContentElem> iterator = new ScoreIterator(score); iterator.hasNext();) {
			ScoreContentElem elem = (ScoreContentElem) iterator.next();
			if(elem instanceof NoteSpec) {
				NoteSpec note = (NoteSpec) elem;
				DiatonicPitch dPitch = offset(clef.diatonicNote, clef.anhorIndex - note.positon());
				if(note.getToneModifier() != null) {
					barAccidentals.put(dPitch.basePitch, note.getToneModifier());
				}
				NoteModifier modifier = keySignAccidentals.get(dPitch.basePitch);
				if(barAccidentals.get(dPitch.basePitch) != null) {
					modifier = barAccidentals.get(dPitch.basePitch);
				}
				Pitch pitch = modify(dPitch, modifier);
				int noteValue = toMidiNoteValue(pitch);
				assert noteValue >= Byte.MIN_VALUE && noteValue <= Byte.MAX_VALUE;
				int deltaTime = ElementSpec.overallLength(note, tickLength);
				if(postponedNoteValue != -1 && postponedNoteValue == noteValue) {
					postponedNoteDuration += deltaTime;
					if(!note.hasJoinArc()) {
						addPostponed();
					}
					continue;
				}
				addPostponedIfPresent();
				if(note.hasJoinArc()) {
					postponedNoteValue = (byte) noteValue;
					postponedNoteDelay = delay;
					postponedNoteDuration = deltaTime;
				} else {
					addNoteOccurence((byte) noteValue, delay, deltaTime);
				}
				delay = 0;
			} else if(elem instanceof PauseSpec) {
				addPostponedIfPresent();
				PauseSpec pause = (PauseSpec) elem;
				delay += ElementSpec.overallLength(pause, tickLength);
			} else if(elem instanceof TimeSpec) {
				TimeSpec time = (TimeSpec) elem;
				clef = notNull(time.getClef(), clef);
				barAccidentals.clear();
				if(time.getKeySignature() != null) {
					keySignAccidentals.clear();
					DiatonicScalePitch[] pitches = time.getKeySignature().pitches;
					for (int i = 0; i < pitches.length; i++) {
						keySignAccidentals.put(
							pitches[i],
							time.getKeySignature().modifier
						);
					}
				}
			}
		}
		addPostponedIfPresent();
		
		return result;
	}

	private void addPostponedIfPresent() throws MidiFormatException {
		if(postponedNoteValue != -1) {
			addPostponed();
		}
	}

	private void addPostponed() throws MidiFormatException {
		addNoteOccurence(postponedNoteValue, postponedNoteDelay, postponedNoteDuration);
		postponedNoteValue = -1;
	}

	private void addNoteOccurence(byte noteValue, int delay,
			int duration) throws MidiFormatException {
		musicTrack.appendChannelEvent(delay, CHANNELEVENT_TYPE_NOTE_ON, musicChannel, noteValue, NOTE_VELOCITY);
		musicTrack.appendChannelEvent(duration, CHANNELEVENT_TYPE_NOTE_OFF, musicChannel, noteValue, NOTE_VELOCITY);
	}

	private static int toMidiNoteValue(Pitch pitch) {
		return (pitch.octaveIndex + SCIENTIFIC_TO_MIDI_OCTAVE) * ChromaticScalePitch.values().length + pitch.basePitch.ordinal();
	}

	/**
	 * @param modifier may be null
	 */
	private static Pitch modify(DiatonicPitch dPitch, NoteModifier modifier) {
		int total = ChromaticScalePitch.values().length;
		int diff = 0;
		if(modifier != null) switch(modifier) {
		case SHARP:
			diff = 1;
			break;
		case FLAT:
			diff = -1;
			break;
		}
		int absPitchIndex = dPitch.octaveIndex * total + dPitch.basePitch.chromatic.ordinal() + diff;
		return new Pitch(
			cycle(dPitch.basePitch.chromatic, diff, ChromaticScalePitch.values()),
			absPitchIndex / total
		);
	}
	
	private static <T extends Enum<T>> T cycle(T value, int delta, T[] spectrum) {
		int index = (value.ordinal() + delta) % spectrum.length;
		if(index < 0) index += spectrum.length;
		return spectrum[index];
	}
	
	private static <T> T notNull(T value, T defaultValue) {
		return value != null ? value : defaultValue;
	}

	private static DiatonicPitch offset(DiatonicPitch pitch,
			int diatonicScaleDiff) {
		int total = DiatonicScalePitch.values().length;
		return new DiatonicPitch(
			cycle(pitch.basePitch, diatonicScaleDiff, DiatonicScalePitch.values()),
			(pitch.octaveIndex * total + pitch.basePitch.ordinal() + diatonicScaleDiff)/total
		);
	}
}
