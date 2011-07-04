package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import org.xmlpull.v1.XmlPullParser;

import pl.edu.mimuw.students.pl249278.android.common.ReflectionUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgParser;
import android.content.Context;

public class NotePartFactory {
	@SuppressWarnings("serial")
	public static class NoteDescriptionLoadingException extends Exception {
		public NoteDescriptionLoadingException(Exception e, int noteLength, int anchorType, boolean isUpdown) {
			super(String.format(
				"Exception occured while loading note (L: %d, anchorT: %s, upsd: %s) parts",
				noteLength,
				ReflectionUtils.findConst(NoteConstants.class, "ANCHOR_TYPE_", anchorType),
				Boolean.toString(isUpdown)
			), e);
		}
	}

	public static NoteBase getBaseImage(Context context, int noteLength, int anchorType, boolean isUpsidedown) throws NoteDescriptionLoadingException {
		// TODO real implementation
		SvgParser parser = new SvgParser();
		int resId = isUpsidedown ? R.xml.test_polnota_upsd_laprawo : R.xml.test_obrazek;
		if(noteLength == 0) { resId = R.xml.test_calanuta; }
		XmlPullParser xmlParser = context.getResources().getXml(resId);
		SvgImage svgImg;
		try {
			svgImg = parser.parse(xmlParser);
			return new NoteBase(svgImg, noteLength != 0);
		} catch (Exception e) {
			throw new NoteDescriptionLoadingException(e, noteLength, anchorType, isUpsidedown);
		}
	}

	/**
	 * @return can return null if note consists of base only
	 * @throws NoteDescriptionLoadingException 
	 */
	public static NoteEnding getEndingImage(Context context, int noteLength, int anchorType, boolean isUpsidedown) throws NoteDescriptionLoadingException {
		if(noteLength == 0) return null;
		// TODO real implementation
		SvgParser parser = new SvgParser();
		int resId = isUpsidedown ? R.xml.test_8ending_upsd_lalewo : R.xml.test_8ending;
		XmlPullParser xmlParser = context.getResources().getXml(resId);
		SvgImage svgImg;
		try {
			svgImg = parser.parse(xmlParser);
			return new NoteEnding(svgImg);
		} catch (Exception e) {
			throw new NoteDescriptionLoadingException(e, noteLength, anchorType, isUpsidedown);
		}
	}

}
