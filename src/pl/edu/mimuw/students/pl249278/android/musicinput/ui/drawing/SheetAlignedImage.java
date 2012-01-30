package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import java.util.ArrayList;

import android.graphics.PointF;
import android.util.Pair;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.AdjustableSizeImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img.EnhancedSvgImage.InvalidMetaException;

public class SheetAlignedImage extends SimpleSheetElement {
	float xMiddleMarker;
	
	public float getxMiddleMarker() {
		return xMiddleMarker*scale;
	}
	
	public SheetAlignedImage(AdjustableSizeImage img) throws InvalidMetaException {
		super(img);
		ArrayList<Pair<PointF, PointF>> markers = img.getMarkers();
		// check if we have 1 vertical marker
		int markersCount = markers.size();
		if(markers.size() != 1) {
			throw new EnhancedSvgImage.InvalidMetaException(String.format(
				"Invalid markers amount %d, expected %d",
				markersCount,
				1
			));
		}
		Pair<PointF, PointF> marker = markers.get(0);
		if(EnhancedSvgImage.isLineVertical(marker)) {
			xMiddleMarker = marker.first.x;
		} else {
			throw new EnhancedSvgImage.InvalidMetaException(String.format(
				"Expected vertical marker, got (%f, %f) -> (%f, %f)",
				marker.first.x, marker.first.y,
				marker.second.x, marker.second.y
			));
		}
	}

}
