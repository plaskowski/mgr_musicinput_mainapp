package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.nature;

import pl.edu.mimuw.students.pl249278.android.musicinput.ui.drawing.SheetVisualParams;

public interface StaveHighlighter {

	void setParams(SheetVisualParams params);

	void setHiglightColor(int color);

	void highlightAnchor(Integer anchorAbsIndex);

}
