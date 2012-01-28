package pl.edu.mimuw.students.pl249278.android.svg;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.*;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class SvgRenderer {
	
	public static void drawSvgImage(Canvas c, SvgImage img, float scale, Paint paint) {
		
		for(SvgObject obj: img.objects) {
			if(obj instanceof SvgPath) {
				Path path = generate((SvgPath) obj, scale);
				c.drawPath(path, paint);
			} else if(obj instanceof SvgRect) {
				SvgRect rect = (SvgRect) obj;
				c.drawRect(
					scale*rect.x, scale*rect.y,
					scale*(rect.x+rect.width), scale*(rect.y+rect.hegiht),
					paint
				);
			} else {
				throw new RuntimeException("Supported type not handled: "+obj.getClass().getName());
			}
		}
	}
	
	private static Path generate(SvgPath path, float scale) {
		Path result = new Path();
		SvgPathCommand cmd = new SvgPathCommand();
		for(MemorySaavyIterator<SvgPathCommand> it = path.getIterator(); it.hasNext();) {
			it.readNext(cmd);
			char cmdLabel = cmd.cmd;
			float[] args = cmd.args;
			scaleArgs(scale, args);
			switch(cmdLabel) {
			case PATH_CMD_MOVETO:
				result.moveTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_RMOVETO:
				result.rMoveTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_LINETO:
				result.lineTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_RLINETO:
				result.rLineTo(args[ARG_X], args[ARG_Y]);
				break;
			case PATH_CMD_CUBICTO:
				result.cubicTo(
					args[ARG_CP1_X], args[ARG_CP1_Y], 
					args[ARG_CP2_X], args[ARG_CP2_Y], 
					args[ARG_DEST_X], args[ARG_DEST_Y]
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
