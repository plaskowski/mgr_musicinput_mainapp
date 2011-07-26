package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.EnhancedSvgImage.IMarker;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Pair;

public abstract class SheetElement {
	
	protected SheetParams sheetParams;

	public abstract int measureHeight();
	public abstract int measureWidth();
	public abstract int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part);
	
	public void setSheetParams(SheetParams params) {
		this.sheetParams = params;
	}
	
	protected static AnchorPart part(IMarker imarker) {
		if(EnhancedSvgImage.isTypeBottomEdge(imarker))
			return AnchorPart.BOTTOM_EDGE;
		else
			return AnchorPart.MIDDLE;
	}

	protected static int imarkerAnchor(EnhancedSvgImage.IMarker iMarker, int enhImgAnchor) {
		int index = EnhancedSvgImage.alphaToIndex(iMarker.getAlpha());
		if(EnhancedSvgImage.isTypeRelative(iMarker)) {
			return enhImgAnchor + index;
		} else {
			return index;
		}
	}

	protected static float lineXSpan(Pair<PointF, PointF> line) {
		return Math.abs(line.first.x - line.second.x);
	}
	public abstract void onDraw(Canvas canvas, Paint paint);
	public SheetParams getSheetParams() {
		return sheetParams;
	}	

}
