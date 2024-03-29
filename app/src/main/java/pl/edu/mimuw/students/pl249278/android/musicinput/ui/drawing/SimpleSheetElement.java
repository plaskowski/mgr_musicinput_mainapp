package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.util.ArrayList;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class SimpleSheetElement extends SheetElement {

	protected float scale;
	private AdjustableSizeImage image;
	
	private int IM1Anchor;
	private int IM2Anchor;
	
	public SimpleSheetElement(AdjustableSizeImage img) {
		this(img, 0);
	}

	public SimpleSheetElement(AdjustableSizeImage img, int baseAnchor) {
		this.image = img;
    	IMarker firstM = image.getImarkers().get(0), secondM = image.getImarkers().get(1);
		// discover appropriate parts images
		IM1Anchor = imarkerAnchor(firstM, baseAnchor);
		IM2Anchor = imarkerAnchor(secondM, baseAnchor);
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
	}

	private void sheetParamsCalculations() {
		IMarker firstM = image.getImarkers().get(0), secondM = image.getImarkers().get(1);
		int im1RelativeOffset = sheetParams.anchorOffset(IM1Anchor, part(firstM));
    	int im2RelativeOffset = sheetParams.anchorOffset(IM2Anchor, part(secondM));
    	scale = (im1RelativeOffset-im2RelativeOffset)/(firstM.getLine().first.y - secondM.getLine().first.y);
	}
	
	public int measureHeight() {
		assertParamsPresence();
		return
		((int) (image.getHeight()*scale))
		;
	}

	public int measureWidth() {
		assertParamsPresence();
		return (int) (image.getWidth()*scale);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		SvgRenderer.drawSvgImage(canvas, image, scale, paint);
	}
	
	@Override
	public void getCollisionRegions(ArrayList<Rect> areas,
			ArrayList<Rect> rectsPool) {
		Rect rect = obtain(rectsPool);
		rect.set(0, 0, measureWidth(), measureHeight());
		areas.add(rect);
	}
	
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		IMarker firstM = image.getImarkers().get(0);
		return
			sheetParams.anchorOffset(IM1Anchor, part(firstM))
			- sheetParams.anchorOffset(anchorAbsIndex, part)
			- (int) (firstM.getLine().first.y*scale)
		;
	}

}
