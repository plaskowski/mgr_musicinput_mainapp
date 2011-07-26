package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import android.graphics.PointF;
import android.util.Pair;
import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;

public class NoteEnding extends EnhancedSvgImage {
	Pair<PointF, PointF> joinLine = null;

	public Pair<PointF, PointF> getJoinLine() {
		return joinLine;
	}

	public NoteEnding(SvgImage source) throws InvalidMetaException {
		super(source);
		
		// check if we have 1 horizontal imarker
		if(imarkers.size() != 1) {
			throw new InvalidMetaException("Excepted 1 imarker, found "+imarkers.size());
		}
		IMarker iMarker = imarkers.get(0);
		assertLineIsHorizontal(iMarker.line);
		assertTypeRelative(iMarker.color);
		
		// check if we have 1 horizontal marker
		int markersCount = markers.size();
		if(markersCount == 1) {
			joinLine = markers.get(0);
			assertLineIsHorizontal(joinLine);
		} else {
			throw new InvalidMetaException(String.format(
				"Invalid markers amount %d, expected = %d",
				markersCount,
				1
			));
		}
	}

}
