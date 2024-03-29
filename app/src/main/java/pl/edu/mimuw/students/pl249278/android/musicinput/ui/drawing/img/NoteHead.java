package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img;

import android.graphics.PointF;
import android.util.Pair;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;

public class NoteHead extends AdjustableSizeImage {
	Pair<PointF, PointF> joinLine = null;
	float xMiddleMarker;

	public Pair<PointF, PointF> getJoinLine() {
		return joinLine;
	}

	public float getxMiddleMarker() {
		return xMiddleMarker;
	}

	public NoteHead(SvgImage source, boolean hasJoinLine) throws InvalidMetaException {
		super(source, true);
		
		// check if we have 1 vertical marker and optional horizontal marker
		int markersCount = markers.size();
		Pair<PointF, PointF> verticalMarker;
		if(markersCount == 1 && !hasJoinLine) {
			verticalMarker = markers.get(0);
			assertLineIsVertical(verticalMarker);
		} else if(markersCount == 2 && hasJoinLine) {
			Pair<PointF, PointF> m1 = markers.get(0), m2 = markers.get(1);
			if(isLineHorizontal(m1) && isLineVertical(m2)) {
				joinLine = m1;
				verticalMarker = m2;
			} else if(isLineHorizontal(m2) && isLineVertical(m1)) {
				joinLine = m2;
				verticalMarker = m1;
			} else {
				throw new InvalidMetaException("Invalid configuration of markers");
			}
		} else {
			throw new InvalidMetaException(String.format(
				"Invalid markers amount %d, when hasJoinLine = %s",
				markersCount,
				Boolean.toString(hasJoinLine)
			));
		}
		xMiddleMarker = verticalMarker.first.x;
	}
}
