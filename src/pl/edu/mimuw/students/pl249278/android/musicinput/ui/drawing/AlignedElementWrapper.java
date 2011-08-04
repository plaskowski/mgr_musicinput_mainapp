package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public abstract class AlignedElementWrapper<WrappedType extends SheetAlignedElement> extends SheetAlignedElement {
	protected WrappedType wrappedElement;
	protected Point elementDrawOffset = new Point();
	protected Point wrapperDrawOffset = new Point();
	protected int totalWidth, totalHeight;
	
	public AlignedElementWrapper(WrappedType wrappedElement) {
		this.wrappedElement = wrappedElement;
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		wrappedElement.setSheetParams(params);
	}
	
	/**
	 * Helper function, fills elementDrawOffset and wrapperDrawOffset
	 * @param elRelOffsetX wrapped.x - wrappingDrawable.x
	 * @param elRelOffsetY wrapped.y - wrappingDrawable.y
	 */
	protected void calcDrawOffsets(int elRelOffsetX, int elRelOffsetY) {
		elementDrawOffset.set(
			elRelOffsetX >= 0 ? elRelOffsetX : 0,
			elRelOffsetY >= 0 ? elRelOffsetY : 0
		);
		wrapperDrawOffset.set(
			elementDrawOffset.x - elRelOffsetX, 
			elementDrawOffset.y - elRelOffsetY
		);
	}
	
	/** 
	 * Helper function, fills totalWidth, totalHeight
	 * @param wrapperWidth width of wrapper drawing area
	 * @param wrapperHeight height of wrapper drawing area
	 */
	protected void calcSize(int wrapperWidth, int wrapperHeight) {
		totalWidth = Math.max(
			elementDrawOffset.x + wrappedElement.measureWidth(),
			wrapperDrawOffset.x + wrapperWidth
		);
		totalHeight = Math.max(
			elementDrawOffset.y + wrappedElement.measureHeight(),
			wrapperDrawOffset.y + wrapperHeight
		);
	}
	
	/**
	 * Simple "macro" for Wrapper that doesn't draw any content
	 */
	protected final void calcNoVisibleWrapper() {
		calcDrawOffsets(0, 0);
		calcSize(0, 0);
	}

	@Override
	public int getMiddleX() {
		return elementDrawOffset.x + wrappedElement.getMiddleX();
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
		return wrappedElement.getOffsetToAnchor(anchorAbsIndex, part)
		- elementDrawOffset.y;
	}
	
	@Override
	public ElementSpec getElementSpec() {
		return wrappedElement.getElementSpec();
	}
	
	/**
	 * Draws wrappedElement onto appropriate place (elementDrawOffset).
	 * Preserve canvas state (matrix, clip).
	 */
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		canvas.translate(elementDrawOffset.x, elementDrawOffset.y);
		wrappedElement.onDraw(canvas, paint);
		canvas.translate(-elementDrawOffset.x, -elementDrawOffset.y);
	}
	
	@Override
	public void positionChanged(int newAbsoluteX, int newAbsoluteY) {
		wrappedElement.positionChanged(elementDrawOffset.x+newAbsoluteX, elementDrawOffset.y+newAbsoluteY);
	}
}