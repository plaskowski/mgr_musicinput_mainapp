package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import javax.annotation.Nullable;

public class ViewInflationContext {
    public final Context context;
    public final AttributeSet attrs;
    @Nullable
    public final Integer defStyle;

    public ViewInflationContext(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ViewInflationContext(Context context, AttributeSet attrs, Integer defStyle) {
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
    }

}
