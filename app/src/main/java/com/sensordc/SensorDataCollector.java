package com.sensordc;

import android.telephony.TelephonyManager;

class SensorDataCollector {
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
        this.phoneSensors.initialize(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        this.phidgetSensors.initialize();

        this.isInitialized = true;
    }

    void stop() {
        this.phoneSensors.stop();
        this.phidgetSensors.stop();
    }

    Boolean areAllUpdated() {
        return this.phoneSensors.areAllUpdated() && this.phidgetSensors.areAllUpdated();
    }

    SensorData getCurrentSensorData() {
        if (!this.isInitialized) {
            return null;
        }

        int versionCode = BuildConfig.VERSION_CODE;
        SensorData sensorData = new SensorData();
        sensorData.setVersionCode(versionCode);
        sensorData.setIMEI(getIMEI());

        float[] accelerationValues = this.phoneSensors.getLinearAcceleration();
        sensorData.setLinearAccelerationX(accelerationValues[0]);
        sensorData.setLinearAccelerationY(accelerationValues[1]);
        sensorData.setLinearAccelerationZ(accelerationValues[2]);

        float[] rotationValues = this.phoneSensors.getRotationVector();
        sensorData.setRotationX(rotationValues[0]);
        sensorData.setRotationY(rotationValues[1]);
        sensorData.setRotationZ(rotationValues[2]);
        sensorData.setRotationScalar(rotationValues[3]);

        float[] locationValues = this.phoneSensors.getLocation();
        sensorData.setGPSLatitude(locationValues[0]);
        sensorData.setGPSLongitude(locationValues[1]);
        sensorData.setGPSAccuracy(locationValues[2]);

        BatteryStatus batteryStatus = this.phoneSensors.getBatteryStatus();
        sensorData.setBatteryPercentage(batteryStatus.getBatteryPercentage());
        sensorData.setIsUSBCharge(batteryStatus.getIsUSBCharge());
        sensorData.setIsACCharge(batteryStatus.getIsACCharge());
        sensorData.setIsChargingOrFull(batteryStatus.getIsChargingOrFull());

        sensorData.setBatteryTemperature(this.phidgetSensors.getBatteryTemperature());
        sensorData.setAmbientTemperature(this.phidgetSensors.getAmbientTemperature());
        sensorData.setVoltage(this.phidgetSensors.getVoltage());
        sensorData.setCurrent(this.phidgetSensors.getCurrent());
        sensorData.setDischargeCurrent(this.phidgetSensors.getDischargeCurrent());

        return sensorData;
    }

    private String getIMEI() {
        return this.imei != null ? this.imei : (this.imei = this.telephonyManager.getDeviceId());
    }
}
