package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINE;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ANCHOR_TYPE_LINESPACE;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;

import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.Key;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteEnding;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteHead;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgParser;
import android.content.Context;
import android.util.Pair;

public class NotePartFactory {
	@SuppressWarnings("serial")
	public static class NoteDescriptionLoadingException extends Exception {
		public NoteDescriptionLoadingException(Exception e, int noteLength, int anchorType, int orientation) {
			super(String.format(
				"Exception occured while loading note (L: %d, anchorT: %s, orient: %s) parts",
				noteLength,
				ReflectionUtils.findConst(NoteConstants.class, "ANCHOR_TYPE_", anchorType),
				ReflectionUtils.findConst(NotePartFactory.class, "ORIENT_", orientation)
			), e);
		}
	}
	@SuppressWarnings("serial")
	public static class LoadingSvgException extends Exception {

		public LoadingSvgException(int xmlResId, Throwable throwable) {
			super(
				"Exception while loading EnhancedSvg from xml: " + ReflectionUtils.findConst(R.xml.class, "", xmlResId),
				throwable
			);
		}
		
	}
	private static final int ORIENT_NORMAL = 0;
	private static final int ORIENT_UPSDOWN = 1;

	public static NoteHead getHeadImage(Context context, int noteLength, int anchorType, boolean isUpsidedown) throws NoteDescriptionLoadingException {
		return prepareHeadImage(context, noteLength, anchorType, isUpsidedown ? ORIENT_UPSDOWN : ORIENT_NORMAL);
	}
	
	private static NoteHead prepareHeadImage(Context context, int noteLength, int anchorType, int orient) throws NoteDescriptionLoadingException {
		int resId = headMapping.get(noteLength)[mappingIndex(orient, anchorType)];
		if(noteHeads.get(resId) == null) {
			SvgParser parser = new SvgParser();
			XmlPullParser xmlParser = context.getResources().getXml(resId);
			SvgImage svgImg;
			try {
				svgImg = parser.parse(xmlParser);
				noteHeads.put(resId, new NoteHead(svgImg, hasEnding(noteLength)));
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
			SvgParser parser = new SvgParser();
			XmlPullParser xmlParser = context.getResources().getXml(resId);
			SvgImage svgImg;
			try {
				svgImg = parser.parse(xmlParser);
				noteEndings.put(resId, new NoteEnding(svgImg));
			} catch (Exception e) {
				throw new NoteDescriptionLoadingException(e, length, anchorType, orientation);
			}
		}
		return noteEndings.get(resId);
	}
	
	public static AdjustableSizeImage prepareModifier(Context context, NoteConstants.NoteModifier modifier, boolean isUpsdown, int anchorType) throws LoadingSvgException {
		int resId = modifiersMapping.get(modifier.ordinal())[mappingIndex(isUpsdown ? ORIENT_UPSDOWN : ORIENT_NORMAL, anchorType)];
		return prepareAdujstableImage(context, resId, true);
	}
	
	public static AdjustableSizeImage prepareKeyImage(Context ctx, Key key) throws LoadingSvgException {
		return prepareAdujstableImage(ctx, keysMapping.get(key), false);
	}
	
	public static AdjustableSizeImage prepareAdujstableImage(Context context, int xmlResId, boolean relativeIMarkers) throws LoadingSvgException {
		if(adjustableImages.get(xmlResId) == null) {
			SvgParser parser = new SvgParser();
			XmlPullParser xmlParser = context.getResources().getXml(xmlResId);
			SvgImage svgImg;
			try {
				svgImg = parser.parse(xmlParser);
				adjustableImages.put(xmlResId, new AdjustableSizeImage(svgImg, relativeIMarkers));
			} catch (Exception e) {
				throw new LoadingSvgException(xmlResId, e);
			}
		}
		return adjustableImages.get(xmlResId);
	}
	
	private static int mappingIndex(int orientation, int anchorType) {
		return (orientation << 1) | anchorType;
	}

	private static Map<Integer, int[]> headMapping = new HashMap<Integer, int[]>();
	private static Map<Integer, int[]> endingMapping = new HashMap<Integer, int[]>();
	private static Map<Integer, int[]> modifiersMapping = new HashMap<Integer, int[]>();
	private static EnumMap<NoteConstants.Key, Integer> keysMapping = new EnumMap<NoteConstants.Key, Integer>(NoteConstants.Key.class);
	private static Map<Integer, NoteHead> noteHeads = new HashMap<Integer, NoteHead>();
	private static Map<Integer, NoteEnding> noteEndings = new HashMap<Integer, NoteEnding>();
	private static Map<Integer, AdjustableSizeImage> adjustableImages = new HashMap<Integer, AdjustableSizeImage>();
	
	static {
		keysMapping.put(NoteConstants.Key.VIOLIN, R.xml.key_violin);
		
		declare(modifiersMapping, NoteConstants.NoteModifier.SHARP,
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.sharp_online),
				updown(R.xml.sharp_online)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.sharp_onspace),
				updown(R.xml.sharp_onspace)
			)
		);
		declare(modifiersMapping, NoteConstants.NoteModifier.DOT,
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.dot_online),
				updown(R.xml.dot_online)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.dot_onspace),
				updown(R.xml.dot_onspace)
			)
		);
		declare(headMapping, 0, 
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.whole),
				updown(R.xml.whole)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.whole),
				updown(R.xml.whole)
			)
		);
		declare(headMapping, 1, 
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.half_online),
				updown(R.xml.half_online_updown)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.half_onspace),
				updown(R.xml.half_onspaceupdown)
			)
		);
		declare(headMapping, lengths(2, 3, 4), 
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.quater_online),
				updown(R.xml.quater_online_updown)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.quater_onspace),
				updown(R.xml.quater_onspace_updown)
			)
		);
		declare(endingMapping, lengths(1, 2),
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.straight_ending),
				updown(R.xml.straight_ending_upsd)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.straight_ending),
				updown(R.xml.straight_ending_upsd)
			)
		);
		declare(endingMapping, lengths(3, 4),
			anchor(ANCHOR_TYPE_LINE,
				normal(R.xml.eight_ending),
				updown(R.xml.eight_ending_updown)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				normal(R.xml.eight_ending),
				updown(R.xml.eight_ending_updown)
			)
		);
	}
	
	// ALL METHODS BELOW SERVE ONLY FOR STATIC DECLARATION PURPOSES
	private static <T extends Enum<T>> void declare(Map<Integer, int[]> lenght2mapping, T value, int[]... partialMappings) {
		declare(lenght2mapping, value.ordinal(), partialMappings);
	}
	private static void declare(Map<Integer, int[]> lenght2mapping, int lenght, int[]... partialMappings) {
		declare(lenght2mapping, new int[] { lenght }, partialMappings);
	}
	private static int[] lengths(int... lengths) {
		return lengths;
	}
	private static void declare(Map<Integer, int[]> lenght2mapping, int[] lenghts, int[]... partialMappings) {
		int[] atypeAndOrientationMapping = new int[4];
		for (int i = 0; i < partialMappings.length; i++) {
			int[] mapping = partialMappings[i];
			for (int j = 0; j < mapping.length; j+=2) {
				atypeAndOrientationMapping[mapping[j]] = mapping[j+1];
			}
		}
		for (int i = 0; i < lenghts.length; i++) {
			lenght2mapping.put(lenghts[i], atypeAndOrientationMapping);
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
	private static Mapping normal(int resId) {
		return new Mapping(ORIENT_NORMAL, resId);
	}
	private static Mapping updown(int resId) {
		return new Mapping(ORIENT_UPSDOWN, resId);
	}
}
