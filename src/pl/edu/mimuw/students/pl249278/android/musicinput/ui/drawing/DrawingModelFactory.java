package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.FakePause;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import android.content.Context;

public class DrawingModelFactory {
	@SuppressWarnings("serial")
	public static class CreationException extends Exception {
		ElementSpec spec;
		public CreationException(Throwable throwable, ElementSpec elementSpec) {
			super(throwable);
			this.spec = elementSpec;
		}
	}
	
	public static SheetAlignedElement createDrawingModel(Context ctx, ElementSpec elementSpec) throws CreationException {
		switch (elementSpec.getType()) {
		case NOTE:
			try {
				NormalNote note = (ElementSpec.NormalNote) elementSpec;
				NoteSpec noteSpec = note.noteSpec();
				NoteHeadElement head = new NoteHeadElement(ctx, note);
				SheetAlignedElement model = head;
				if(!note.hasNoStem() && NoteConstants.hasStem(noteSpec.length())) {
					model = new NoteStemAndFlag(ctx, head);
				}
				if(noteSpec.hasDot()) {
					model = new Modifier.Suffix(ctx, model, noteSpec.positon(), NoteModifier.DOT);
				}
				NoteModifier toneModifier = noteSpec.getToneModifier();
				if(toneModifier != null) {
					model = new Modifier.Prefix(ctx, model, noteSpec.positon(), toneModifier);
				}
				int nearestLine = NoteConstants.anchorTypedIndex(noteSpec.positon());
				if(NoteConstants.anchorType(noteSpec.positon()) == NoteConstants.ANCHOR_TYPE_LINESPACE
					&& nearestLine < 0) {
					nearestLine += 1;
				}
				if(nearestLine < 0 || nearestLine > 4) {
					model = new AddedLine(model, nearestLine);
				}
				return model;
			} catch (LoadingSvgException e) {
				throw new CreationException(e, elementSpec);
			} catch (NoteDescriptionLoadingException e) {
				throw new CreationException(e, elementSpec);
			}
		case FAKE_PAUSE:
			return new FakePauseElement((FakePause) elementSpec);
		case TIMES_DIVIDER:
			try {
				return new TimeDivider(ctx, (pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.TimeDivider) elementSpec);
			} catch (LoadingSvgException e) {
				throw new CreationException(e, elementSpec);
			}
		case PAUSE:
			try {
				return new PauseElement(ctx, (ElementSpec.Pause) elementSpec);
			} catch (LoadingSvgException e) {
				throw new CreationException(e, elementSpec);
			}
		case SPECIAL_SIGN:
		default:
			// TODO implement
			throw new UnsupportedOperationException("Unhandled type: "+elementSpec.getType().name());
				
		}
	}
}
