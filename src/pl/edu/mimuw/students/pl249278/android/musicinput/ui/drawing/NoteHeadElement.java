package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer.drawSvgImage;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteHead;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class NoteHeadElement extends SheetAlignedElement {
	static int METAVAL_JOINLINE_LEFT = registerIndex();
	static int METAVAL_JOINLINE_RIGHT = registerIndex();
	static int NOTEHEAD_LEFT = registerIndex();
	static int NOTEHEAD_RIGHT = registerIndex();
	static int AREA_NOTEHEAD_LEFT = registerIndex();
	static int AREA_NOTEHEAD_RIGHT = registerIndex();
	static int JOINLINE_Y = SheetAlignedElement.registerIndex();

	private ElementSpec.NormalNote spec;
	private NoteHead head;
	
	private int headIM1Anchor;
	private int headIM2Anchor;
	private float scale;
	private AnchorPart headIM1AnchorPart;

	public NoteHeadElement(Context context, NormalNote elementSpec) throws NoteDescriptionLoadingException {
		this.spec = elementSpec;
		boolean upsdown = spec.getOrientation() == NoteConstants.ORIENT_DOWN;
		int headAnchor = spec.noteSpec().positon();
		
		// discover appropriate parts images
		this.head = NotePartFactory.getHeadImage(context, spec.lengthSpec().length(), NoteConstants.anchorType(headAnchor), upsdown);
    	IMarker firstM = head.getImarkers().get(0), secondM = head.getImarkers().get(1);
		headIM1Anchor = imarkerAnchor(firstM, headAnchor);
    	headIM1AnchorPart = part(firstM);
		headIM2Anchor = imarkerAnchor(secondM, headAnchor);

		if(sheetParams != null) {
			sheetParamsCalculations();
		}
	}
	
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		sheetParamsCalculations();
	}

	private static LogUtils log = new LogUtils("Note");
	private void sheetParamsCalculations() {
		IMarker firstM = head.getImarkers().get(0), secondM = head.getImarkers().get(1);
		int headIM1RelativeOffset = sheetParams.anchorOffset(headIM1Anchor, headIM1AnchorPart);
    	int headIM2RelativeOffset = sheetParams.anchorOffset(headIM2Anchor, part(secondM));
    	scale = ((float) (headIM1RelativeOffset-headIM2RelativeOffset))/(firstM.getLine().first.y - secondM.getLine().first.y);
    	log.i("scaleH: %f", scale);
	}
	
	@Override
	public int measureHeight() {
		assertParamsPresence();
		return (int) Math.ceil(head.getHeight()*scale);
	}

	@Override
	public int measureWidth() {
		assertParamsPresence();
		return (int) Math.ceil(head.getWidth()*scale);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		drawSvgImage(canvas, head, scale, paint);
	}
	
	@Override
	public int getHorizontalOffset(int lineIdentifier) {
		if(lineIdentifier == NOTEHEAD_LEFT || lineIdentifier == AREA_NOTEHEAD_LEFT) {
			return 0;
		} else if(lineIdentifier == NOTEHEAD_RIGHT || lineIdentifier == AREA_NOTEHEAD_RIGHT) {
			return measureWidth();
		} else if(lineIdentifier == SheetAlignedElement.MIDDLE_X) {
			return (int) (head.getxMiddleMarker() * scale);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public int getVerticalOffset(int lineIdentifier) {
		if(lineIdentifier == JOINLINE_Y) {
			assertJLpresence();
			return (int) joinLineY();
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public float getMetaValue(int valueIndentifier, int param) {
		if(valueIndentifier == METAVAL_JOINLINE_LEFT) {
			return joinLineLeft();
		} else if(valueIndentifier == METAVAL_JOINLINE_RIGHT) {
			assertJLpresence();
			return head.getJoinLine().second.x * scale;
		}
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return
			sheetParams.anchorOffset(headIM1Anchor, headIM1AnchorPart)
			- sheetParams.anchorOffset(anchorAbsIndex, part)
			- ((int) ((head.getImarkers().get(0).getLine().first.y*scale)))
		;
	}
	
	@Override
	public ElementSpec getElementSpec() {
		return spec;
	}
	
	float joinLineExactWidth() {
		assertJLpresence();
		return scale * lineXSpan(head.getJoinLine());
	}
	
	float joinLineLeft() {
		assertJLpresence();
		return head.getJoinLine().first.x * scale;
	}
	float joinLineY() {
		assertJLpresence();
		return head.getJoinLine().first.y * scale;
	}

	private void assertJLpresence() {
		if(head.getJoinLine() == null) {
			throw new UnsupportedOperationException("This note head image doesn't contain join line");
		}
	}

	float getScale() {
		return scale;
	}

}
