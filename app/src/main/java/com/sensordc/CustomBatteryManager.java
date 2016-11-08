package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.Nullable;

class CustomBatteryManager {
    private static final int DEFAULT_VALUE = -1;
    private static final String TAG = CustomBatteryManager.class.getSimpleName();
    private final Context context;

    CustomBatteryManager(Context context) {
        this.context = context;
    }


    @Nullable
    BatteryStatus getCurrentValues() {
        Intent currentStatus = this.context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (currentStatus == null) {
            SensorDCLog.e(TAG, "Could not retrieve the current battery status");
            return null;
        }

        int status = currentStatus.getIntExtra(BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
        int chargePlug = currentStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, DEFAULT_VALUE);
        int level = currentStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
        int scale = currentStatus.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);

        BatteryStatus batteryStatus = new BatteryStatus();
        batteryStatus.setChargingOrFull(
                status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        batteryStatus.setIsUSBCharge(chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
        batteryStatus.setIsACCharge(chargePlug == BatteryManager.BATTERY_PLUGGED_AC);
        batteryStatus.setBatteryPercentage(level / (float) scale);
        return batteryStatus;
    }
}
