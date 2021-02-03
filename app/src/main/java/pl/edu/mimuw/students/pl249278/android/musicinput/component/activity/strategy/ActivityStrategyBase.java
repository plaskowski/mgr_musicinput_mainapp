package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import android.os.Bundle;

public class ActivityStrategyBase implements ActivityStrategy {

    private final ActivityStrategy parent;

    public ActivityStrategyBase(ActivityStrategy parent) {
        this.parent = parent;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, OnSaveInstanceStateSuperCall superCall) {
        parent.onSaveInstanceState(outState, superCall);
    }

    @Override
    public void onDestroy(OnDestroySuperCall superCall) {
        parent.onDestroy(superCall);
    }

    @Override
    public void onCustomEvent(CustomEventInterface customEvent) {
        parent.onCustomEvent(customEvent);
    }

    @Override
    public ActivityStrategyCallbacks callbacks() {
        return parent.callbacks();
    }

    protected android.app.Activity getContext() {
        return callbacks().getContext();
    }

}
