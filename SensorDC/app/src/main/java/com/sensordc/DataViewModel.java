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
        DeviceFactory factory = new DeviceFactory(context, settings);
        int minDistanceBetweenGPSUpdates = context.getResources().getInteger(R.integer.minDistanceBetweenGPSUpdates);
        int minTimeBetweenGPSUpdates = context.getResources().getInteger(R.integer.minTimeBetweenGPSUpdatesInMS);
//        int delay = context.getResources().getInteger(R.integer.sensorRecordingDelayInMS);
        int delay = 2000;
        this.sensorKit = factory.assembleSensorKit(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates, delay);

        sensorKit.updated.subscribe(new Action1<SensorKit>() {
            @Override
            public void call(SensorKit kit) {
                DataViewModel.this.update(kit);
            }
        });
    }

    private void update(SensorKit kit) {
        notifyPropertyChanged(BR.latestUpdate);
        notifyPropertyChanged(BR.linearAcceleration);
        notifyPropertyChanged(BR.rotation);
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
    public String getLinearAcceleration() {
        return "".concat(String.valueOf(this.sensorKit.linearAccelerationX)).concat("\t").concat(String.valueOf(this
                .sensorKit.linearAccelerationY)).concat("\t").concat(String.valueOf(this.sensorKit
                .linearAccelerationZ)).concat("\t");
    }

    @Bindable
    public String getRotation() {
        return "".concat(String.valueOf(this.sensorKit.rotationX)).concat("\t").concat(String.valueOf(this.sensorKit
                .rotationY)).concat("\t").concat(String.valueOf(this.sensorKit.rotationZ)).concat("\t").concat(String
                .valueOf(this.sensorKit.rotationScalar));
    }
}
