package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;

public class JoinArc extends AlignedElementWrapper<SheetAlignedElement> {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils(JoinArc.class);
	private ElementProxy rightElement;
	private Point leftpos = new Point(), rightpos = new Point();
	private Point drawVector = new Point();

	public JoinArc(SheetAlignedElement leftElement) {
		super(leftElement);
	}
	
	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		if(rightElement != null) {
			rightElement.setSheetParams(params);
		}
		recalculate();
	}

	public SheetAlignedElement wrapRightElement(SheetAlignedElement elementToWrap) {
		this.rightElement = new ElementProxy(elementToWrap);
		SheetParams params = getSheetParams();
		if(params != null) {
			rightElement.setSheetParams(params);
			recalculate();
		}
		return this.rightElement;
	}
	
	@Override
	public void positionChanged(int newAbsoluteX, int newAbsoluteY) {
//		log.i("left::positionChanged(%d, %d)", newAbsoluteX, newAbsoluteY);
		super.positionChanged(newAbsoluteX, newAbsoluteY);
		leftpos.set(newAbsoluteX, newAbsoluteY);
		recalculate();
	}

	private void recalculate() {
		if(rightElement == null) {
			calcNoVisibleWrapper();
			return;
		}
		int horizontalDistance = rightpos.x + rightElement.collisionRegionLeft()
			- (leftpos.x + wrappedElement.collisionRegionRight());
		if(horizontalDistance <= 0) {
			calcNoVisibleWrapper();
			return;
		}
		int leftAnchorY = elAnchorYoffset(wrappedElement);
		int rightAnchorY = elAnchorYoffset(rightElement);
		int verticalDistance = (rightpos.y + rightAnchorY) - (leftpos.y + leftAnchorY);
		calcDrawOffsets(-wrappedElement.collisionRegionRight(), -leftAnchorY);
		calcSize(horizontalDistance, Math.abs(verticalDistance));
		drawVector.set(horizontalDistance, verticalDistance);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		super.onDraw(canvas, paint);
		canvas.drawLine(
			wrapperDrawOffset.x,
			wrapperDrawOffset.y,
			wrapperDrawOffset.x + drawVector.x,
			wrapperDrawOffset.y + drawVector.y,
			paint
		);
	}

	private static int elAnchorYoffset(SheetAlignedElement element) {
		return -element.getOffsetToAnchor(
			element.getElementSpec().positonSpec().positon(),
			AnchorPart.MIDDLE
		);
	}
	
	class ElementProxy extends AlignedElementWrapper<SheetAlignedElement> {

		public ElementProxy(SheetAlignedElement wrappedElement) {
			super(wrappedElement);
		}
		
		@Override
		public void setSheetParams(SheetParams params) {
			super.setSheetParams(params);
			calcNoVisibleWrapper();
		}
		
		@Override
		public void positionChanged(int newAbsoluteX, int newAbsoluteY) {
//			log.i("rightWrapper::positionChanged(%d, %d)", newAbsoluteX, newAbsoluteY);
			super.positionChanged(newAbsoluteX, newAbsoluteY);
			JoinArc.this.rightpos.set(newAbsoluteX, newAbsoluteY);
			JoinArc.this.recalculate();
		}
		
	}
}
