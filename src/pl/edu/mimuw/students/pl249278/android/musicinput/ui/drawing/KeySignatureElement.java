package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.KeySignature;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.KeySignature.Accidental;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class KeySignatureElement extends SheetElement {
	/** image[i].top - line0.y */ 
	private int[] relativeYPositions;
	private SimpleSheetElement[] images;
	private int totalWidth;
	/** line0.y - min({for image in this.images: image.top}) */ 
	private int minTop;
	/** max({for image in this.images: image.bottom}) - line0.y */
	private int maxBottom;

	public KeySignatureElement(Context ctx, KeySignature keySignature) throws LoadingSvgException {
		Accidental[] accidentals = keySignature.accidentals;
		relativeYPositions = new int[accidentals.length];
		images = new SimpleSheetElement[accidentals.length];
		
		for (int i = 0; i < accidentals.length; i++) {
			Accidental accidental = accidentals[i];
			images[i] = new SimpleSheetElement(
				NotePartFactory.prepareModifier(
					ctx, 
					accidental.accidental, 
					false, 
					NoteConstants.anchorType(accidental.anchor)
				), 
				accidental.anchor
			);
		}
	}
	
	@Override
	public void setSheetParams(SheetParams params) {
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

}
