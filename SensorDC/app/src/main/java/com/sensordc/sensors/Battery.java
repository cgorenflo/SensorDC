package com.sensordc.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.sensordc.logging.SensorDCLog;

class Battery {
    private static final int DEFAULT_VALUE = -1;
    private static final String TAG = Battery.class.getSimpleName();
    private final Context context;
    private Boolean isChargingOrFull;
    private float batteryPercentage;

    Battery(Context context) {
        this.context = context;
        this.batteryPercentage = Float.NaN;
        this.isChargingOrFull = false;
    }

    void measure() {
        Intent currentStatus = this.context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (currentStatus == null) {
            SensorDCLog.e(TAG, "Could not retrieve the current battery status");
            return;
        }

        int status = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
        int level = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
        int scale = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);

        this.isChargingOrFull = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os
                .BatteryManager.BATTERY_STATUS_FULL;
        this.batteryPercentage = level / (float) scale;
    }


    Boolean getIsChargingOrFull() {
        return this.isChargingOrFull;
    }


    float getBatteryPercentage() {
        return this.batteryPercentage;
    }

}
