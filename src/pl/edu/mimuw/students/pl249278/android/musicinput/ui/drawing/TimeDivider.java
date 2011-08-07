package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE4_ABSINDEX;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TimeDivider extends SheetAlignedElement {
	private static final int EL_SPACING = 2;
	private ElementSpec.TimeDivider spec;
	private List<SheetElement> rightParts = null;
	private int line0Yoffset, totalWidth, totalHeight;
	
	public TimeDivider(Context ctx, TimeSpec leftTime, TimeSpec rightTime) throws LoadingSvgException {
		spec = new ElementSpec.TimeDivider(leftTime, rightTime);
		if(rightTime != null) {
			rightParts = new ArrayList<SheetElement>(3);
			if(rightTime.getClef() != null) {
				rightParts.add(new SimpleSheetElement(
					NotePartFactory.prepareClefImage(ctx, rightTime.getClef())
				));
			}
			if(rightTime.getKeySignature() != null) {
				rightParts.add(new KeySignatureElement(ctx, rightTime.getKeySignature()));
			}
			TimeStep rTS = rightTime.getTimeStep();
			if(rTS != null) {
				rightParts.add(new Tempo(rTS.getBaseMultiplier(), 1 << rTS.getTempoBaseLength()));
			}
		}
	}
	
	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		recalculate();
	}
	
	private void recalculate() {
		line0Yoffset = 0;
		totalWidth = sheetParams.getLineThickness();
		totalHeight = sheetParams.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE);
		int elementsSpacing = EL_SPACING*sheetParams.getLineThickness();
		if(rightParts != null) for(SheetElement el: rightParts) {
			el.setSheetParams(sheetParams);
			totalWidth += elementsSpacing+el.measureWidth();
			int off = el.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			if(-off > line0Yoffset) {
				totalHeight = Math.max(-off-line0Yoffset+totalHeight, el.measureHeight());
				line0Yoffset = -off;
			} else {
				totalHeight = Math.max(totalHeight, line0Yoffset+off+el.measureHeight());
			}
		}
	}

	@Override
	public int measureHeight() {
		return totalHeight;
	}

	@Override
	public int measureWidth() {
		return totalWidth;
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return sheetParams.anchorOffset(anchorAbsIndex, part) - line0Yoffset;
	}

	@Override
	public void onDraw(Canvas canvas, Paint paint) {
//		debugElementDrawArea(canvas, this);
		canvas.translate(0, line0Yoffset);
		int lineThickness = sheetParams.getLineThickness();
		canvas.drawRect(
			0, 0,
			lineThickness, sheetParams.anchorOffset(LINE4_ABSINDEX, AnchorPart.BOTTOM_EDGE),
			paint
		);
		int totalDX = 0;
		int dy2line0 = 0;
		int xLazyShift = lineThickness;
		if(rightParts != null) for(SheetElement el: rightParts) {
			int eldY = el.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			int dx = xLazyShift+EL_SPACING*lineThickness;
			canvas.translate(dx, eldY-dy2line0);
//			debugElementDrawArea(canvas, el);
			el.onDraw(canvas, paint);
			dy2line0 = eldY;
			totalDX += dx;
			xLazyShift = el.measureWidth();
		}
		canvas.translate(-totalDX, -(line0Yoffset+dy2line0));
	}

	@SuppressWarnings("unused")
	private static void debugElementDrawArea(Canvas canvas, SheetElement el) {
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setAlpha(100);
		canvas.drawRect(
			0, 0,
			el.measureWidth(),
			el.measureHeight(),
			paint
		);
	}

	@Override
	public int getHorizontalOffset(int lineIdentifier) {
		if(lineIdentifier == SheetAlignedElement.MIDDLE_X) {
			return sheetParams.getLineThickness()/2;
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public ElementSpec getElementSpec() {
		return spec;
	}

	@Override
	public int getVerticalOffset(int lineIdentifier) {
		throw new UnsupportedOperationException();
	}
	
}
