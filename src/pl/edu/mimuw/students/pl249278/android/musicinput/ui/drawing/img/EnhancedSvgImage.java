package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img;

import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_X;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.ARG_Y;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_MOVETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RLINETO;
import static pl.edu.mimuw.students.pl249278.android.svg.SvgPath.PATH_CMD_RMOVETO;

import java.util.ArrayList;

import org.joda.primitives.list.impl.ArrayIntList;

import pl.edu.mimuw.students.pl249278.android.svg.StyleAttribute;
import pl.edu.mimuw.students.pl249278.android.svg.StyleAttribute.ValueType;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;
import pl.edu.mimuw.students.pl249278.android.svg.SvgObject;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.MemorySaavyIterator;
import pl.edu.mimuw.students.pl249278.android.svg.SvgPath.SvgPathCommand;
import pl.edu.mimuw.students.pl249278.android.svg.SvgRect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Pair;

public class EnhancedSvgImage extends SvgImage {
	protected static final int CLIPAREA_COLOR = 0xFF0000;
	protected static final int MARKER_COLOR = 0x00FF00;
	protected static final int MARKER_ALPHA = 50;
	protected static final int IMARKER_MIDDLE_REL_COLOR = 0x0000FF;
	protected static final int IMARKER_MIDDLE_COLOR = 0x00FFFF;
	/** bottom edge */
	protected static final int IMARKER_EDGE_REL_COLOR = 0xFF00FF;
	protected static final int IMARKER_EDGE_COLOR = 0x0FF0FF;
	private static final ArrayIntList IMARKERS = new ArrayIntList(new int[] {
		IMARKER_MIDDLE_REL_COLOR,
		IMARKER_MIDDLE_COLOR,
		IMARKER_EDGE_REL_COLOR,
		IMARKER_EDGE_COLOR
	});
	
	public ArrayList<Pair<PointF, PointF>> getMarkers() {
		return markers;
	}

	public ArrayList<IMarker> getImarkers() {
		return imarkers;
	}

	protected ArrayList<Pair<PointF, PointF>> markers = new ArrayList<Pair<PointF,PointF>>();
	protected ArrayList<IMarker> imarkers = new ArrayList<EnhancedSvgImage.IMarker>();
	
	public static class IMarker {
		int alpha;
		public int getAlpha() {
			return alpha;
		}
		public int getColor() {
			return color;
		}
		public Pair<PointF, PointF> getLine() {
			return line;
		}
		int color;
		Pair<PointF, PointF> line;
		public IMarker(int alpha, int color, Pair<PointF, PointF> line) {
			super();
			this.alpha = alpha;
			this.color = color;
			this.line = line;
		}
	}
	
	private static int alpha(float opacityVal) {
		return (int) (opacityVal*255);
	}
	
	@SuppressWarnings("serial")
	public static class InvalidMetaException extends Exception {
		public InvalidMetaException(String detailMessage) {
			super(detailMessage);
		}
	}

	public EnhancedSvgImage(SvgImage source) throws InvalidMetaException {
		super(source.getWidth(), source.getHeight(), null);
		
		RectF clipArea = null;
		for (int i = 0; i < source.objects.size(); i++) {
			SvgObject obj = source.objects.get(i);
			
			if(obj instanceof SvgRect) {
				SvgRect rect = (SvgRect) obj;
				if(matchesFill(rect, CLIPAREA_COLOR, MARKER_ALPHA)) {
					if(clipArea != null) {
						throw new InvalidMetaException("Found second ClipArea meta-object");
					}
					clipArea = new RectF(
						rect.getX(), rect.getY(),
						rect.getX()+rect.getWidth(), rect.getY()+rect.getHegiht()
					);
					obj = null;
				}
			} else if(obj instanceof SvgPath && isStraightLine((SvgPath) obj)) {
				SvgPath path = (SvgPath) obj;
				if(matchesStrokeFill(obj, MARKER_COLOR, MARKER_ALPHA)) {
					markers.add(asLine(path));
					obj = null;
				} else if(IMARKERS.contains(obj.getIntProperty(StyleAttribute.STROKE, ValueType.PAINT_NONE))) {
					imarkers.add(new IMarker(
						alpha(obj.getFloatProperty(StyleAttribute.STROKE_OPACITY, 0f)),
						obj.getIntProperty(StyleAttribute.STROKE, ValueType.PAINT_NONE),
						asLine(path)
					));
					obj = null;
				}
			}
			
			if(obj != null) {
				this.objects.add(obj);
			}
		}
		
		// apply clip area
		if(clipArea != null) {
			for (SvgObject obj : objects) {
				obj.translate(-clipArea.left, -clipArea.top);
			}
			for (Pair<PointF, PointF> marker : markers) {
				translateLine(marker, -clipArea.left, -clipArea.top);
			}
			for(IMarker marker: imarkers) {
				translateLine(marker.line, -clipArea.left, -clipArea.top);
			}
			this.width = clipArea.width();
			this.height = clipArea.height();
		}
	}

	private static void translateLine(Pair<PointF, PointF> line, float dx, float dy) {
		line.first.offset(dx, dy);
		line.second.offset(dx, dy);
	}

	public static int alphaToIndex(int alpha) {
		int diff = alpha-125;
		int sign = diff >= 0 ? 1 : -1;
		diff = Math.abs(diff);
		int index = diff/5;
		int rest = diff%5;
		int result = sign*(index + (rest > 2 ? 1 : 0));
		return result;
	}

	private static Pair<PointF, PointF> asLine(SvgPath obj) {
		MemorySaavyIterator<SvgPathCommand> it = obj.getIterator();
		SvgPathCommand first = new SvgPathCommand(), second = new SvgPathCommand();
		it.readNext(first);
		it.readNext(second);
		PointF start = arg(first, ARG_X, ARG_Y), end = arg(second, ARG_X, ARG_Y);
		if(second.cmd == PATH_CMD_RLINETO) {
			end.offset(start.x, start.y);
		}
		if(start.x <= end.x)
			return new Pair<PointF, PointF>(start, end);
		else 
			return new Pair<PointF, PointF>(end, start);
	}

	private boolean matchesFill(SvgObject obj, int color, int alpha) {
		return matchesPaintProperty(obj, StyleAttribute.FILL, color)
		&& matchesOpacityProperty(obj, StyleAttribute.FILL_OPACITY, alpha);
	}
	
	private boolean matchesStrokeFill(SvgObject obj, int color, int alpha) {
		return matchesPaintProperty(obj, StyleAttribute.STROKE, color)
		&& matchesOpacityProperty(obj, StyleAttribute.STROKE_OPACITY, alpha);
	}
	
	private boolean matchesPaintProperty(SvgObject obj, StyleAttribute property, int color) {
		return color == obj.getIntProperty(property, ValueType.PAINT_NONE); 
	}
	private boolean matchesOpacityProperty(SvgObject obj, StyleAttribute property, int alpha) {
		return alpha == alpha(obj.getFloatProperty(property, 0f));
	}

	private boolean isStraightLine(SvgPath obj) {
		if(obj.commandsCount() != 2) return false;
		MemorySaavyIterator<SvgPathCommand> it = obj.getIterator();
		SvgPathCommand first = new SvgPathCommand(), second = new SvgPathCommand();
		it.readNext(first);
		it.readNext(second);
		return ( 
		(first.cmd == PATH_CMD_MOVETO || first.cmd == PATH_CMD_RMOVETO)
		&& (
			(second.cmd == SvgPath.PATH_CMD_RLINETO && arg(second, ARG_X) * arg(second, ARG_Y) == 0)
			||
			(second.cmd == SvgPath.PATH_CMD_LINETO && (
				arg(first, ARG_X) == arg(second, ARG_X)
				|| arg(first, ARG_Y) == arg(second, ARG_Y)
			))
		));
	}
	
	protected void assertTypeRelative(IMarker marker) throws InvalidMetaException {
		if(!isTypeRelative(marker.color)) {
			throw new InvalidMetaException("Expected relative imarker type, got "+marker.color);
		}
	}
	protected void assertTypeAbsolute(IMarker marker) throws InvalidMetaException {
		if(isTypeRelative(marker.color)) {
			throw new InvalidMetaException("Expected absolute imarker type, got "+marker.color);
		}
	}

	protected void assertLineIsHorizontal(Pair<PointF, PointF> line)
			throws InvalidMetaException {
				if(!isLineHorizontal(line))
					throw new InvalidMetaException("Expected horizontal line, got "+line);
			}

	protected void assertLineIsVertical(Pair<PointF, PointF> line)
			throws InvalidMetaException {
				if(!isLineVertical(line))
					throw new InvalidMetaException("Expected vertical line, got "+line);
			}

	public static boolean isLineHorizontal(Pair<PointF, PointF> line) {
		return line.first.y ==  line.second.y;
	}

	public static boolean isLineVertical(Pair<PointF, PointF> line) {
		return line.first.x ==  line.second.x;
	}

	private static float arg(SvgPathCommand cmd, int index) {
		return cmd.args[index];
	}
	
	private static PointF arg(SvgPathCommand cmd, int xIndex, int yIndex) {
		return new PointF(arg(cmd, xIndex), arg(cmd, yIndex));
	}

	private static boolean isTypeRelative(int color) {
		return color == IMARKER_EDGE_REL_COLOR || color == IMARKER_MIDDLE_REL_COLOR;
	}

	private static boolean isTypeBottomEdge(int color) {
		return color == IMARKER_EDGE_COLOR || color == IMARKER_EDGE_REL_COLOR;
	}

	public static boolean isTypeRelative(IMarker iMarker) {
		return isTypeRelative(iMarker.getColor());
	}

	public static boolean isTypeBottomEdge(IMarker imarker) {
		return isTypeBottomEdge(imarker.getColor());
	}
}
