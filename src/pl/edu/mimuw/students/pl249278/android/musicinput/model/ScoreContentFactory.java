package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;

public class ScoreContentFactory {
	private static final int CURRENT_VERSION = 1;
	private static final String SEPARATOR = " ";
	private static final String TYPE_PAUSE_LABEL = "P";
	private static final String TYPE_NOTE_LABEL = "N";
	private static final String TYPE_TIME_LABEL = "T";
	private static final String NULL_LABEL = "NULL";
	private static final String COMMON_TIME_LABEL = "CT";
	private static final String CUT_COMMON_TIME_LABEL = "CCT";
	private static final String TIMESTEP_CUSTOM_SEPARATOR = "_";
	private static final String SET_START = "{";
	private static final String SET_END = "}";
	
	public static String serialize(List<ScoreContentElem> content) 
		throws SerializationException {
		StringBuilder builder = new StringBuilder();
		builder.append(CURRENT_VERSION);
		for(ScoreContentElem elem: content) {
			if(elem instanceof NoteSpec) {
				NoteSpec noteSpec = (NoteSpec) elem;
				builder.append(SEPARATOR);
				builder.append(TYPE_NOTE_LABEL);
				builder.append(SEPARATOR);
				builder.append(noteSpec.length());
				builder.append(SEPARATOR);
				builder.append(noteSpec.dotExtension());
				builder.append(SEPARATOR);
				builder.append(noteSpec.positon());
				builder.append(SEPARATOR);
				builder.append(enumToString(noteSpec.getToneModifier()));
				builder.append(SEPARATOR);
				builder.append(boolToString(noteSpec.hasJoinArc()));
				builder.append(SEPARATOR);
				builder.append(boolToString(noteSpec.isGrouped()));
			} else if(elem instanceof PauseSpec) {
				PauseSpec pauseSpec = (PauseSpec) elem;
				builder.append(SEPARATOR);
				builder.append(TYPE_PAUSE_LABEL);
				builder.append(SEPARATOR);
				builder.append(pauseSpec.length());
				builder.append(SEPARATOR);
				builder.append(pauseSpec.dotExtension());
			} else if(elem instanceof TimeSpec) {
				TimeSpec timeSpec = (TimeSpec) elem;
				builder.append(SEPARATOR);
				builder.append(TYPE_TIME_LABEL);
				builder.append(SEPARATOR);
				builder.append(timeStepToString(timeSpec.timeStep));
				builder.append(SEPARATOR);
				builder.append(enumToString(timeSpec.getClef()));
				builder.append(SEPARATOR);
				builder.append(enumToString(timeSpec.getKeySignature()));
				appendEnumSet(builder, timeSpec.marks);
			} else {
				throw new SerializationException("Uknown class " + elem.getClass().getName());
			}
		}
		return builder.toString();
	}

	private static void appendEnumSet(StringBuilder builder,
			EnumSet<? extends Enum<?>> set) {
		builder.append(SEPARATOR);
		builder.append(SET_START);
		if(set != null) {
			for(Enum<?> value: set) {
				builder.append(SEPARATOR);
				builder.append(enumToString(value));
			}
		}
		builder.append(SEPARATOR);
		builder.append(SET_END);
	}

	private static String timeStepToString(TimeStep timeStep) {
		if(timeStep == null) {
			return NULL_LABEL;
		} else if(timeStep == TimeStep.commonTime) {
			return COMMON_TIME_LABEL;
		} else if(timeStep == TimeStep.cutCommonTime) {
			return CUT_COMMON_TIME_LABEL;
		} else {
			return timeStep.getBaseMultiplier()+TIMESTEP_CUSTOM_SEPARATOR+timeStep.getTempoBaseLength();
		}
	}

	private static String enumToString(Enum<?> enumValue) {
		if(enumValue == null) {
			return NULL_LABEL;
		} else {
			return enumValue.name();
		}
	}
	
	private static String boolToString(boolean value) {
		return Boolean.toString(value);
	}

	public static List<ScoreContentElem> deserialize(String rawContent)
		throws SerializationException {
		Pattern pattern = Pattern.compile(" ([^ ]+)");
		Matcher matcher = pattern.matcher(rawContent);
		if(!matcher.find()) {
			throw new SerializationException("Raw content doesn't specify version");
		}
		int version = tryReadIntOrThrow(matcher, "content version");
		if(version != CURRENT_VERSION) {
			throw new SerializationException("Raw content in usupported version "+version);
		}
		List<ScoreContentElem> result = new ArrayList<ScoreContentElem>();
		while(matcher.find()) {
			String typeLabel = matcher.group(1);
			if(typeLabel.equals(TYPE_PAUSE_LABEL)) {
				result.add(new PauseSpec(
					tryReadIntOrThrow(matcher, "pause length"),
					tryReadIntOrThrow(matcher, "pause dot extension")
				));
			} else if(typeLabel.equals(TYPE_NOTE_LABEL)) {
				result.add(new NoteSpec(
					tryReadIntOrThrow(matcher, "note length"),
					tryReadIntOrThrow(matcher, "note dot extension"),
					tryReadIntOrThrow(matcher, "note position"),
					tryReadEnumOrThrow(matcher, NoteModifier.class, "note modifier"),
					tryReadBoolOrThrow(matcher, "note.hasJoinArc"),
					tryReadBoolOrThrow(matcher, "note.isGrouped")
				));
			} else if(typeLabel.equals(TYPE_TIME_LABEL)) {
				result.add(new TimeSpec(
					readTimeStep(matcher),
					tryReadEnumOrThrow(matcher, Clef.class, "time clef"),
					tryReadEnumOrThrow(matcher, KeySignature.class, "key signature"),
					readEnumSet(matcher, AdditionalMark.class, "time additional marks")
				));
			} else {
				throw new SerializationException("Uknown type label "+typeLabel);
			}
		}
		return result;
	}
	
	private static <T extends Enum<T>> EnumSet<T> readEnumSet(Matcher matcher, Class<T> enumClass, String metaLabel) throws SerializationException {
		assertFind(matcher, metaLabel + " set start");
		String token = matcher.group(1);
		if(!token.equals(SET_START)) {
			throw new SerializationException(token+" instead of "+metaLabel+" set start");
		}
		EnumSet<T> result = EnumSet.noneOf(enumClass);
		while(true) {
			assertFind(matcher, metaLabel + " element or set end");
			token = matcher.group(1);
			if(token.equals(SET_END)) {
				break;
			} else {
				result.add(parseEnum(enumClass, metaLabel, token));
			}
			
		}
		return result;
	}

	private static TimeStep readTimeStep(Matcher matcher) throws SerializationException {
		assertFind(matcher, "time step");
		String value = matcher.group(1);
		if(value.equals(NULL_LABEL)) {
			return null;
		} else if(value.equals(COMMON_TIME_LABEL)) {
			return TimeStep.commonTime;
		} else if(value.equals(CUT_COMMON_TIME_LABEL)) {
			return TimeStep.cutCommonTime;
		} else {
			int sepPos = value.indexOf(TIMESTEP_CUSTOM_SEPARATOR);
			if(sepPos <= 0 || sepPos >= value.length()-1) {
				throw new SerializationException("Invalid position of separator in custom time step "+value);
			}
			int multi = parseInt(value.substring(0, sepPos), "time step multiplier");
			int base = parseInt(value.substring(sepPos+1), "time step base length");
			if(multi <= 0) {
				throw new SerializationException("Invalid time step multiplier value "+multi);
			} else if(base < 0) {
				throw new SerializationException("Invalid time step base length value "+base);
			}
			return new TimeStep(multi, base);
		}
	}

	private static <T extends Enum<T>> T tryReadEnumOrThrow(Matcher matcher, Class<T> enumClass, String metaLabel) throws SerializationException {
		assertFind(matcher, metaLabel);
		String value = matcher.group(1);
		if(value.equals(NULL_LABEL)) {
			return null;
		} else {
			return parseEnum(enumClass, metaLabel, value);
		}
	}

	private static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String metaLabel,
			String value) throws SerializationException {
		try {
			return Enum.valueOf(enumClass, value);
		} catch(IllegalArgumentException e) {
			throw new SerializationException("Unable to read "+metaLabel, e);
		}
	}
	
	private static boolean tryReadBoolOrThrow(Matcher matcher, String metaLabel) throws SerializationException {
		assertFind(matcher, metaLabel);
		return Boolean.parseBoolean(matcher.group(1));
	}

	private static int tryReadIntOrThrow(Matcher matcher, String metaLabel) throws SerializationException {
		assertFind(matcher, metaLabel);
		return parseInt(matcher.group(1), metaLabel);
	}

	private static int parseInt(String serialized, String metaLabel)
			throws SerializationException {
		try {
			return Integer.valueOf(serialized);
		} catch(NumberFormatException e) {
			throw new SerializationException("Invalid int for "+metaLabel, e);
		}
	}

	private static void assertFind(Matcher matcher, String metaLabel)
			throws SerializationException {
		if(!matcher.find()) {
			throw new SerializationException(metaLabel+" not present");
		}
	}

	public static List<ScoreContentElem> initialContent(Clef clef,
			KeySignature keySignature, TimeStep timeSignature) {
		List<ScoreContentElem> result = new ArrayList<ScoreContentElem>();
		result.add(new TimeSpec(timeSignature, clef, keySignature));
		return result;
	}
}
