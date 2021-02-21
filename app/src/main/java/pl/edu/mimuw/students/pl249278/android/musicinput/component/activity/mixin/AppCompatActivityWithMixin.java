package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.mixin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy.ActivityStrategy;

public class AppCompatActivityWithMixin extends AppCompatActivity {

    protected ActivityStrategy mixin;

    protected void initMixin(ActivityStrategy mixin) {
        this.mixin = mixin;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mixin.onSaveInstanceState(outState, new ActivityStrategy.OnSaveInstanceStateSuperCall() {
            @Override
            public void onSaveInstanceState(Bundle outState) {
                AppCompatActivityWithMixin.super.onSaveInstanceState(outState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mixin.onDestroy(new ActivityStrategy.OnDestroySuperCall() {
            @Override
            public void onDestroy() {
                AppCompatActivityWithMixin.super.onDestroy();
            }
        });
    }
}
