package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ORIENT_UP;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NoteHeadElement.*;

import java.security.InvalidParameterException;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;

public class JoinArc extends ElementsOverlay {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils(JoinArc.class);
	
	private SheetAlignedElement leftElement, rightElement;
	private Point leftpos = new Point(), rightpos = new Point();

	private Path path;

	public JoinArc(SheetAlignedElement leftElement) {
		this.leftElement = leftElement;
	}
	
	public void setRightElement(SheetAlignedElement rightElement) {
		this.rightElement = rightElement;
		SheetParams params = getSheetParams();
		if(params != null) {
			rightElement.setSheetParams(params);
			recalculate();
		}
	}
	
	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		leftElement.setSheetParams(params);
		if(rightElement != null) {
			rightElement.setSheetParams(params);
		}
		recalculate();
	}
	
	@Override
	public void positionChanged(SheetAlignedElement element, int newX, int newY) {
		if(leftElement == element) {
			leftpos.set(newX, newY);
		} else if(rightElement == element) {
			rightpos.set(newX, newY);
		} else {
			throw new InvalidParameterException();
		}
		recalculate();
	}

	private void recalculate() {
		if(rightElement == null) {
			makeEmpty();
			return;
		}
		
		int indent = sheetParams.getLinespacingThickness()/2;
		int drawSpacing = sheetParams.getLineThickness()/2;
		int leftOrient = ((ElementSpec.NormalNote) leftElement.getElementSpec()).getOrientation();
		int rightOrient = ((ElementSpec.NormalNote) rightElement.getElementSpec()).getOrientation();
		int sign = leftOrient == ORIENT_UP ? 1 : -1;
		
		int absLeft = leftpos.x;
		int absRight = rightpos.x;
		int extrLineId = leftOrient == ORIENT_UP ? AREA_NOTEHEAD_BOTTOM : AREA_NOTEHEAD_TOP;
		int leftExtr = leftpos.y + leftElement.getVerticalOffset(extrLineId) + sign*drawSpacing;
		int absTop = leftExtr;
		if(leftOrient == rightOrient) {
			int leftAnchorY = leftpos.y + elAnchorYoffset(leftElement);
			int rightExtr = rightpos.y + rightElement.getVerticalOffset(extrLineId) + sign*drawSpacing;
			if(sign*rightExtr <= leftAnchorY*sign) {
				absLeft += leftElement.getHorizontalOffset(AREA_NOTEHEAD_RIGHT) + drawSpacing;
				absTop = leftAnchorY;
				absRight += rightElement.getMiddleX();
			} else if(rightExtr*sign <= leftExtr*sign) {
				absLeft += leftElement.getHorizontalOffset(AREA_NOTEHEAD_RIGHT) - indent;
				absRight += rightElement.getHorizontalOffset(AREA_NOTEHEAD_LEFT) + indent;
			} else {
				absLeft += leftElement.getMiddleX();
				absRight += rightElement.getHorizontalOffset(AREA_NOTEHEAD_LEFT) - drawSpacing;
			}
		} else {
			absLeft += leftElement.getMiddleX();
			absRight += rightElement.collisionRegionLeft() - drawSpacing;
		}
		
		int horizontalDistance = absRight - absLeft;
		if(horizontalDistance <= 0) {
			makeEmpty();
			return;
		}
		
		int arcThickness = 2*sheetParams.getLineThickness();
		int arcBoundsHeight = arcThickness+6*sheetParams.getLineThickness();
		path = new Path();
		path.rMoveTo(0, arcBoundsHeight);
		path.rQuadTo(horizontalDistance/2, sign*arcBoundsHeight, horizontalDistance, 0);
		path.rQuadTo(-horizontalDistance/2, sign*(arcBoundsHeight-arcThickness), -horizontalDistance, 0);
//		path.close();
		
		setPosition(absLeft, absTop-arcBoundsHeight);
		setMeasured(horizontalDistance, 2*arcBoundsHeight);
		onMeasureInvalidated();
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		Style style = paint.getStyle();
		paint.setStyle(Style.FILL);
		canvas.drawPath(path, paint);
		paint.setStyle(style);
	}

	private static int elAnchorYoffset(SheetAlignedElement element) {
		return -element.getOffsetToAnchor(
			element.getElementSpec().positonSpec().positon(),
			AnchorPart.MIDDLE
		);
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		throw new UnsupportedOperationException();
	}
	
	public static boolean canStrartJA(ElementSpec elementSpec) {
		return elementSpec.getType() == ElementType.NOTE && ((NormalNote) elementSpec).noteSpec().hasJoinArc();
	}
	public static boolean canEndJA(ElementSpec elementSpec) {
		return elementSpec.getType() == ElementType.NOTE;
	}
	public static boolean canSkipOver(ElementSpec elementSpec) {
		return elementSpec.getType() == ElementType.TIMES_DIVIDER || elementSpec.getType() == ElementType.FAKE_PAUSE;
	}

	@Override
	public int elementsCount() {
		return 2;
	}
	
	@Override
	public SheetAlignedElement getElement(int elementIndex) {
		switch(elementIndex) {
		case 0:
			return leftElement;
		case 1:
			return rightElement;
		default:
			throw new IllegalArgumentException();
		}
	}
}
