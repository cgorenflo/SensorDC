package com.sensordc;

import android.telephony.TelephonyManager;
import com.sensordc.BatteryManager.BatteryStatus;

class SensorDataCollector {
    private static final String TAG = SensorDataCollector.class.getSimpleName();
    private final PhoneSensors phoneSensors;
    private final PhidgetSensors phidgetSensors;
    private final TelephonyManager telephonyManager;
    private boolean isInitialized;
    private String imei;

    SensorDataCollector(TelephonyManager telephonyManager, PhoneSensors phoneSensors, PhidgetSensors phidgetSensors) {
        this.telephonyManager = telephonyManager;
        this.phoneSensors = phoneSensors;
        this.phidgetSensors = phidgetSensors;

        this.isInitialized = false;
    }

    void initializeSensors(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates) {
        SensorDCLog.i(TAG, "Initializing sensor listeners.");
        this.phoneSensors.initialize(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        this.phidgetSensors.initialize();

        this.isInitialized = true;
    }

    void stop() {
        SensorDCLog.i(TAG, "Unregistering sensor listeners.");
        this.phoneSensors.stop();
        this.phidgetSensors.stop();
    }

    SensorData getCurrentSensorData() {
        SensorDCLog.i(TAG, "Retrieving sensor data.");
        if (!this.isInitialized) {
            return null;
        }

        int versionCode = BuildConfig.VERSION_CODE;
        SensorData sensorData = new SensorData();
        sensorData.versionCode = versionCode;
        sensorData.deviceID = getIMEI();

        float[] accelerationValues = this.phoneSensors.getLinearAcceleration();
        sensorData.linearAccelerationX = accelerationValues[0];
        sensorData.linearAccelerationY = accelerationValues[1];
        sensorData.linearAccelerationZ = accelerationValues[2];

        float[] rotationValues = this.phoneSensors.getRotationVector();
        sensorData.rotationX = rotationValues[0];
        sensorData.rotationY = rotationValues[1];
        sensorData.rotationZ = rotationValues[2];
        sensorData.rotationScalar = rotationValues[3];

        float[] locationValues = this.phoneSensors.getLocation();
        sensorData.gpsLatitude = locationValues[0];
        sensorData.gpsLongitude = locationValues[1];
        sensorData.gpsAccuracy = locationValues[2];

        BatteryStatus batteryStatus = this.phoneSensors.getBatteryStatus();
        sensorData.batteryPercentage = batteryStatus.getBatteryPercentage();
        sensorData.isUSBCharge = batteryStatus.getIsUSBCharge();
        sensorData.isACCharge = batteryStatus.getIsACCharge();
        sensorData.isChargingOrFull = batteryStatus.getIsChargingOrFull();

        sensorData.batteryTemperature = this.phidgetSensors.getBatteryTemperature();
        sensorData.ambientTemperature = this.phidgetSensors.getAmbientTemperature();
        sensorData.voltage = this.phidgetSensors.getVoltage();
        sensorData.current = this.phidgetSensors.getCurrent();
        sensorData.dischargeCurrent = this.phidgetSensors.getDischargeCurrent();

        return sensorData;
    }

    private String getIMEI() {
        return this.imei != null ? this.imei : (this.imei = this.telephonyManager.getDeviceId());
    }
}
