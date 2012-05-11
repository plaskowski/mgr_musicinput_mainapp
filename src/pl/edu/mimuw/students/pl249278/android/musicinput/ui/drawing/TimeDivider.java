package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;

import java.util.ArrayList;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.TimeStep;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.InvalidMetaException;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class TimeDivider extends SheetAlignedElement {
	private static final int EL_SPACING = 2;
	private ElementSpec.TimeDivider spec;
	private List<SheetElement> rightParts = null;
	private int line0Yoffset, totalWidth, totalHeight;
	
	public TimeDivider(Context ctx, pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.TimeDivider elementSpec) throws LoadingSvgException {
		this.spec = elementSpec;
		rightParts = new ArrayList<SheetElement>(4);
		
		TimeSpec rightTime = this.spec.rightTime, leftTime = this.spec.leftTime;
		boolean endRepeat = leftTime != null && leftTime.hasMark(AdditionalMark.END_REPEAT);
		boolean beginRepeat = rightTime != null && rightTime.hasMark(AdditionalMark.BEGIN_REPEAT);
		int xmlId;
		if(beginRepeat && endRepeat) {
			xmlId = R.xml.timebar_both_repeat;
		} else if(beginRepeat) {
			xmlId = R.xml.timebar_begin_repeat;
		} else if(endRepeat) {
			xmlId = R.xml.timebar_end_repeat;
		} else {
			xmlId = R.xml.timebar_single;
		}
		try {
			rightParts.add(new SheetAlignedImage(NotePartFactory.prepareAdujstableImage(
				ctx, xmlId, 
				true
			)));
		} catch (InvalidMetaException e) {
			throw new NotePartFactory.LoadingSvgException(xmlId, e);
		}
		
		if(rightTime != null) {
			if(rightTime.getClef() != null) {
				rightParts.add(new SimpleSheetElement(
					NotePartFactory.prepareClefImage(ctx, rightTime.getClef())
				));
			}
			if(rightTime.getKeySignature() != null) {
				rightParts.add(new KeySignatureElement(ctx, rightTime.getClef(), rightTime.getKeySignature()));
			}
			TimeStep rTS = rightTime.getTimeStep();
			if(rTS != null) {
				SheetElement metrum;
				if(rTS == TimeStep.commonTime) {
					metrum = new SimpleSheetElement(
						NotePartFactory.prepareAdujstableImage(
							ctx, 
							R.xml.timesignature_commontime, 
							false
					));
				} else if(rTS == TimeStep.cutCommonTime) {
					metrum = new SimpleSheetElement(
						NotePartFactory.prepareAdujstableImage(
							ctx, 
							R.xml.timesignature_cutcommontime, 
							false
					));
				} else {
					metrum = new Tempo(rTS.getBaseMultiplier(), 1 << rTS.getTempoBaseLength());
				}
				rightParts.add(metrum);
			}
		}
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		recalculate();
	}
	
	private void recalculate() {
		line0Yoffset = 0;
		int elementsSpacing = EL_SPACING*sheetParams.getLineThickness();
		totalWidth = -elementsSpacing;
		totalHeight = 0;
		for(SheetElement el: rightParts) {
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
		int spacing = EL_SPACING*lineThickness;
		int totalDX = 0;
		int dy2line0 = 0;
		int xLazyShift = -spacing;
		for(SheetElement el: rightParts) {
			int eldY = el.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
			int dx = xLazyShift+spacing;
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
			return (int) ((SheetAlignedImage) rightParts.get(0)).getxMiddleMarker();
		} else {
			return super.getHorizontalOffset(lineIdentifier);
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
