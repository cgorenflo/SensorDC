package com.sensordc;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import com.sensordc.sensors.DeviceFactory;
import com.sensordc.sensors.SensorKit;
import com.sensordc.settings.Settings;
import rx.functions.Action1;

import java.text.DateFormat;
import java.util.Date;

public class DataViewModel extends BaseObservable {

    private final SensorKit sensorKit;

    DataViewModel(Context context, Settings settings) {
        DeviceFactory factory = new DeviceFactory(context, settings, false);
        int minDistanceBetweenGPSUpdates = context.getResources().getInteger(R.integer.minDistanceBetweenGPSUpdates);
        int minTimeBetweenGPSUpdates = context.getResources().getInteger(R.integer.minTimeBetweenGPSUpdatesInMS);
        int delay = 2000;
        this.sensorKit = factory.assembleSensorKit(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates, delay);

        sensorKit.updated.subscribe(new Action1<SensorKit>() {
            @Override
            public void call(SensorKit kit) {
                DataViewModel.this.update();
            }
        });
    }

    private void update() {
        notifyPropertyChanged(BR.latestUpdate);
        notifyPropertyChanged(BR.linearAccelerationX);
        notifyPropertyChanged(BR.linearAccelerationY);
        notifyPropertyChanged(BR.linearAccelerationZ);
        notifyPropertyChanged(BR.rotationX);
        notifyPropertyChanged(BR.rotationY);
        notifyPropertyChanged(BR.rotationZ);
        notifyPropertyChanged(BR.rotationScalar);
        notifyPropertyChanged(BR.gpsLat);
        notifyPropertyChanged(BR.gpsLong);
        notifyPropertyChanged(BR.gpsAcc);
        notifyPropertyChanged(BR.batteryCharg);
        notifyPropertyChanged(BR.batteryPerc);
        notifyPropertyChanged(BR.box_Temperature);
        notifyPropertyChanged(BR.amb_Temperature);
        notifyPropertyChanged(BR.voltage);
        notifyPropertyChanged(BR.chargingCurrent);
        notifyPropertyChanged(BR.dischargeCurrent);
    }

    @Bindable
    public String getLatestUpdate() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    @Bindable
    public String getCodeVersion() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }

    @Bindable
    public String getLinearAccelerationX() {
        return String.valueOf(this.sensorKit.getLinearAccelerationX());
    }

    @Bindable
    public String getLinearAccelerationY() {
        return String.valueOf(this.sensorKit.getLinearAccelerationX());
    }

    @Bindable
    public String getLinearAccelerationZ() {
        return String.valueOf(this.sensorKit.getLinearAccelerationZ());
    }

    @Bindable
    public String getRotationX() {
        return String.valueOf(this.sensorKit.getRotationX());
    }

    @Bindable
    public String getRotationY() {
        return String.valueOf(this.sensorKit.getRotationY());
    }

    @Bindable
    public String getRotationZ() {
        return String.valueOf(this.sensorKit.getRotationZ());
    }

    @Bindable
    public String getRotationScalar() {
        return String.valueOf(this.sensorKit.getRotationScalar());
    }

    @Bindable
    public String getGpsLat() {
        return String.valueOf(this.sensorKit.getGpsLatitude());
    }

    @Bindable
    public String getGpsLong() {
        return String.valueOf(this.sensorKit.getGpsLongitude());
    }

    @Bindable
    public String getGpsAcc() {
        return String.valueOf(this.sensorKit.getGpsAccuracy());
    }

    @Bindable
    public String getBatteryPerc() {
        return String.valueOf(this.sensorKit.getBatteryPercentage());
    }

    @Bindable
    public String getBatteryCharg() {
        if (sensorKit.isChargingOrFull()) {
            return "charging or full";
        } else
            return "not charging";
    }

    @Bindable
    public String getVoltage() {
        return String.valueOf(sensorKit.getVoltage());
    }

    @Bindable
    public String getChargingCurrent() {
        return String.valueOf(sensorKit.getCurrent());
    }

    @Bindable
    public String getDischargeCurrent() {
        return String.valueOf(sensorKit.getDischargeCurrent());
    }

    @Bindable
    public String getAmb_Temperature() {
        return String.valueOf(sensorKit.getAmbientTemperature());
    }

    @Bindable
    public String getBox_Temperature() {
        return String.valueOf(sensorKit.getBatteryTemperature());
    }
}
