package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;

class BatteryManager {
    private static final int DEFAULT_VALUE = -1;
    private static final String TAG = BatteryManager.class.getSimpleName();
    private final Context context;

    BatteryManager(Context context) {
        this.context = context;
    }


    @Nullable
    BatteryStatus getCurrentValues() {
        Intent currentStatus = this.context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (currentStatus == null) {
            SensorDCLog.e(TAG, "Could not retrieve the current battery status");
            return BatteryStatus.None();
        }

        int status = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
        int chargePlug = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, DEFAULT_VALUE);
        int level = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
        int scale = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);

        BatteryStatus batteryStatus = new BatteryStatus();
        batteryStatus.setChargingOrFull(status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                        status == android.os.BatteryManager.BATTERY_STATUS_FULL);
        batteryStatus.setIsUSBCharge(chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_USB);
        batteryStatus.setIsACCharge(chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_AC);
        batteryStatus.setBatteryPercentage(level / (float) scale);
        return batteryStatus;
    }

    static class BatteryStatus {
        private Boolean isChargingOrFull;
        private Boolean isUSBCharge;
        private Boolean isACCharge;
        private float batteryPercentage;

        private BatteryStatus() {
        }

        private static BatteryStatus None() {
            BatteryStatus noStatus = new BatteryStatus();
            noStatus.setBatteryPercentage(Float.NaN);
            noStatus.setChargingOrFull(null);
            noStatus.setIsACCharge(null);
            noStatus.setIsUSBCharge(null);

            return noStatus;
        }

        private void setChargingOrFull(Boolean isChargingOrFull) {
            this.isChargingOrFull = isChargingOrFull;
        }

        Boolean getIsChargingOrFull() {
            return this.isChargingOrFull;
        }

        Boolean getIsUSBCharge() {
            return this.isUSBCharge;
        }

        private void setIsUSBCharge(Boolean isUSBCharge) {
            this.isUSBCharge = isUSBCharge;
        }

        Boolean getIsACCharge() {
            return this.isACCharge;
        }

        private void setIsACCharge(Boolean isACCharge) {
            this.isACCharge = isACCharge;
        }

        float getBatteryPercentage() {
            return this.batteryPercentage;
        }

        private void setBatteryPercentage(float batteryPercentage) {
            this.batteryPercentage = batteryPercentage;
        }
    }
}
