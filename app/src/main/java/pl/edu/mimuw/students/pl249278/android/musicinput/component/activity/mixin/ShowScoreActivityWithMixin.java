package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.mixin;

import android.os.Bundle;

import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ActivityStrategy;
import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.ShowScoreActivity;

public class ShowScoreActivityWithMixin extends ShowScoreActivity {

    protected ActivityStrategy mixin;

    protected void initMixin(ActivityStrategy mixin) {
        this.mixin = mixin;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mixin.onSaveInstanceState(outState, new ActivityStrategy.OnSaveInstanceStateSuperCall() {
            @Override
            public void onSaveInstanceState(Bundle outState) {
                ShowScoreActivityWithMixin.super.onSaveInstanceState(outState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mixin.onDestroy(new ActivityStrategy.OnDestroySuperCall() {
            @Override
            public void onDestroy() {
                ShowScoreActivityWithMixin.super.onDestroy();
            }
        });
    }

}
