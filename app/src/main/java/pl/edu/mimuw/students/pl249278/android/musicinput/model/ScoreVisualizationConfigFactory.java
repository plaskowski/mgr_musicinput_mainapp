package pl.edu.mimuw.students.pl249278.android.musicinput.model;

import pl.edu.mimuw.students.pl249278.android.musicinput.R;
import pl.edu.mimuw.students.pl249278.android.musicinput.model.ScoreVisualizationConfig.DisplayMode;
import android.content.Context;

public class ScoreVisualizationConfigFactory {
	public static ScoreVisualizationConfig createWithDefaults(Context ctx) {
		return new ScoreVisualizationConfig(
			DisplayMode.NORMAL,
			ctx.getResources().getInteger(R.integer.minSpaceDefault),
			ctx.getResources().getInteger(R.integer.maxSpaceDefault)
		);
	}
}
