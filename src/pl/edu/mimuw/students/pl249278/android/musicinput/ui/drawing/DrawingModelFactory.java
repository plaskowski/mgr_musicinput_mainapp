package pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing;

import android.content.Context;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.NoteSpec;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NoteConstants.NoteModifier;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.LoadingSvgException;
import pl.edu.mimuw.students.pl249278.android.musicinput.ui.NotePartFactory.NoteDescriptionLoadingException;

public class DrawingModelFactory {
	public static SheetAlignedElement createDrawingModel(Context ctx, ElementSpec elementSpec) throws NoteDescriptionLoadingException {
		switch (elementSpec.getType()) {
		case NOTE:
			NoteSpec noteSpec = ((ElementSpec.NormalNote) elementSpec).spec;
			NoteHeadElement head = new NoteHeadElement(ctx, noteSpec);
			SheetAlignedElement model = head;
			if(NoteConstants.hasStem(noteSpec)) {
				model = new NoteStemAndFlag(ctx, head);
			}
			try {
				if(noteSpec.hasDot()) {
					model = new Modifier.Suffix(ctx, model, noteSpec.positon(), NoteModifier.DOT);
				}
				NoteModifier toneModifier = noteSpec.getToneModifier();
				if(toneModifier != null) {
					model = new Modifier.Prefix(ctx, model, noteSpec.positon(), toneModifier);
				}
			} catch (LoadingSvgException e) {
				throw new RuntimeException(e);
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
		case PAUSE:
		case SPECIAL_SIGN:
		case FAKE_PAUSE:
		default:
			// TODO implement
			throw new UnsupportedOperationException();
				
		}
	}
}
