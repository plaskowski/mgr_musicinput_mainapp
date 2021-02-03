package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view.strategy;

import android.view.MotionEvent;
import android.view.View;

/**
 * {@link android.view.ViewGroup} protected methods used by strategies.
 */
public interface ViewGroupInternals {
    void setChildrenDrawingOrderEnabled(boolean enabled);
    View viewObject();

    boolean super_dispatchTouchEvent(MotionEvent ev);
    void super_setPadding(int left, int top, int right, int bottom);
}
