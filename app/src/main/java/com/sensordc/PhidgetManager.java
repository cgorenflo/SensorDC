package com.sensordc;

import android.content.Context;

class PhidgetManager {
    private final Context context;
    private final Settings settings;

    PhidgetManager(Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
    }

    Context getContext() {
        return this.context;
    }

    Settings getSettings() {
        return this.settings;
    }
}
