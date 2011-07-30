package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgRenderer.drawSvgImage;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.NoteHead;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

public class NoteHeadElement extends SheetAlignedElement {

	private NoteSpec spec;
	private NoteHead head;
	
	private int headIM1Anchor;
	private int headIM2Anchor;
	private float scale;
	private AnchorPart headIM1AnchorPart;

	public NoteHeadElement(Context context, NoteSpec noteSpec) throws NoteDescriptionLoadingException {
		setNoteSpec(context, noteSpec);
	}
	
	private void setNoteSpec(Context context, NoteSpec noteSpec)
			throws NoteDescriptionLoadingException {
		this.spec = noteSpec;
		// FIXME real logic for discovering if it's upsidedown or normal
		boolean upsdown = noteSpec.getOrientation() == NoteSpec.ORIENT_DOWN;
		int headAnchor = noteSpec.positon();
		
		// discover appropriate parts images
		this.head = NotePartFactory.getHeadImage(context, noteSpec.length(), NoteConstants.anchorType(headAnchor), upsdown);
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
		return (int) (head.getHeight()*scale);
	}

	@Override
	public int measureWidth() {
		assertParamsPresence();
		return (int) (head.getWidth()*scale);
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		drawSvgImage(canvas, head, scale, paint);
	}
	
	@Override
	public int getHeadMiddleX() {
		return (int) (head.getxMiddleMarker() * scale);
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
	public NoteSpec getNoteSpec() {
		return this.spec;
	}
	
	float joinLineExactWidth() {
		assertJLpresence();
		return scale * lineXSpan(head.getJoinLine());
	}
	
	int joinLineX() {
		assertJLpresence();
		return (int) (head.getJoinLine().first.x * scale);
	}
	int joinLineY() {
		assertJLpresence();
		return (int) (head.getJoinLine().first.y * scale);
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
