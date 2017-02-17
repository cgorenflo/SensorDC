package com.sensordc.sensors;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import com.sensordc.logging.SensorDCLog;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

class Battery {
    private static final int DEFAULT_VALUE = -1;
    private static final String TAG = Battery.class.getSimpleName();
    private final Context context;
    public Observable<Battery> updated;
    private Boolean isChargingOrFull;
    private Boolean isUSBCharge;
    private Boolean isACCharge;
    private float batteryPercentage;

    Battery(Context context) {
        this.context = context;
        this.setBatteryPercentage(Float.NaN);
        this.setChargingOrFull(null);
        this.setIsACCharge(null);
        this.setIsUSBCharge(null);
    }

    void initialize(final long measurementDelay) {
        updated = Observable.defer(new Func0<Observable<Battery>>() {
            @Override
            public Observable<Battery> call() {
                return Observable.interval(measurementDelay, TimeUnit.MILLISECONDS).flatMap(new Func1<Long,
                        Observable<Battery>>() {
                    @Override
                    public Observable<Battery> call(Long interval) {
                        return measure();
                    }
                });
            }
        });
    }

    @Nullable
    private Observable<Battery> measure() {
        Intent currentStatus = this.context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (currentStatus == null) {
            SensorDCLog.e(TAG, "Could not retrieve the current battery status");
            return Observable.just(this);
        }

        int status = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, DEFAULT_VALUE);
        int chargePlug = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, DEFAULT_VALUE);
        int level = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, DEFAULT_VALUE);
        int scale = currentStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, DEFAULT_VALUE);

        this.setChargingOrFull(status == android.os.BatteryManager.BATTERY_STATUS_CHARGING || status == android.os
                .BatteryManager.BATTERY_STATUS_FULL);
        this.setIsUSBCharge(chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_USB);
        this.setIsACCharge(chargePlug == android.os.BatteryManager.BATTERY_PLUGGED_AC);
        this.setBatteryPercentage(level / (float) scale);
        return Observable.just(this);
    }


    public Boolean getIsChargingOrFull() {
        return this.isChargingOrFull;
    }

    public Boolean getIsUSBCharge() {
        return this.isUSBCharge;
    }

    private void setIsUSBCharge(Boolean isUSBCharge) {
        this.isUSBCharge = isUSBCharge;
    }

    public Boolean getIsACCharge() {
        return this.isACCharge;
    }

    private void setIsACCharge(Boolean isACCharge) {
        this.isACCharge = isACCharge;
    }

    private void setChargingOrFull(Boolean isChargingOrFull) {
        this.isChargingOrFull = isChargingOrFull;
    }

    public float getBatteryPercentage() {
        return this.batteryPercentage;
    }

    private void setBatteryPercentage(float batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }
}
