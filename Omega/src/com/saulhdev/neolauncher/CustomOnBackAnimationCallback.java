package com.saulhdev.neolauncher;

import android.window.BackEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public interface CustomOnBackAnimationCallback {

    default void onBackStarted(@NonNull BackEvent backEvent) {
    }

    @RequiresApi(34)
    default void onBackProgressed(@NonNull BackEvent backEvent) {
    }

    default void onBackProgressed(@NonNull Float event) {
    }

    default void onBackCancelled() {
    }

    void onBackInvoked();
}
