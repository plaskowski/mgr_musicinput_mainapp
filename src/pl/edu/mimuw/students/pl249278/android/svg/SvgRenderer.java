package pl.edu.mimuw.students.pl249278.android.svg;

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
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

public class SvgRenderer {
	
	public static void drawSvgImage(Canvas c, SvgImage img, float scale, PointF pxOffset, Paint paint) {
		
		for(SvgObject obj: img.objects) {
			if(obj instanceof SvgPath) {
				Path path = generate((SvgPath) obj, scale, pxOffset);
				c.drawPath(path, paint);
			} else {
				throw new RuntimeException("Supported type not handled: "+obj.getClass().getName());
			}
		}
	}
	
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
}
