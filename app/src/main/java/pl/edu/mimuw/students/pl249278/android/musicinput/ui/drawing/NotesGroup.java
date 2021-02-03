package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.MIN_STEM_SPAN;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ORIENT_DOWN;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.ORIENT_UP;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote.SpacingSource;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams.AnchorPart;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.FloatMath;
import android.util.Log;

public class NotesGroup extends ElementsOverlay {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils(NotesGroup.class);
	private static final int MAX_NOTE_LENGTH = NoteConstants.LEN_QUATERNOTE+1;
	
	private SheetAlignedElement[] elements;
	private int[] xpositions;
	private int groupOrientation;

	private NotesGroup(int totalElements, int orientation) {
		elements = new SheetAlignedElement[totalElements];
		xpositions = new int[totalElements];
		Arrays.fill(xpositions, -1);
		this.groupOrientation = orientation;
	}
	
	private int buildIndex = 0;

	public void wrapNext(SheetAlignedElement model) {
		assert buildIndex < elements.length;
		wrapElement(buildIndex, model);
		buildIndex++;
	}
	public boolean hasNext() {
		return buildIndex < elements.length;
	}
	
	private void wrapElement(int index, SheetAlignedElement element) {
		elements[index] = element;
	}
	
	@Override
	public void setSheetParams(SheetVisualParams params) {
		super.setSheetParams(params);
		recalculate();
	}

	private Point start = new Point(), end = new Point(), mStemEndExtremum = new Point();
	private void recalculate() {
		assert(elements.length > 1);
		if(!isValid()) {
			makeEmpty();
			LogUtils.log(Log.VERBOSE, LogUtils.COMMON_TAG, "INVALID recalculate()");
			return;
		}
		
		float slopesSum = 0;
		int prevStemTop = stemTop(0);
		int prevStemMiddleX = absJMiddeX(0);
		for(int i = 1; i < elements.length; i++) {
			int stemTop = stemTop(i);
			int stemMiddleX = absJMiddeX(i);
			float slope = ((float) stemTop - prevStemTop)/(stemMiddleX - prevStemMiddleX);
			if(slope == 0 || (slope < 0 && slopesSum <= 0) || (slope > 0 && slopesSum >= 0)) {
				slopesSum += slope;
			} else {
				slopesSum = 0;
				break;
			}
			prevStemTop = stemTop;
			prevStemMiddleX = stemMiddleX;
		}
		slope = slopesSum / (elements.length-1);
		
		int sign = groupOrientation == ORIENT_DOWN ? 1 : -1;
		mStemEndExtremum.set(absJMiddeX(0), stemTop(0));
		int jlYextremum = joinLineY(0);
		for(int i = 1; i < elements.length; i++) {
			int middleX = absJMiddeX(i);
			int actualTop = (int) (slope*(middleX-mStemEndExtremum.x) + mStemEndExtremum.y);
			int minimumTop = stemTop(i);
			if(minimumTop*sign > actualTop*sign) {
				mStemEndExtremum.x = absJMiddeX(i);
				mStemEndExtremum.y = minimumTop;
			}
			int currentJLy = joinLineY(i);
			if(currentJLy*sign < jlYextremum*sign) {
				jlYextremum = currentJLy;
			}
		}
		
		int last = elements.length-1;
		int el0JLeft = (int) jLeft(0);
		start.x = 0;
		start.y = (int) (mStemEndExtremum.y - slope * (mStemEndExtremum.x - absJLLeft(0)));
		end.x = (xpositions[last] + (int) Math.ceil(jRight(last))) - (xpositions[0] + el0JLeft);
		end.y = (int) (slope * (absJRight(last) - mStemEndExtremum.x) + mStemEndExtremum.y);
		
		offset2line0 = Math.min(Math.min(
			start.y, end.y), jlYextremum
		);
		setPosition(xpositions[0] + el0JLeft, line0absTop + offset2line0);
		setMeasured((int) (end.x-start.x), (int) (Math.max(
			Math.abs(end.y-jlYextremum),
			Math.abs(start.y-jlYextremum)
		)));
		
		isDirty = true;
		onMeasureInvalidated();
	}
	
	private Path mPath = new Path();
	private boolean isDirty = true;
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		int translateY = -offset2line0;
		canvas.translate(0, translateY);
		if(isDirty) {
			mPath.reset();
			generateDrawingPath(mPath);
			isDirty = false;
		}
		canvas.drawPath(mPath, paint);
		canvas.translate(0, -translateY);
	}
	
	private void generateDrawingPath(Path path) {
		
		int sign = groupOrientation == ORIENT_UP ? 1 : -1;
		double dist = (double) Math.sqrt((start.x - end.x)*(start.x - end.x) + (start.y - end.y)*(start.y - end.y));
		int thickness = (int) (3.5 * sheetParams.getLineThickness() / Math.abs((end.x - start.x)/dist));
		int linesSpacing = 2 * sheetParams.getLineThickness();
		path.moveTo(end.x, end.y);
		path.lineTo(start.x, start.y);
		int absLeft = this.left();
		int lastElement = elements.length -1;
		int prevLength = MAX_NOTE_LENGTH;
		for (int i = 0; i <= lastElement; i++) {
			int posRelX = xpositions[i] - absLeft;
			float x1 = posRelX + jLeft(i);
			float x2 = posRelX + jRight(i);
			int y1 = start.y + (int) (slope*(x1-start.x)) + sign*thickness;
			int y2 = start.y + (int) (slope*(x2-start.x)) + sign*thickness;
			int jlY = joinLineY(i);
			path.lineTo(x1, y1);
			int currLength = length(i);
			int nextLength = i < lastElement ? length(i+1) : MAX_NOTE_LENGTH-1;
			if(currLength > prevLength && prevLength > nextLength) {
				float prevX2 = xpositions[i-1] - absLeft + jRight(i-1);
				// skip full-stems drawn by previous note
				path.rLineTo(0, (prevLength-MAX_NOTE_LENGTH)*sign*(linesSpacing+thickness));
				// draw half-stems that indicates that current note is "smaller" that previous
				for(int stemI = 0; stemI < currLength - prevLength; stemI++) {
					path.rLineTo(0, sign*linesSpacing);
					float dx = (prevX2 - x1)/2;
					float dy = dx*slope;
					path.rLineTo(dx, dy);
					path.rLineTo(0, sign*thickness);
					path.rLineTo(-dx, -dy);
				}
			}
			path.lineTo(x1, jlY);
			path.lineTo(x2, jlY);
			if(i < lastElement) {
				float nextX1 = xpositions[i+1] - absLeft + jLeft(i+1);
				int fullStems = Math.min(currLength, nextLength)-MAX_NOTE_LENGTH;
				int halfStems = nextLength >= prevLength ? Math.max(currLength-nextLength, 0) : 0;
				path.lineTo(x2, y2+sign*(halfStems+fullStems)*(thickness+linesSpacing));
				for(int stemI = 0; stemI < halfStems+fullStems; stemI++) {
					float dx = stemI >= halfStems ? nextX1-x2 : (nextX1-x2)/2;
					float dy = dx*slope;
					path.rLineTo(dx, dy);
					path.rLineTo(0, -sign*thickness);
					path.rLineTo(-dx, -dy);
					path.rLineTo(0, -sign*linesSpacing);
				}
			} else {
				path.lineTo(x2, y2);
			}
			prevLength = currLength;
		}
		path.close();
	}

	private boolean isValid() {
		int prevX = -1;
		for (int i = 0; i < elements.length; i++) {
			int x = xpositions[i];
			if(x == -1 || x <= prevX)
				return false;
			prevX = x;
		}
		return true;
	}

	private int length(int index) {
		return elements[index].getElementSpec().lengthSpec().length();
	}
	private int joinLineY(int index) {
		SheetAlignedElement current = elements[index];
		return current.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE) 
		+ current.getVerticalOffset(NoteHeadElement.JOINLINE_Y)
	    + (groupOrientation == ORIENT_DOWN ? -1 : 1)
		;
	}
	private int stemTop(int index) {
		SheetAlignedElement current = elements[index];
		int stemEndAnchor = current.getElementSpec().positonSpec().positon()+(groupOrientation == ORIENT_DOWN ? 1 : -1)*MIN_STEM_SPAN;
		return sheetParams.anchorOffset(stemEndAnchor, AnchorPart.MIDDLE);
	}
	private int absJMiddeX(int index) {
		return (absJRight(index)+absJLLeft(index))/2;
	}
	private int absJLLeft(int index) {
		return xpositions[index] + (int) jLeft(index);		
	}
	private int absJRight(int index) {
		return xpositions[index] + (int) Math.ceil(jRight(index));
	}
	private float jLeft(int index) {
		SheetAlignedElement el = elements[index];
		return el.getHorizontalOffset(NoteHeadElement.NOTEHEAD_LEFT)+el.getMetaValue(NoteHeadElement.METAVAL_JOINLINE_LEFT, 0);
	}
	private float jRight(int index) {
		SheetAlignedElement el = elements[index];
		return el.getHorizontalOffset(NoteHeadElement.NOTEHEAD_LEFT)+el.getMetaValue(NoteHeadElement.METAVAL_JOINLINE_RIGHT, 0);
	}
	
	private float slope;
	private int line0absTop;
	private int offset2line0;

	int lastMeasureUnit = -1;
	int minSpacingLength, maxSpacingLength;
	private int spacingLength(int index, int measureUnit) {
		if(measureUnit != lastMeasureUnit) {
			lastMeasureUnit = measureUnit;
			maxSpacingLength = ((NormalNote) elements[0].getElementSpec()).defaultSpacingLength(measureUnit);
			minSpacingLength = maxSpacingLength;
			for (int i = 1; i < elements.length; i++) {
				int elSpacing = ((NormalNote) elements[i].getElementSpec()).defaultSpacingLength(measureUnit);
				maxSpacingLength = Math.max(
					elSpacing, 
					maxSpacingLength
				);
				minSpacingLength = Math.min(
					elSpacing,
					minSpacingLength
				);
			}
		}
		return index < elements.length-1 ? minSpacingLength : maxSpacingLength;
	}
	
	@Override
	public void positionChanged(SheetAlignedElement element, int newX, int newY) {
		for (int i = 0; i < elements.length; i++) {
			if(elements[i] == element) {
				line0absTop = newY - element.getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
				xpositions[i] = newX;
				recalculate();
				return;
			}
		}
		throw new InvalidParameterException();
	}

	@Override
	public int elementsCount() {
		return elements.length;
	}
	
	@Override
	public SheetAlignedElement getElement(int elementIndex) {
		return elements[elementIndex];
	}

	@Override
	public int getOffsetToAnchor(int anchorAbsIndex, AnchorPart part) {
		throw new UnsupportedOperationException();
	}
	
	public static class GroupBuilder {
		private List<ElementSpec.NormalNote> specs = new ArrayList<ElementSpec.NormalNote>(5);
		private boolean closed = false;

		public GroupBuilder(ElementSpec firstElementSpec) {
			specs.add((NormalNote) firstElementSpec);
		}

		public static boolean canStartGroup(ElementSpec spec) {
			return isShortNote(spec)
			&& hasFlag(spec);
		}
		
		public static boolean canExtendGroup(ElementSpec spec) {
			return isShortNote(spec) && hasFlag(spec);
		}
		public static boolean couldExtendGroup(ElementSpec spec) {
			return isShortNote(spec);
		}

		public static boolean canEndGroup(ElementSpec spec) {
			return isShortNote(spec);
		}
		
		private static boolean hasFlag(ElementSpec spec) {
			return ((ElementSpec.NormalNote) spec).noteSpec().isGrouped();
		}

		private static boolean isShortNote(ElementSpec spec) {
			return spec.getType() == ElementType.NOTE 
			&& spec.lengthSpec().length() >= MAX_NOTE_LENGTH;
		}

		public boolean tryExtend(ElementSpec elementSpec) {
			if(!closed && isShortNote(elementSpec)) {
				specs.add((NormalNote) elementSpec);
				closed = !hasFlag(elementSpec);
				return true;
			}
			return false;
		}
		
		public boolean isValid() {
			return specs.size() > 1;
		}
		
		public int total() {
			return specs.size();
		}

		/**
		 * Modified accumulated ElementSpec-s accordingly to logic of GroupBuilder
		 * @return 
		 */
		public NotesGroup build() {
			assert isValid();
			/**
			 * czy wiązanie ma być na górze czy na dole,
			 * orient < 0,  wiązanie na dole
			 * wpp, wiązanie na górze
			 */
			NormalNote prev = specs.get(0), current;
			int orient = prev.getOrientation() == NoteConstants.ORIENT_DOWN ? -1 : 1;
			for (int i = 1; i < specs.size(); i++) {
				current = specs.get(i);
				orient += current.getOrientation() == NoteConstants.ORIENT_DOWN ? -1 : 1;
				prev = current;
			}
			int orientation = orient == 0 ? specs.get(0).getOrientation() : (orient < 0 ? ORIENT_DOWN : ORIENT_UP);
			NotesGroup result = new NotesGroup(specs.size(), orientation);
			
			// update specs
			for(int i = 0; i < specs.size(); i++) {
				NormalNote spec = specs.get(i);
				spec.setNoStem(true);
				spec.setOrientation(orientation);
				spec.setForcedSpacing(result.spacingSource(i));
			}
			return result;
		}
	}

	public SpacingSource spacingSource(final int i) {
		return new SpacingSource() {
			@Override
			public int spacingLength(int measureUnit) {
				return NotesGroup.this.spacingLength(i, measureUnit);
			}
		};
	}

}
