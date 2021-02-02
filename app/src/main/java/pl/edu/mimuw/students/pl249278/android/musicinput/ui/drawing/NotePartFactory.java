package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ANCHOR_TYPE_LINESPACE;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteEnding;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteHead;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgInflater;
import pl.edu.mimuw.students.pl249278.android.svg.SvgParser;
import android.content.Context;
import android.content.res.Resources;
import android.util.Pair;
import android.util.SparseArray;

public class NotePartFactory {
	@SuppressWarnings("serial")
	public static class NoteDescriptionLoadingException extends Exception {
		public NoteDescriptionLoadingException(Exception e, int noteLength, int anchorType, int orientation) {
			super(String.format(
				"Exception occured while loading note (L: %d, anchorT: %s, orient: %s) parts",
				noteLength,
				ReflectionUtils.findConstName(NoteConstants.class, "ANCHOR_TYPE_", anchorType),
				ReflectionUtils.findConstName(NotePartFactory.class, "ORIENT_", orientation)
			), e);
		}
	}
	@SuppressWarnings("serial")
	public static class LoadingSvgException extends Exception {

		public LoadingSvgException(int xmlResId, Throwable throwable) {
			super(
				message(xmlResId),
				throwable
			);
		}

		private static String message(int xmlResId) {
			String constName = ReflectionUtils.findConstName(R.array.class, "svg", xmlResId, null);
			String type = "array";
			if(constName == null) {
				constName = ReflectionUtils.findConstName(R.xml.class, "", xmlResId);
				type = "xml";
			}
			return "Exception while loading EnhancedSvg from " + type + ": " + constName;
		}
		
	}
	private static final int ORIENT_NORMAL = NoteConstants.ORIENT_UP;
	private static final int ORIENT_UPSDOWN = NoteConstants.ORIENT_DOWN;

	public static NoteHead getHeadImage(Context context, int noteLength, int anchorType, boolean isUpsidedown) throws NoteDescriptionLoadingException {
		return prepareHeadImage(context, noteLength, anchorType, isUpsidedown ? ORIENT_UPSDOWN : ORIENT_NORMAL);
	}
	
	private static NoteHead prepareHeadImage(Context context, int noteLength, int anchorType, int orient) throws NoteDescriptionLoadingException {
		int resId = headMapping.get(noteLength)[mappingIndex(orient, anchorType)];
		if(noteHeads.get(resId) == null) {
			try {
				noteHeads.put(resId, new NoteHead(parseSvgImage(context.getResources(), resId), hasEnding(noteLength)));
			} catch (Exception e) {
				throw new NoteDescriptionLoadingException(e, noteLength, anchorType, orient);
			}
		}
		return noteHeads.get(resId);
	}
	
	private static boolean hasEnding(int noteLength) {
		return NoteConstants.hasStem(noteLength);
	}

	/**
	 * @return can return null if note consists of head only
	 * @throws NoteDescriptionLoadingException 
	 */
	public static NoteEnding getEndingImage(Context context, int noteLength, int anchorType, boolean isUpsidedown) throws NoteDescriptionLoadingException {
		if(!hasEnding(noteLength)) return null;
		return prepareEnding(context, noteLength, isUpsidedown ? ORIENT_UPSDOWN : ORIENT_NORMAL, anchorType);
	}
	
	private static NoteEnding prepareEnding(Context context, int length, int orientation, int anchorType) throws NoteDescriptionLoadingException {
		int resId = endingMapping.get(length)[mappingIndex(orientation, anchorType)];
		if(noteEndings.get(resId) == null) {
			try {
				noteEndings.put(resId, new NoteEnding(parseSvgImage(context.getResources(), resId)));
			} catch (Exception e) {
				throw new NoteDescriptionLoadingException(e, length, anchorType, orientation);
			}
		}
		return noteEndings.get(resId);
	}
	
	public static AdjustableSizeImage prepareModifier(Context context, ElementModifier modifier, int orientation, int positon) throws LoadingSvgException {
		int anchorType = NoteConstants.anchorType(positon);
		int resId = modifiersMapping.get(modifier.ordinal())[mappingIndex(orientation, anchorType)];
		return prepareAdujstableImage(context, resId, true);
	}
	
	public static AdjustableSizeImage prepareClefImage(Context ctx, Clef key) throws LoadingSvgException {
		return prepareAdujstableImage(ctx, clefMapping.get(key), null);
	}
	
	public static AdjustableSizeImage preparePauseImage(Context ctx, int pauseLength) throws LoadingSvgException {
		return prepareAdujstableImage(ctx, pauseMapping.get(pauseLength), false);
	}
	
	public static AdjustableSizeImage prepareAdujstableImage(Context context, int xmlResId, Boolean relativeIMarkers) throws LoadingSvgException {
		if(adjustableImages.get(xmlResId) == null) {
			try {
				adjustableImages.put(xmlResId, new AdjustableSizeImage(parseSvgImage(context.getResources(), xmlResId), relativeIMarkers));
			} catch (Exception e) {
				throw new LoadingSvgException(xmlResId, e);
			}
		}
		return adjustableImages.get(xmlResId);
	}
	
	private static SvgImage parseSvgImage(Resources res, int resId) throws LoadingSvgException {
		if(res.getResourceTypeName(resId).equals(res.getResourceTypeName(pl.edu.mimuw.students.pl249278.android.svg.R.array.svggen_dummy_arr))) {
			SvgInflater inflater = new SvgInflater();
			return inflater.inflate(res, resId);
		}
		return parseSvgImageFromXml(res, resId);
	}

	private static SvgImage parseSvgImageFromXml(Resources res, int xmlResId)
			throws LoadingSvgException {
		SvgParser parser = new SvgParser();
		XmlPullParser xmlParser = res.getXml(xmlResId);
		try {
			return parser.parse(xmlParser);
		} catch (Exception e) {
			throw new LoadingSvgException(xmlResId, e);
		}
	}
	
	public static SvgImage prepareSvgImage(Context context, int svgResId) throws LoadingSvgException {
		return prepareSvgImage(context.getResources(), svgResId);
	}
	public static SvgImage prepareSvgImage(Resources resources, int svgResId) throws LoadingSvgException {
		if(svgImages.get(svgResId) == null) {
			try {
				svgImages.put(svgResId, new EnhancedSvgImage(parseSvgImage(resources, svgResId)));
			} catch (Exception e) {
				throw new LoadingSvgException(svgResId, e);
			}
		}
		return svgImages.get(svgResId);
	}
	
	private static int mappingIndex(int orientation, int anchorType) {
		return (orientation << 1) | anchorType;
	}

	private static Map<Integer, int[]> headMapping = new HashMap<Integer, int[]>();
	private static Map<Integer, int[]> endingMapping = new HashMap<Integer, int[]>();
	private static Map<Integer, int[]> modifiersMapping = new HashMap<Integer, int[]>();
	private static EnumMap<NoteConstants.Clef, Integer> clefMapping = new EnumMap<NoteConstants.Clef, Integer>(NoteConstants.Clef.class);
	private static Map<Integer, Integer> pauseMapping = new HashMap<Integer, Integer>();
	private static Map<Integer, NoteHead> noteHeads = new HashMap<Integer, NoteHead>();
	private static Map<Integer, NoteEnding> noteEndings = new HashMap<Integer, NoteEnding>();
	private static SparseArray<AdjustableSizeImage> adjustableImages = new SparseArray<AdjustableSizeImage>();
	private static SparseArray<SvgImage> svgImages = new SparseArray<SvgImage>();
	
	static {
		clefMapping.put(NoteConstants.Clef.VIOLIN, R.array.svg_key_violin);
		clefMapping.put(NoteConstants.Clef.ALTO, R.array.svg_clef_alto);
		clefMapping.put(NoteConstants.Clef.BASS, R.array.svg_clef_bass);
		
		pauseMapping.put(NoteConstants.LEN_FULLNOTE, R.array.svg_pause_whole);
		pauseMapping.put(NoteConstants.LEN_HALFNOTE, R.array.svg_pause_half);
		pauseMapping.put(NoteConstants.LEN_QUATERNOTE, R.array.svg_pause_quater);
		pauseMapping.put(NoteConstants.LEN_EIGHTNOTE, R.array.svg_pause_eight);
		pauseMapping.put(NoteConstants.LEN_SIXTEENNOTE, R.array.svg_pause_sixteen);
		pauseMapping.put(NoteConstants.LEN_SIXTEENNOTE+1, R.array.svg_pause_32);
		
		declare(modifiersMapping, ElementModifier.SHARP,
			anchor(ANCHOR_TYPE_LINE,
				anyOrient(R.array.svg_sharp_onspace)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				anyOrient(R.array.svg_sharp_onspace)
			)
		);
		declare(modifiersMapping, ElementModifier.FLAT,
			anyAnchor(
				anyOrient(R.array.svg_flat)
			)
		);
		declare(modifiersMapping, ElementModifier.DOT,
			anchor(ANCHOR_TYPE_LINE,
				anyOrient(R.array.svg_dot_online)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				anyOrient(R.array.svg_dot_onspace)
			)
		);
		declare(modifiersMapping, ElementModifier.NATURAL,
			anyAnchor(
				anyOrient(R.array.svg_natural_online)
			)
		);
		declare(headMapping, 0, 
			anyAnchor(
				anyOrient(R.array.svg_whole)
			)
		);
		declare(headMapping, 1, 
			anchor(ANCHOR_TYPE_LINE,
				normal(R.array.svg_half_online),
				updown(R.array.svg_half_online_updown)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.array.svg_half_onspace),
				updown(R.array.svg_half_onspaceupdown)
			)
		);
		declare(headMapping, lengths(2, 3, 4), 
			anchor(ANCHOR_TYPE_LINE,
				normal(R.array.svg_quater_online),
				updown(R.array.svg_quater_online_updown)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.array.svg_quater_onspace),
				updown(R.array.svg_quater_onspace_updown)
			)
		);
		declare(endingMapping, lengths(1, 2),
			anyAnchor(
				normal(R.array.svg_straight_ending),
				updown(R.array.svg_straight_ending_upsd)
			)
		);
		declare(endingMapping, 3,
			anyAnchor(
				normal(R.array.svg_eight_ending),
				updown(R.array.svg_eight_ending_updown)
			)
		);
		declare(endingMapping, 4,
			anyAnchor(
				normal(R.array.svg_sixteen_ending),
				updown(R.array.svg_sixteen_ending_updown)
			)
		);
	}
	
	// ALL METHODS BELOW SERVE ONLY FOR STATIC DECLARATION PURPOSES
	private static <T extends Enum<T>> void declare(Map<Integer, int[]> length2mapping, T value, int[]... partialMappings) {
		declare(length2mapping, value.ordinal(), partialMappings);
	}

	private static void declare(Map<Integer, int[]> length2mapping, int length, int[]... partialMappings) {
		declare(length2mapping, new int[] { length }, partialMappings);
	}
	private static int[] lengths(int... lengths) {
		return lengths;
	}
	private static void declare(Map<Integer, int[]> length2mapping, int[] lengths, int[]... partialMappings) {
		int[] atypeAndOrientationMapping = new int[4];
		for (int i = 0; i < partialMappings.length; i++) {
			int[] mapping = partialMappings[i];
			for (int j = 0; j < mapping.length; j+=2) {
				atypeAndOrientationMapping[mapping[j]] = mapping[j+1];
			}
		}
		for (int i = 0; i < lengths.length; i++) {
			length2mapping.put(lengths[i], atypeAndOrientationMapping);
		}
	}
	private static class Mapping extends Pair<Integer, Integer> {
		public Mapping(Integer first, Integer second) {
			super(first, second);
		}
		
	}
	private static int[] anchor(int anchorType, Mapping... orientation2Id) {
		int[] result = new int[orientation2Id.length*2];
		for (int i = 0; i < orientation2Id.length; i++) {
			result[i*2] = mappingIndex(orientation2Id[i].first, anchorType);
			result[i*2+1] = orientation2Id[i].second;
		}
		return result;
	}
	private static int[] anyAnchor(Mapping... orientation2Id) {
		int[] first = anchor(ORIENT_NORMAL, orientation2Id);
		int[] second = anchor(ORIENT_UPSDOWN, orientation2Id);
		int[] result = new int[first.length+second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	private static Mapping normal(int resId) {
		return new Mapping(ORIENT_NORMAL, resId);
	}
	private static Mapping updown(int resId) {
		return new Mapping(ORIENT_UPSDOWN, resId);
	}
	private static Mapping[] anyOrient(int resId) {
		return new Mapping[] {
			normal(resId),
			updown(resId)
		};
	}
}
