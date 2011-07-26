package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.EnhancedSvgImage.InvalidMetaException;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class SimpleSheetElement extends SheetElement {

	private float scale;
	private EnhancedSvgImage image;
	
	private int IM1Anchor;
	private int IM2Anchor;

	public void setImage(EnhancedSvgImage img) throws InvalidMetaException {
		this.image = img;
		// we require 2 i-markers so we can scale svg image appropriately
		if(image.getImarkers().size() != 2) {
			throw new EnhancedSvgImage.InvalidMetaException("Expected exaclty 2 imarkers, found "+image.getImarkers().size());
		}
    	IMarker firstM = image.getImarkers().get(0), secondM = image.getImarkers().get(1);
    	// imarkers must reffer to absolute indexes of anchors
    	if(EnhancedSvgImage.isTypeRelative(firstM)) {
    		throw new EnhancedSvgImage.InvalidMetaException("First imarker is relative-type, but static element doesn't reffer to any height");
    	} else if(EnhancedSvgImage.isTypeRelative(secondM)) {
    		throw new EnhancedSvgImage.InvalidMetaException("Second imarker is relative-type, but static element doesn't reffer to any height");
    	}
		
		// discover appropriate parts images
		IM1Anchor = imarkerAnchor(firstM, 0);
		IM2Anchor = imarkerAnchor(secondM, 0);
		
		if(sheetParams != null) {
			sheetParamsCalculations();
		}
	}
	
	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
	}

	private void sheetParamsCalculations() {
		IMarker firstM = image.getImarkers().get(0), secondM = image.getImarkers().get(1);
		int baseIM1RelativeOffset = sheetParams.anchorOffset(IM1Anchor, part(firstM));
    	int baseIM2RelativeOffset = sheetParams.anchorOffset(IM2Anchor, part(secondM));
    	scale = (baseIM1RelativeOffset-baseIM2RelativeOffset)/(firstM.getLine().first.y - secondM.getLine().first.y);
	}
	
	public int measureHeight() {
		return
		((int) (image.getHeight()*scale))
		;
	}

	public int measureWidth() {
		if(sheetParams == null) {
			throw new IllegalStateException();
		}
		return (int) (image.getWidth()*scale);
	}
	
	private PointF drawOffset = new PointF();
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		SvgRenderer.drawSvgImage(canvas, image, scale, drawOffset, paint);
	}
	
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		IMarker firstM = image.getImarkers().get(0);
		return
			sheetParams.anchorOffset(IM1Anchor, part(firstM))
			- sheetParams.anchorOffset(anchorAbsIndex, part)
			- ((int) (drawOffset.y + (firstM.getLine().first.y*scale)))
		;
	}

}
