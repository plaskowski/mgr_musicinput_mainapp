package pl.edu.mimuw.students.pl249278.android.svg;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.*;

import java.util.HashMap;
import java.util.Map;

import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class SvgRenderer {
	private static PathsCacheKey temp = new PathsCacheKey(null, 0);
	
	public static void drawSvgImage(Canvas c, SvgImage img, float scale, Paint paint) {
		int total = img.objects.size();
		for(int i = 0; i < total; i++) {
			SvgObject obj = img.objects.get(i);
			if(obj instanceof SvgPath) {
				SvgPath svgPath = (SvgPath) obj;
				temp.path = svgPath;
				temp.scale = scale;
				Path path;
				// FIXME ugly hack to speed up drawing same image at same scale over and over
				if((path = pathsCache.get(temp)) == null) {
					path = generate(svgPath, scale);
					pathsCache.put(new PathsCacheKey(svgPath, scale), path);
				}
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
	
	private static class PathsCacheKey {
		private SvgPath path;
		private float scale;
		
		public PathsCacheKey(SvgPath path, float scale) {
			this.path = path;
			this.scale = scale;
		}
		
		@Override
		public int hashCode() {
			return path.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof PathsCacheKey) {
				PathsCacheKey obj = (PathsCacheKey) o;
				return obj.path == path && obj.scale == scale;
			} else {
				return false;
			}
		}		
	}
	
	private static Map<PathsCacheKey, Path> pathsCache = new HashMap<PathsCacheKey, Path>();
	
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
