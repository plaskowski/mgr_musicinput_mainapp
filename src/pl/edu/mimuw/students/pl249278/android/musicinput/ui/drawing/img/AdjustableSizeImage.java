package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.img;

import pl.edu.mimuw.students.pl249278.android.svg.SvgImage;

public class AdjustableSizeImage extends EnhancedSvgImage {

	public AdjustableSizeImage(SvgImage source, Boolean realtiveIMarkers) throws InvalidMetaException {
		super(source);
		
		// check if we have 2 horizontal imarkers
		if(imarkers.size() != 2) {
			throw new InvalidMetaException("Excepted 2 imarkers, found "+imarkers.size());
		}
		for (IMarker iMarker : imarkers) {
			assertLineIsHorizontal(iMarker.line);
			if(realtiveIMarkers == null)
				continue;
			else if(realtiveIMarkers)
				assertTypeRelative(iMarker);
			else 
				assertTypeAbsolute(iMarker);
		}
	}
}
