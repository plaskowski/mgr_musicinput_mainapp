package pl.edu.mimuw.students.pl249278.android.musicinput.component;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class LifecycleOneTimeCallback implements LifecycleEventObserver {

    private final Lifecycle.Event event;
    private final Runnable callback;

    public static void doWhenResumed(LifecycleOwner lifecycleOwner, Runnable callback) {
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        if (lifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            callback.run();
        } else {
            lifecycle.addObserver(new LifecycleOneTimeCallback(Lifecycle.Event.ON_RESUME, callback));
        }
    }

    private LifecycleOneTimeCallback(Lifecycle.Event event, Runnable callback) {
        this.event = event;
        this.callback = callback;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == this.event) {
            source.getLifecycle().removeObserver(this);
            callback.run();
        }
    }
}
