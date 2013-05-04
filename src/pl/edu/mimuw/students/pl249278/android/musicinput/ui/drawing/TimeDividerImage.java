package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.util.ArrayList;
import java.util.Arrays;

import android.graphics.PointF;
import android.util.Pair;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.InvalidMetaException;

public class TimeDividerImage extends SheetAlignedImage {
	private float leftMarker, rightMarker;

	public TimeDividerImage(AdjustableSizeImage img)
			throws InvalidMetaException {
		this(img, parseVerticalMarkersAndSort(img.getMarkers()));
	}
	
	private TimeDividerImage(AdjustableSizeImage img,
			float[] sortedVerticalMarkers) throws InvalidMetaException {
		super(img, sortedVerticalMarkers[sortedVerticalMarkers.length/2]);
		leftMarker = sortedVerticalMarkers[0];
		rightMarker = sortedVerticalMarkers[2];
	}

	private static float[] parseVerticalMarkersAndSort(ArrayList<Pair<PointF, PointF>> markersTripple) throws InvalidMetaException {
		int total = markersTripple.size();
		if(total != 3) 
			throw new InvalidMetaException("TimeDivider image expected to have exactly 3 markers, has " + total);
		float[] result = new float[total];
		for (int i = 0; i < total; i++) {
			Pair<PointF, PointF> marker = markersTripple.get(i);
			if(EnhancedSvgImage.isLineVertical(marker)) {
				result[i] = marker.first.x;
			} else {
				throw new EnhancedSvgImage.InvalidMetaException(String.format(
					"Expected marker#%d to be vertical, got (%f, %f) -> (%f, %f)",
					i,
					marker.first.x, marker.first.y,
					marker.second.x, marker.second.y
				));
			}
		}
		Arrays.sort(result);
		return result;
	}
	
	public float getLeftEdge() {
		return leftMarker * scale;
	}
	
	public float getRightEdge() {
		return rightMarker * scale;
	}

}
