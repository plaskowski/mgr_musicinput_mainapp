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
				ReflectionUtils.findConstName(NoteConstants.class, "ANCHOR_TYPE_", anchorType),
				ReflectionUtils.findConstName(NotePartFactory.class, "ORIENT_", orientation)
			), e);
		}
	}
	@SuppressWarnings("serial")
	public static class LoadingSvgException extends Exception {

		public LoadingSvgException(int xmlResId, Throwable throwable) {
			super(
				"Exception while loading EnhancedSvg from xml: " + ReflectionUtils.findConstName(R.xml.class, "", xmlResId),
				throwable
			);
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
	
	public static AdjustableSizeImage prepareModifier(Context context, NoteConstants.NoteModifier modifier, int orientation, int positon) throws LoadingSvgException {
		int anchorType = NoteConstants.anchorType(positon);
		int resId = modifiersMapping.get(modifier.ordinal())[mappingIndex(orientation, anchorType)];
		return prepareAdujstableImage(context, resId, true);
	}
	
	public static AdjustableSizeImage prepareClefImage(Context ctx, Clef key) throws LoadingSvgException {
		return prepareAdujstableImage(ctx, clefMapping.get(key), false);
	}
	
	public static AdjustableSizeImage preparePauseImage(Context ctx, int pauseLength) throws LoadingSvgException {
		return prepareAdujstableImage(ctx, pauseMapping.get(pauseLength), false);
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
	
	public static SvgImage prepareSvgImage(Context context, int svgResId) throws LoadingSvgException {
		if(svgImages.get(svgResId) == null) {
			SvgParser parser = new SvgParser();
			XmlPullParser xmlParser = context.getResources().getXml(svgResId);
			SvgImage svgImg;
			try {
				svgImg = parser.parse(xmlParser);
				svgImages.put(svgResId, new EnhancedSvgImage(svgImg));
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
	private static Map<Integer, AdjustableSizeImage> adjustableImages = new HashMap<Integer, AdjustableSizeImage>();
	private static Map<Integer, SvgImage> svgImages = new HashMap<Integer, SvgImage>();
	
	static {
		clefMapping.put(NoteConstants.Clef.VIOLIN, R.xml.key_violin);
		
		pauseMapping.put(NoteConstants.LEN_FULLNOTE, R.xml.pause_whole);
		pauseMapping.put(NoteConstants.LEN_HALFNOTE, R.xml.pause_half);
		pauseMapping.put(NoteConstants.LEN_QUATERNOTE, R.xml.pause_quater);
		pauseMapping.put(NoteConstants.LEN_EIGHTNOTE, R.xml.pause_eight);
		pauseMapping.put(NoteConstants.LEN_SIXTEENNOTE, R.xml.pause_sixteen);
		pauseMapping.put(NoteConstants.LEN_SIXTEENNOTE+1, R.xml.pause_32);
		
		declare(modifiersMapping, NoteConstants.NoteModifier.SHARP,
			anchor(ANCHOR_TYPE_LINE,
				anyOrient(R.xml.sharp_online)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				anyOrient(R.xml.sharp_onspace)
			)
		);
		declare(modifiersMapping, NoteConstants.NoteModifier.FLAT,
			anyAnchor(
				anyOrient(R.xml.flat)
			)
		);
		declare(modifiersMapping, NoteConstants.NoteModifier.DOT,
			anchor(ANCHOR_TYPE_LINE,
				anyOrient(R.xml.dot_online)
			),
			anchor(ANCHOR_TYPE_LINESPACE,
				anyOrient(R.xml.dot_onspace)
			)
		);
		declare(headMapping, 0, 
			anyAnchor(
				anyOrient(R.xml.whole)
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
			anyAnchor(
				normal(R.xml.straight_ending),
				updown(R.xml.straight_ending_upsd)
			)
		);
		declare(endingMapping, 3,
			anyAnchor(
				normal(R.xml.eight_ending),
				updown(R.xml.eight_ending_updown)
			)
		);
		declare(endingMapping, 4,
			anyAnchor(
				normal(R.xml.sixteen_ending),
				updown(R.xml.sixteen_ending_updown)
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
