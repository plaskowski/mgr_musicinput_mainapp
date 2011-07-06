	package pl.edu.mimuw.students.pl249278.android.musicinput.ui;
	
	public class SheetParams {
	
		private int lineFactor;
		private int linespacingFactor;
		private float scale = 1;
		
		public SheetParams(int lineFactor, int linespacingFactor) {
			super();
			this.lineFactor = lineFactor;
			this.linespacingFactor = linespacingFactor;
		}

		public static enum AnchorPart {
			MIDDLE,
			BOTTOM_EDGE,
			TOP_EDGE
		}
		
		private static int line0absIndex = NoteConstants.anchorIndex(0, NoteConstants.ANCHOR_TYPE_LINE);
		
		/**
		 * @param anchorAbsIndex absolute index of anchor
		 * @param part determines exact horizontal line inside anchor we measure distance to
		 * @return anchor[absIndex][part].y - [line0 (first line from 5lines)][top edge].y
		 */
		public int anchorOffset(int anchorAbsIndex, AnchorPart part) {
			int lineThickness = (int) (lineFactor * scale);
			int linespacingThickness = (int) (linespacingFactor * scale);
			int anchorSize = NoteConstants.anchorType(anchorAbsIndex) == NoteConstants.ANCHOR_TYPE_LINE ? lineThickness : linespacingThickness;
			
			int dist = anchorAbsIndex-line0absIndex;
			int linesBetweenSize = ((dist+1)/2)*lineThickness;
			int spacesetweenSize = (dist/2)*linespacingThickness;

			int partDiff = 0;
			switch(part) {
			case BOTTOM_EDGE:
				partDiff = anchorSize;
				break;
			case MIDDLE:
				partDiff = anchorSize/2;
				break;
			}
			
			return (linesBetweenSize + spacesetweenSize) + (partDiff - (dist < 0 ? anchorSize : 0));
		}	

		public float getScale() {
			return scale;
		}
	
		public void setScale(float scale) {
			this.scale = scale;
		}

		public int getLineFactor() {
			return lineFactor;
		}

		public int getLinespacingFactor() {
			return linespacingFactor;
		}

		public int getLineThickness() {
			return (int) (lineFactor*scale);
		}
	
	}
