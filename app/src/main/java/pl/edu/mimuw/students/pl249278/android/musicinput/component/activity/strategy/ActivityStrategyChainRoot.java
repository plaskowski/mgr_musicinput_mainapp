package pl.edu.mimuw.students.pl249278.android.musicinput.component.activity.strategy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ActivityStrategyChainRoot implements ActivityStrategy {
    private final android.app.Activity target;

    public ActivityStrategyChainRoot(Activity target) {
        this.target = target;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, OnSaveInstanceStateSuperCall superCall) {
        superCall.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy(OnDestroySuperCall superCall) {
        superCall.onDestroy();
    }

    @Override
    public void onCustomEvent(CustomEventInterface customEvent) {
        // here it ends
    }

    @Override
    public ActivityStrategyCallbacks callbacks() {
        return new ActivityStrategyCallbacks() {
            @Override
            public void runOnUiThread(Runnable action) {
                target.runOnUiThread(action);
            }

            @Override
            public void finish() {
                target.finish();
            }

            @Override
            public android.app.Activity getContext() {
                return target;
            }

            @Override
            public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    return target.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
                } else {
                    return target.registerReceiver(receiver, filter);
                }
            }

            @Override
            public void unregisterReceiver(BroadcastReceiver receiver) {
                target.unregisterReceiver(receiver);
            }

            @Override
            public SharedPreferences getSharedPreferences(String name, int mode) {
                return target.getSharedPreferences(name, mode);
            }

            @Override
            public FragmentManager getSupportFragmentManager() {
                return ((FragmentActivity) target).getSupportFragmentManager();
            }
        };
    }

}
