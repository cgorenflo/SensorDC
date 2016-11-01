package com.sensordc;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

import java.util.Locale;

class PhoneSensors {
    private static final String TAG = PhoneSensors.class.getSimpleName();
    private final SensorManager sensorManager;
    private final LocationManager locationManager;
    private final CustomBatteryManager batteryManager;
    private CustomSensorEventListener linearAccelerationListener;
    private CustomSensorEventListener rotationVectorListener;
    private CustomLocationListener locationListener;
    private TelephonyManager telephonyManager;

    PhoneSensors(SensorManager sensorManager, LocationManager locationManager, CustomBatteryManager batteryManager,
                 TelephonyManager telephonyManager) {

        this.sensorManager = sensorManager;
        this.locationManager = locationManager;
        this.batteryManager = batteryManager;
        this.telephonyManager = telephonyManager;
    }

    private void registerSensorListener(Sensor sensor, SensorEventListener listener, int sensorType) {
        if (sensor != null) {
            this.sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            SensorDCLog.e(TAG,
                    String.format(Locale.CANADA, "The sensor of type %d could not be initialized", sensorType));
        }
    }

    void initialize(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates) {
        Sensor linearAcceleration = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor rotationVector = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        this.linearAccelerationListener = new CustomSensorEventListener();
        this.rotationVectorListener = new CustomSensorEventListener();
        this.locationListener = new CustomLocationListener();

        registerSensorListener(linearAcceleration, this.linearAccelerationListener, Sensor.TYPE_LINEAR_ACCELERATION);
        registerSensorListener(rotationVector, this.rotationVectorListener, Sensor.TYPE_ROTATION_VECTOR);

        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeBetweenGPSUpdates,
                minDistanceBetweenGPSUpdates, this.locationListener);
    }

    void stop() {
        this.sensorManager.unregisterListener(this.linearAccelerationListener);
        this.sensorManager.unregisterListener(this.rotationVectorListener);
        this.locationManager.removeUpdates(this.locationListener);
    }

    boolean areAllUpdated() {
        return this.linearAccelerationListener.hasBeenUpdatedSinceLastRetrieval() && this.rotationVectorListener
                .hasBeenUpdatedSinceLastRetrieval() && this.locationListener.hasBeenUpdatedSinceLastRetrieval();
    }

    float[] getLinearAcceleration() {
        return this.linearAccelerationListener.getCurrentValues().getValues();
    }

    float[] getRotationVector() {
        return this.rotationVectorListener.getCurrentValues().getValues();
    }

    float[] getLocation() {
        return this.locationListener.getCurrentValues().getValues();
    }

    BatteryStatus getBatteryStatus() {
        return this.batteryManager.getCurrentValues();
    }

    String getIMEI() {
        return this.telephonyManager.getDeviceId();
    }
}
