package pl.edu.mimuw.students.pl249278.android.musicinput.test;

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
import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.EnhancedSvgImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.EnhancedSvgImage.IMarker;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteBase;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteEnding;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgObject;
import pl.edu.mimuw.students.pl249278.android.svg.SvgParser;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;

public class SvgTestActivity extends Activity {
	int zeroLineYOffset = 50;
	int lineThickness = 15;
	int linespacingThickness = 80;
	//TODO externally provide where should note middle go
	int middleDestX = 150;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.test_svgtest);
        
        ImageView imgV = (ImageView) findViewById(R.id.test_imgview);
        Bitmap b = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.drawColor(Color.WHITE);
        
        // draw visible anchors
        for(int i = 0; i < 5; i++) {
        	float yoffset = zeroLineYOffset+i*(linespacingThickness+lineThickness);
        	c.drawRect(0, yoffset, 500, yoffset+lineThickness, new Paint());
        }

        SvgParser parser = new SvgParser();
        try {
        	NoteBase base = new NoteBase(parser.parse(getResources().getXml(R.xml.test_obrazek)), true);
        	NoteEnding ending = new NoteEnding(parser.parse(getResources().getXml(R.xml.test_8ending)));
        	
        	//TODO magicallly calculate that note spans from 8 to 2 area
        	int baseAnchor = 6;
        	int endingAnchor = 0;
        	
        	// calculate base scale
        	IMarker firstM = base.getImarkers().get(0), secondM = base.getImarkers().get(1);
			int firstAnchor = imarkerAnchor(firstM, baseAnchor);
			int secondAnchor = imarkerAnchor(secondM, baseAnchor);
        	int firstRealOffset = anchorRealOffset(firstAnchor, firstM);
        	int secondRealOffset = anchorRealOffset(secondAnchor, secondM);
        	float scaleB = (firstRealOffset-secondRealOffset)/(firstM.getLine().first.y - secondM.getLine().first.y);
        	PointF offsetB = new PointF(
        		middleDestX - base.getxMiddleMarker()*scaleB,
        		firstRealOffset - firstM.getLine().first.y*scaleB
			);
        	
        	// paint base
        	drawSvgImage(c, base, scaleB, offsetB);
        	
        	float jlBlength = lineXSpan(base.getJoinLine());
        	float jlBrealLength = jlBlength*scaleB;
        	Pair<PointF, PointF> endingJL = ending.getJoinLine();
			float jlEndLength = lineXSpan(endingJL);
        	float scaleE = jlBrealLength / jlEndLength;
        	IMarker endingIM = ending.getImarkers().get(0);
			PointF offsetE = new PointF(
        		offsetB.x + base.getJoinLine().first.x * scaleB - endingJL.first.x * scaleE,
        		anchorRealOffset(imarkerAnchor(endingIM, endingAnchor), endingIM) - endingIM.getLine().first.y*scaleE
			);
			
			// paint ending
			drawSvgImage(c, ending, scaleE, offsetE);
			
			// paint connection
			c.drawRect(
				offsetE.x + endingJL.first.x*scaleE,
				offsetE.y + endingJL.first.y*scaleE,
				offsetB.x + base.getJoinLine().second.x * scaleB,
				offsetB.y + base.getJoinLine().first.y * scaleB,
				new Paint()
			);
        	
        	/*
			SvgImage svgImg = parser.parse(getResources().getXml(R.xml.test_obrazek));
	        SvgPath svgPath = (SvgPath) svgImg.objects.get(1);
	        Path path;
	        
			path = generate(svgPath, 0.5f);
			c.drawPath(path, paint);
			
			path = generate(svgPath, 1.5f);
			c.drawPath(path, paint);
			
			path = generate(svgPath, 1f);
			paint.setColor(Color.GRAY);
			c.drawPath(path, paint);
			*/
        	
	        imgV.setImageBitmap(b);
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	
	private float lineXSpan(Pair<PointF, PointF> line) {
		return Math.abs(line.first.x - line.second.x);
	}

	private void drawSvgImage(Canvas c, SvgImage img, float scale, PointF pxOffset) {
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		for(SvgObject obj: img.objects) {
			if(obj instanceof SvgPath) {
				Path path = generate((SvgPath) obj, scale, pxOffset);
				c.drawPath(path, paint);
			} else {
				throw new RuntimeException("Supported type not handled: "+obj.getClass().getName());
			}
		}
	}

	private int anchorRealOffset(int absAnchorIndex, IMarker iMarker) {
		//TODO inline
		int anchorSize = absAnchorIndex%2 == 0 ? lineThickness : linespacingThickness;
		int result = zeroLineYOffset;
		int linesBefore = (absAnchorIndex+1)/2;
		int spacesBefore = absAnchorIndex/2;
		int linesBeforeSize = linesBefore*lineThickness;
		int spacesBeforeSize = spacesBefore*linespacingThickness;
		int baseDiff = linesBeforeSize + spacesBeforeSize;
		int optDiff = EnhancedSvgImage.isTypeBottomEdge(iMarker.getColor()) ? anchorSize : anchorSize/2;
		result += optDiff;
		result += baseDiff;
		return result;
	}

	private int imarkerAnchor(EnhancedSvgImage.IMarker iMarker, int enhImgAnchor) {
		int index = EnhancedSvgImage.alphaToIndex(iMarker.getAlpha());
		if(EnhancedSvgImage.isTypeRelative(iMarker.getColor())) {
			return enhImgAnchor + index;
		} else {
			return index;
		}
	}

	private Path generate(SvgPath path, float scale, PointF pxOffset) {
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

	private void scaleArgs(float scale, float[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i]*scale;
		}
	}	
}
