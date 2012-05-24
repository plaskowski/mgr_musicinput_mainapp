package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.Clef;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.DiatonicScalePitch;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class KeySignatureElement extends SheetElement {
	private static final int SCALE_LENGTH = DiatonicScalePitch.values().length;
	/** image[i].top - line0.y */ 
	private int[] relativeYPositions;
	private SimpleSheetElement[] images;
	private int totalWidth;
	/** line0.y - min({for image in this.images: image.top}) */ 
	private int minTop;
	/** max({for image in this.images: image.bottom}) - line0.y */
	private int maxBottom;

	public KeySignatureElement(Context ctx, Clef clef, KeySignature keySignature) throws LoadingSvgException {
		DiatonicScalePitch[] pitches = keySignature.pitches;
		relativeYPositions = new int[pitches.length];
		images = new SimpleSheetElement[pitches.length];
		if(pitches.length <= 0) {
			return;
		}
		// find starting one
		DiatonicScalePitch startPitch = pitches[0];
		// find its position that must be at least LINE0
		int startAnhor = clef.anhorIndex + (clef.diatonicNote.basePitch.ordinal() - startPitch.ordinal());
		startAnhor = limit(startAnhor - SCALE_LENGTH, LINE0_ABSINDEX);
		int minAnhor = startAnhor - (keySignature.modifier == NoteModifier.SHARP ? 1 : 3);
		for(int i = 0; i < pitches.length; i++) {
			DiatonicScalePitch pitch = pitches[i];
			int anhor = startAnhor + (startPitch.ordinal() - pitch.ordinal());
			anhor = limit(anhor - SCALE_LENGTH, minAnhor);
			images[i] = new SimpleSheetElement(
				NotePartFactory.prepareModifier(
					ctx, 
					ElementModifier.map(keySignature.modifier), 
					NoteConstants.ORIENT_UP, 
					anhor
				), 
				anhor
			);
		}
	}
	
	private int limit(int anhorIndex, int minValue) {
		while(anhorIndex < minValue) {
			anhorIndex += SCALE_LENGTH;
		}
		return anhorIndex;
	}

	@Override
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		
		totalWidth = 0;
		minTop = 0;
		maxBottom = 0;
		for (int i = 0; i < images.length; i++) {
			SimpleSheetElement image = images[i];
			image.setSheetParams(params);
			totalWidth += image.measureWidth();
			int offset2line0 = image.getOffsetToAnchor(NoteConstants.LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			relativeYPositions[i] = offset2line0;
			minTop = Math.max(-offset2line0, minTop);
			maxBottom = Math.max(offset2line0 + image.measureHeight(), maxBottom);
		}
	}

	@Override
	public int measureHeight() {
		return minTop+maxBottom;
	}

	@Override
	public int measureWidth() {
		return totalWidth;
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return sheetParams.anchorOffset(anchorAbsIndex, part) - minTop;
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		canvas.translate(0, minTop);
		int totalDx = 0;
		int lazyXTranslate = 0;
		int totalDy = 0;
		for (int i = 0; i < images.length; i++) {
			SimpleSheetElement image = images[i];
			int dy = relativeYPositions[i]-totalDy;
			canvas.translate(lazyXTranslate, dy);
			image.onDraw(canvas, paint);
			totalDy = relativeYPositions[i];
			totalDx += lazyXTranslate;
			lazyXTranslate = image.measureWidth();
		}
		canvas.translate(-totalDx, -minTop-totalDy);
	}
	
	@Override
	public void getCollisionRegions(ArrayList<Rect> areas,
			ArrayList<Rect> rectsPool) {
		int x = 0;
		for (int i = 0; i < images.length; i++) {
			SimpleSheetElement image = images[i];
			int y = minTop + relativeYPositions[i];
			collectChildRegionsAndOffset(image, x, y, areas, rectsPool);
			x += image.measureWidth();
		}
	}

}
