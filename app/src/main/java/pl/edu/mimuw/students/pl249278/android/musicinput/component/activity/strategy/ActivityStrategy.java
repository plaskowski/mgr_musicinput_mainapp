package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentManager;

/**
 * See {@link android.app.Activity}.
 */
public interface ActivityStrategy {

    ActivityStrategyCallbacks callbacks();

    void onSaveInstanceState(Bundle outState, OnSaveInstanceStateSuperCall superCall);

    void onDestroy(OnDestroySuperCall superCall);

    void onCustomEvent(CustomEventInterface customEvent);

    interface OnSaveInstanceStateSuperCall {
        void onSaveInstanceState(Bundle outState);
    }

    interface OnDestroySuperCall {
        void onDestroy();
    }

    /**
     * Methods from {@link android.app.Activity} required by {@link ActivityStrategy}.
     */
    interface ActivityStrategyCallbacks {
        void runOnUiThread(Runnable action);

        void finish();

        android.app.Activity getContext();

        Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter);

        void unregisterReceiver(BroadcastReceiver receiver);

        SharedPreferences getSharedPreferences(String name, int mode);

        FragmentManager getSupportFragmentManager();
    }

}
