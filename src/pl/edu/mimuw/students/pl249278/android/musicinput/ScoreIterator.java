package pl.edu.mimuw.students.pl249278.android.musicinput;

import static pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark.BEGIN_REPEAT;
import static pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec.AdditionalMark.END_REPEAT;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreContentElem;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.TimeSpec;

public class ScoreIterator implements Iterator<ScoreContentElem> {
	private List<ScoreContentElem> content;
	
	public ScoreIterator(List<ScoreContentElem> content) {
		this.content = content;
	}

	private int index = 0;
	private Set<Integer> handledEnds = new HashSet<Integer>();
	private int jumpIndex = 0;
	private boolean jumpOnCurrentBarEnd;
	private int lastEndRepeatIndex = -1;

	private ScoreContentElem poppedAheadNext = null;
	@Override
	public boolean hasNext() {
		if(poppedAheadNext == null) {
			poppedAheadNext = mNext();
		}
		return poppedAheadNext != null;
	}
	
	public ScoreContentElem previewNext() {
		hasNext();
		return poppedAheadNext;
	}
	
	@Override
	public ScoreContentElem next() {
		ScoreContentElem result;
		if(poppedAheadNext != null) {
			result = poppedAheadNext;
			poppedAheadNext = null;
		} else {
			result = mNext();
			if(result == null) {
				throw new NoSuchElementException();
			}
		}
		return result;
	}
	
	private ScoreContentElem mNext() {
		int total = content.size();
		if(index < total) {
			ScoreContentElem elem = content.get(index);
			if(elem instanceof TimeSpec) {
				if(jumpOnCurrentBarEnd) {
					jumpOnCurrentBarEnd = false;
					index = jumpIndex;
					return next();
				}
				TimeSpec time = (TimeSpec) elem;
				if(time.hasMark(BEGIN_REPEAT)) {
					jumpIndex = index;
				}
				if(time.hasMark(END_REPEAT) && !handledEnds.contains(index)) {
					jumpOnCurrentBarEnd = true;
					handledEnds.add(index);
					lastEndRepeatIndex = index;
				} else {
					jumpOnCurrentBarEnd = false;
				}
			}
			index++;
			return elem;
		} else if(index == total && (
			jumpOnCurrentBarEnd 
			|| (
				!handledEnds.contains(index) 
				&& ((TimeSpec) content.get(jumpIndex)).hasMark(BEGIN_REPEAT)
				&& jumpIndex > lastEndRepeatIndex
			)
		)) {
			handledEnds.add(index);
			jumpOnCurrentBarEnd = false;
			index = jumpIndex;
			return next();
		}
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	
}
