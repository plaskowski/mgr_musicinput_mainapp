package pl.edu.mimuw.students.pl249278.android.musicinput.ui;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_CP1_X;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_CP1_Y;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_CP2_X;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_CP2_Y;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_DEST_X;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_DEST_Y;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_X;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_Y;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CLOSE;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_CUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_LINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_MOVETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RCUBICTO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RLINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RMOVETO;
import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgObject;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Pair;
import android.view.View;

public class NoteView extends View {

	private NoteBase base;
	private NoteEnding ending;
	private SheetParams sheetParams;
	private Paint paint = new Paint();
	
	private int baseIM1Anchor;
	private int baseIM2Anchor;
	private int endingIMAnchor;
	/** in base units */
	private float baseXoffset;
	/** in base units */
	private float endingXoffset;
	/** in base units */
	private float composedWidth;
	private float ratioE2B;
	private float scaleB;
	private float scaleE;
	private PointF baseDrawOffset;
	private PointF endingDrawOffset;
	private AnchorPart baseIM1AnchorPart;

	public NoteView(Context context, int noteLength, int noteHeight) throws NoteDescriptionLoadingException {
		super(context);
		
		// FIXME real logic for discovering if it's upsidedown or normal
		boolean upsdown = noteHeight <= NoteConstants.anchorIndex(2, NoteConstants.ANCHOR_TYPE_LINE);
		// FIXME real logic for discovering anchors
		int baseAnchor = noteHeight;
		int endingAnchor = baseAnchor + (upsdown ? 6 : -6);
		
		// discover appropriate parts images
		this.base = NotePartFactory.getBaseImage(context, noteLength, NoteConstants.anchorType(baseAnchor), upsdown);
    	IMarker firstM = base.getImarkers().get(0), secondM = base.getImarkers().get(1);
		baseIM1Anchor = imarkerAnchor(firstM, baseAnchor);
    	baseIM1AnchorPart = part(firstM);
		baseIM2Anchor = imarkerAnchor(secondM, baseAnchor);

		this.ending = NotePartFactory.getEndingImage(context, noteLength, NoteConstants.anchorType(endingAnchor), upsdown);
		if(ending != null) {
			endingIMAnchor = imarkerAnchor(ending.imarkers.get(0), endingAnchor);
	
			ratioE2B = lineXSpan(base.getJoinLine()) / lineXSpan(ending.getJoinLine());
	    	float diff = base.getJoinLine().first.x - ending.getJoinLine().first.x * ratioE2B;
			if(diff >= 0) {
				this.baseXoffset = 0;
				this.endingXoffset = diff;
			} else {
				this.baseXoffset = -diff;
				this.endingXoffset = 0;
			}
			this.composedWidth = Math.max(baseXoffset+base.getWidth(), endingXoffset+ending.getWidth()*ratioE2B);
		} else {
			this.baseXoffset = 0;
			this.composedWidth = base.getWidth();
		}
	}
	
	public void setSheetParams(SheetParams params) {
		this.sheetParams = params;
	
    	IMarker firstM = base.getImarkers().get(0), secondM = base.getImarkers().get(1);
		int baseIM1RelativeOffset = sheetParams.anchorOffset(baseIM1Anchor, baseIM1AnchorPart);
    	int baseIM2RelativeOffset = sheetParams.anchorOffset(baseIM2Anchor, part(secondM));
    	scaleB = (baseIM1RelativeOffset-baseIM2RelativeOffset)/(firstM.getLine().first.y - secondM.getLine().first.y);
    	baseDrawOffset = new PointF(
			baseXoffset * scaleB, 0
		);
    		
    	if(ending != null) {
	    	scaleE = scaleB * ratioE2B;
	    	IMarker endingIM = ending.getImarkers().get(0);
	    	int endingIMRelativeOffset = sheetParams.anchorOffset(endingIMAnchor, part(endingIM));
			endingDrawOffset = new PointF(
				endingXoffset * scaleB, 0
			);
	    	
	    	int baseTopOffset = (int) (baseIM1RelativeOffset - firstM.line.first.y * scaleB);
	    	int endingTopOffset = (int) (endingIMRelativeOffset - endingIM.line.first.y * scaleE);
	    	if(baseTopOffset > endingTopOffset) {
	    		baseDrawOffset.y = baseTopOffset - endingTopOffset;
	    	} else {
	    		endingDrawOffset.y = endingTopOffset - baseTopOffset;
	    	}
    	}
    	
    	invalidate();
	}
	
	private AnchorPart part(IMarker imarker) {
		if(EnhancedSvgImage.isTypeBottomEdge(imarker))
			return AnchorPart.BOTTOM_EDGE;
		else
			return AnchorPart.MIDDLE;
	}

	private static int imarkerAnchor(EnhancedSvgImage.IMarker iMarker, int enhImgAnchor) {
		int index = EnhancedSvgImage.alphaToIndex(iMarker.getAlpha());
		if(EnhancedSvgImage.isTypeRelative(iMarker)) {
			return enhImgAnchor + index;
		} else {
			return index;
		}
	}

	private static float lineXSpan(Pair<PointF, PointF> line) {
		return Math.abs(line.first.x - line.second.x);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(sheetParams == null) {
			LogUtils.info("onMeasure without sheetParams set");
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		setMeasuredDimension(
			measureWidth(), 
			(int) (Math.max(
				baseDrawOffset.y + base.getHeight()*scaleB,
				ending == null ? 0 : endingDrawOffset.y + ending.getHeight()*scaleE
			))
		);
	}

	public int measureWidth() {
		if(sheetParams == null) {
			throw new IllegalStateException();
		}
		return (int) (composedWidth*scaleB);
	}
	
	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		return
			sheetParams.anchorOffset(baseIM1Anchor, baseIM1AnchorPart)
			- sheetParams.anchorOffset(anchorAbsIndex, part)
			- ((int) (baseDrawOffset.y + (base.getImarkers().get(0).line.first.y*scaleB)))
		;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		canvas.drawColor(Color.YELLOW);
		
		if(ending != null) {
			PointF baseJLStart = new PointF(baseDrawOffset.x, baseDrawOffset.y);
			baseJLStart.offset(base.getJoinLine().first.x*scaleB, base.getJoinLine().first.y * scaleB);
			PointF endingJLEnd = new PointF(endingDrawOffset.x, endingDrawOffset.y);
			endingJLEnd.offset(ending.getJoinLine().second.x*scaleE, ending.getJoinLine().second.y * scaleE);
			canvas.drawRect(
				baseJLStart.x,
				Math.min(baseJLStart.y, endingJLEnd.y)-1,
				endingJLEnd.x,
				Math.max(baseJLStart.y, endingJLEnd.y)+1,
				paint
			);
	
			/*
			paint.setColor(Color.GREEN);
			canvas.drawRect(
				endingDrawOffset.x, endingDrawOffset.y, 
				endingDrawOffset.x + ending.getWidth()*scaleE, endingDrawOffset.y+ending.getHeight()*scaleE, 
				paint
			);
			*/
			
			paint.setColor(Color.BLACK);
			drawSvgImage(canvas, ending, scaleE, endingDrawOffset, paint);
		}
		
		/*
		paint.setColor(Color.GRAY);
		canvas.drawRect(
			baseDrawOffset.x, baseDrawOffset.y, 
			baseDrawOffset.x + base.getWidth()*scaleB, baseDrawOffset.y + base.getHeight()*scaleB, 
			paint
		);
		*/

		paint.setColor(Color.BLACK);
		drawSvgImage(canvas, base, scaleB, baseDrawOffset, paint);
	}
	
	// TODO move to more appropriate class
	private void drawSvgImage(Canvas c, SvgImage img, float scale, PointF pxOffset, Paint paint) {
		
		for(SvgObject obj: img.objects) {
			if(obj instanceof SvgPath) {
				Path path = generate((SvgPath) obj, scale, pxOffset);
				c.drawPath(path, paint);
			} else {
				throw new RuntimeException("Supported type not handled: "+obj.getClass().getName());
			}
		}
	}
	
	// TODO move to more appropriate class
	private static Path generate(SvgPath path, float scale, PointF pxOffset) {
		Path result = new Path();
		SvgPathCommand cmd = new SvgPathCommand();
		for(MemorySaavyIterator<SvgPathCommand> it = path.getIterator(); it.hasNext();) {
			it.readNext(cmd);
			char cmdLabel = cmd.cmd;
			float[] args = cmd.args;
			scaleArgs(scale, args);
			switch(cmdLabel) {
			case PATH_CMD_MOVETO:
				result.moveTo(args[ARG_X]+pxOffset.x, args[ARG_Y]+pxOffset.y);
				break;
			case PATH_CMD_RMOVETO:
				result.rMoveTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_LINETO:
				result.lineTo(args[ARG_X]+pxOffset.x, args[ARG_Y]+pxOffset.y);
				break;
			case PATH_CMD_RLINETO:
				result.rLineTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_CUBICTO:
				result.cubicTo(
					args[ARG_CP1_X]+pxOffset.x, args[ARG_CP1_Y]+pxOffset.y, 
					args[ARG_CP2_X]+pxOffset.x, args[ARG_CP2_Y]+pxOffset.y, 
					args[ARG_DEST_X]+pxOffset.x, args[ARG_DEST_Y]+pxOffset.y
				);
				break;
			case PATH_CMD_RCUBICTO:
				result.rCubicTo(
					args[ARG_CP1_X], args[ARG_CP1_Y], 
					args[ARG_CP2_X], args[ARG_CP2_Y], 
					args[ARG_DEST_X], args[ARG_DEST_Y]
				);
				break;
			case PATH_CMD_CLOSE:
				result.close();
			}
		}
		return result;
	}

	private static void scaleArgs(float scale, float[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i]*scale;
		}
	}

	public int getBaseMiddleX() {
		return (int) (baseDrawOffset.x + base.xMiddleMarker * scaleB);
	}	
	
}
