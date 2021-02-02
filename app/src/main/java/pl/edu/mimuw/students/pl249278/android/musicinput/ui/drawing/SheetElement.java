package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.IMarker;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Pair;

public abstract class SheetElement {
	
	protected SheetVisualParams sheetParams;

	public abstract int measureWidth();
	public abstract int measureHeight();
	public abstract int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part);
	
	public void setSheetParams(SheetVisualParams params) {
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
	public SheetVisualParams getSheetParams() {
		return sheetParams;
	}
	
	protected void assertParamsPresence() {
		if(sheetParams == null) {
			throw new IllegalStateException();
		}
	}
	
	public abstract void getCollisionRegions(ArrayList<Rect> areas, ArrayList<Rect> rectsPool);
	
	/**
	 * Helper method. See {@link #collectChildRegionsAndOffset(SheetElement, int, int, ArrayList, ArrayList)}.
	 */
	protected static void collectChildRegionsAndOffset(SheetElement child, Point offset, ArrayList<Rect> areas, ArrayList<Rect> rectsPool) {
		collectChildRegionsAndOffset(child, offset.x, offset.y, areas, rectsPool);
	}
	
	/**
	 * Helper method. Collects collision regions from child and translates them by given offset.
	 */
	protected static void collectChildRegionsAndOffset(SheetElement child, int xoffset, int yoffset, ArrayList<Rect> areas, ArrayList<Rect> rectsPool) {
		// collect regions from el and translate by x, y
		int addedIndex = areas.size();
		child.getCollisionRegions(areas, rectsPool);
		for(; addedIndex < areas.size(); addedIndex++) {
			areas.get(addedIndex).offset(xoffset, yoffset);
		}
	}

	/**
	 * Helper method for using Rectangles pool.
	 */
	protected static Rect obtain(ArrayList<Rect> rectsPool) {
		if(rectsPool.isEmpty()) {
			return new Rect();
		} else {
			return rectsPool.remove(rectsPool.size()-1);
		}
	}
}
