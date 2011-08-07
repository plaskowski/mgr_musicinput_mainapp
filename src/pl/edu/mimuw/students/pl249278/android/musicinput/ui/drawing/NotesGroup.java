package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.LINE0_ABSINDEX;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.MIN_STEM_SPAN;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ORIENT_DOWN;
import static pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.ORIENT_UP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pl.edu.mimuw.students.pl249278.android.common.LogUtils;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.SheetParams.AnchorPart;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.ElementType;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

public class NotesGroup extends AlignedElementWrapper<SheetAlignedElement> {
	@SuppressWarnings("unused")
	private static LogUtils log = new LogUtils(NotesGroup.class);
	private static int METAVAL_ORGINAL_SPACINGLEGTH = registerIndex();
	private static final int MAX_NOTE_LENGTH = NoteConstants.LEN_QUATERNOTE+1;
	
	private SheetAlignedElement[] elements;
	private int[] xpositions;
	private int groupOrientation;

	private NotesGroup(SheetAlignedElement firstElement, int totalElements, int orientation) {
		super(firstElement);
		elements = new SheetAlignedElement[totalElements];
		xpositions = new int[totalElements];
		Arrays.fill(xpositions, -1);
		this.groupOrientation = orientation;
		
		elements[0] =  firstElement;
	}
	
	private SheetAlignedElement wrapElement(int index, SheetAlignedElement elementToWrap) {
		SheetAlignedElement result = new ElementProxy(index, elementToWrap);
		elements[index] = result;
		return result;
	}
	
	private void onPositionChanged(int elementIndex, int newAbsoluteX) {
		if(xpositions[elementIndex] != newAbsoluteX) {
			xpositions[elementIndex] = newAbsoluteX;
			recalculate();
		}
	}

	@Override
	public void setSheetParams(SheetParams params) {
		super.setSheetParams(params);
		log.i("setSheetParams(), scale: %f", params.getScale());
		recalculate();
	}

	Point start = new Point(), end = new Point();
	private void recalculate() {
		assert(elements.length > 1);
		if(!isValid()) {
			calcNoVisibleWrapper();
			log.i("recalculate(), calculated when invalid %dx%d", totalWidth, totalHeight);
			return;
		}
		
		int sign = groupOrientation == ORIENT_DOWN ? 1 : -1;
		Point stemEndExtremum = new Point(absJMiddeX(0), stemTop(0));
		int jlYextremum = joinLineY(0);
		for(int i = 1; i < elements.length; i++) {
			int currentTop = stemTop(i);
			if(currentTop*sign > stemEndExtremum.y*sign) {
				stemEndExtremum.x = absJMiddeX(i);
				stemEndExtremum.y = currentTop;
			}
			int currentJLy = joinLineY(i);
			if(currentJLy*sign < jlYextremum*sign) {
				jlYextremum = currentJLy;
			}
		}
		int last = elements.length-1;
		slope = ((float) (stemTop(last)-stemTop(0)))/(absJRight(last)-absJLLeft(0));
		
		start.x = (int) jLeft(0);
		start.y = (int) (stemEndExtremum.y - slope * (stemEndExtremum.x - absJLLeft(0)));
		end.x = xpositions[last] - xpositions[0] + (int) Math.ceil(jRight(last));
		end.y = (int) (slope * (absJRight(last) - stemEndExtremum.x) + stemEndExtremum.y);
		
		int wrappedOffset2line0 = elements[0].getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		calcDrawOffsets(-start.x, (int) (wrappedOffset2line0 - Math.min(Math.min(
			start.y, end.y), jlYextremum
		)));
		calcSize((int) (end.x-start.x), (int) (Math.max(
			Math.abs(end.y-jlYextremum),
			Math.abs(start.y-jlYextremum)
		)));
		
		log.i("recalculate() result: size %dx%d elOffset %dx%d wrapperOffset %dx%d", 
			totalWidth, totalHeight,
			elementDrawOffset.x, elementDrawOffset.y,
			wrapperDrawOffset.x, wrapperDrawOffset.y
		);
		if(measureObserver != null) {
			measureObserver.onMeasurementInvalid();
		}
	}
	
	@Override
	public void onDraw(Canvas canvas, Paint paint) {
		super.onDraw(canvas, paint);
		
		// TODO wprowadzić oddzielny Paint dla wiązania
		
		int translateY = -getOffsetToAnchor(LINE0_ABSINDEX, AnchorPart.TOP_EDGE);
		canvas.translate(0, translateY);
		
		Path path = new Path();
		int sign = groupOrientation == ORIENT_UP ? 1 : -1;
		int thickness = 4 * sheetParams.getLineThickness() * sign;
		int linesSpacing = 2 * sheetParams.getLineThickness();
		path.moveTo(end.x, end.y);
		path.lineTo(start.x, start.y);
		int absLeft = xpositions[0]-elementDrawOffset.x;
		int lastElement = elements.length -1;
		int prevLength = MAX_NOTE_LENGTH;
		for (int i = 0; i <= lastElement; i++) {
			int posRelX = xpositions[i] - absLeft;
			float x1 = posRelX + jLeft(i);
			float x2 = posRelX + jRight(i);
			int y1 = start.y + (int) (slope*(x1-start.x)) + thickness;
			int y2 = start.y + (int) (slope*(x2-start.x)) + thickness;
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
		canvas.drawPath(path, paint);
		
		canvas.translate(0, -translateY);
	}

	private int length(int index) {
		return elements[index].getElementSpec().lengthSpec().length();
	}
	
	private boolean isValid() {
		int prevX = -1;
		for (int i = 0; i < elements.length; i++) {
			int x = xpositions[i];
			if(x == -1 || x <= prevX)
				return false;
		}
		return true;
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
	
	@Override
	public void positionChanged(int newAbsoluteX, int newAbsoluteY) {
		super.positionChanged(newAbsoluteX, newAbsoluteY);
		onPositionChanged(0, newAbsoluteX+elementDrawOffset.x);
	}
	
	private Observer measureObserver = null;

	private float slope;

	public void setMeasureObserver(Observer measureObserver) {
		this.measureObserver = measureObserver;
	}
	public interface Observer {
		void onMeasurementInvalid();
	}
	
	int lastMeasureUnit = -1;
	double minSpacingLength, maxSpacingLength;
	private double spacingLength(int index, int measureUnit) {
		if(measureUnit != lastMeasureUnit) {
			lastMeasureUnit = measureUnit;
			maxSpacingLength = elements[0].spacingLength(measureUnit);
			minSpacingLength = maxSpacingLength;
			for (int i = 1; i < elements.length; i++) {
				double elSpacing = elements[i].getMetaValue(METAVAL_ORGINAL_SPACINGLEGTH, measureUnit);
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
	public double spacingLength(int measureUnit) {
		return spacingLength(0, measureUnit);
	}
	
	private class ElementProxy extends AlignedElementWrapper<SheetAlignedElement> {
		private int index;

		public ElementProxy(int index, SheetAlignedElement wrappedElement) {
			super(wrappedElement);
			this.index = index;
		}
		
		@Override
		public void setSheetParams(SheetParams params) {
			super.setSheetParams(params);
			calcNoVisibleWrapper();
		}
		
		@Override
		public void positionChanged(int newAbsoluteX, int newAbsoluteY) {
			super.positionChanged(newAbsoluteX, newAbsoluteY);
			onPositionChanged(index, newAbsoluteX);
		}
		
		@Override
		public float getMetaValue(int valueIndentifier, int param) {
			if(valueIndentifier == METAVAL_ORGINAL_SPACINGLEGTH) {
				return (float) wrappedElement.spacingLength(param);
			} else {
				return super.getMetaValue(valueIndentifier, param);
			}
		}
		
		@Override
		public double spacingLength(int measureUnit) {
			return NotesGroup.this.spacingLength(index, measureUnit);
		}
		
	}
	
	public static class GroupBuilder {
		private List<ElementSpec.NormalNote> specs = new ArrayList<ElementSpec.NormalNote>(5);
		private boolean closed = false;
		private int buildIndex = -1;
		private int orientation = -1;
		private NotesGroup notesGroup;

		public GroupBuilder(SheetParams params, ElementSpec firstElementSpec) {
			specs.add((NormalNote) firstElementSpec);
			switch(params.getDisplayMode()) {
			case LOWER_VOICE:
				orientation = ORIENT_DOWN;
				break;
			case UPPER_VOICE:
				orientation = ORIENT_UP;
				break;
			}
		}

		public static boolean canStartGroup(ElementSpec spec) {
			return isShortNote(spec)
			&& hasFlag(spec);
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

		/**
		 * Modified accumulated ElementSpec-s accordingly to logic of GroupBuilder
		 */
		public void build() {
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
			// if not forced by external conditions
			if(this.orientation == -1) {
				this.orientation = orient == 0 ? specs.get(0).getOrientation() : (orient < 0 ? ORIENT_DOWN : ORIENT_UP);
			}
			if(specs.size() > 1) {
				// update specs
				for(int i = 0; i < specs.size(); i++) {
					NormalNote spec = specs.get(i);
					spec.setNoStem(true);
					spec.setOrientation(this.orientation);
				}
			}
			this.buildIndex = 0;
			
		}

		public boolean hasNext() {
			assert buildIndex >= 0;
			return buildIndex < specs.size();
		}

		/**
		 * @param model model that has been externally built from specs[buildIndex]
		 * @param groupObserver 
		 * @return wrapped model
		 */
		public SheetAlignedElement wrapNext(SheetAlignedElement model, Observer groupObserver) {
			assert buildIndex < specs.size();
			if(buildIndex == 0) {
				if(specs.size() > 1) {
					notesGroup = new NotesGroup(model, specs.size(), orientation);
					notesGroup.setMeasureObserver(groupObserver);
					model = notesGroup;
				}
			} else {
				model = notesGroup.wrapElement(buildIndex, model);
			}
			buildIndex++;
			return model;
		}
		
	}

}
