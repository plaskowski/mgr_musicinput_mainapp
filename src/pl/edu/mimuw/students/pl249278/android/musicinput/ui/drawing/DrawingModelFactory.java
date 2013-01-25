package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.NormalNote;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.NotePartFactory.NoteDescriptionLoadingException;
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
		SheetAlignedElement model;
		switch (elementSpec.getType()) {
		case NOTE:
			try {
				NormalNote note = (ElementSpec.NormalNote) elementSpec;
				NoteSpec noteSpec = note.noteSpec();
				NoteHeadElement head = new NoteHeadElement(ctx, note);
				model = head;
				if(NoteConstants.hasStem(noteSpec.length())) {
					model = new NoteStemAndFlag(ctx, head, note.hasNoStem());
				}
				for(int i = 0; i < noteSpec.dotExtension(); i++) {
					model = new Modifier.Suffix(ctx, model, noteSpec.positon(), ElementModifier.DOT);
				}
				NoteModifier toneModifier = noteSpec.getToneModifier();
				if(toneModifier != null) {
					model = new Modifier.Prefix(ctx, model, noteSpec.positon(), ElementModifier.map(toneModifier));
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
		case TIMES_DIVIDER:
			try {
				return new TimeDivider(ctx, (pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.ElementSpec.TimeDivider) elementSpec);
			} catch (LoadingSvgException e) {
				throw new CreationException(e, elementSpec);
			}
		case FAKE_PAUSE:
		case PAUSE:
			try {
				ElementSpec.Pause pauseSpec = (ElementSpec.Pause) elementSpec;
				model = new PauseElement(ctx, pauseSpec);
				for(int i = 0; i < pauseSpec.lengthSpec().dotExtension(); i++) {
					model = new PauseElement.PauseDot(ctx, model);
				}
				return model;
			} catch (LoadingSvgException e) {
				throw new CreationException(e, elementSpec);
			}
		default:
			throw new UnsupportedOperationException("Unhandled type: "+elementSpec.getType().name());
				
		}
	}
}
