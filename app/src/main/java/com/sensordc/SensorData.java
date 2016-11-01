package com.sensordc;

import java.util.ArrayList;

class SensorData {

    private int versionCode;
    private String imei;

    private float gpsLatitude;
    private float gpsLongitude;

    private float linearAccelerationX;
    private float linearAccelerationY;
    private float linearAccelerationZ;

    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float rotationScalar;
    private float gpsAccuracy;
    private float batteryTemperature;
    private float ambientTemperature;
    private float voltage;
    private float current;
    private float dischargeCurrent;
    private float batteryPercentage;
    private Boolean isUSBCharge;
    private Boolean isACCharge;
    private Boolean isChargingOrFull;

    public String toString() {
        StringBuilder sensorData = new StringBuilder(this.imei);

        ArrayList<String> sensorValues = new ArrayList<>();
        sensorValues.add(replaceNaNByNull(this.versionCode));
        sensorValues.add(replaceNaNByNull(this.gpsLatitude));
        sensorValues.add(replaceNaNByNull(this.gpsLongitude));
        sensorValues.add(replaceNaNByNull(this.gpsAccuracy));
        sensorValues.add(replaceNaNByNull(this.linearAccelerationX));
        sensorValues.add(replaceNaNByNull(this.linearAccelerationY));
        sensorValues.add(replaceNaNByNull(this.linearAccelerationZ));
        sensorValues.add(replaceNaNByNull(this.rotationX));
        sensorValues.add(replaceNaNByNull(this.rotationY));
        sensorValues.add(replaceNaNByNull(this.rotationZ));
        sensorValues.add(replaceNaNByNull(this.rotationScalar));
        sensorValues.add(replaceNaNByNull(this.batteryTemperature));
        sensorValues.add(replaceNaNByNull(this.ambientTemperature));
        sensorValues.add(replaceNaNByNull(this.voltage));
        sensorValues.add(replaceNaNByNull(this.current));
        sensorValues.add(replaceNaNByNull(this.dischargeCurrent));
        sensorValues.add(replaceNaNByNull(this.batteryPercentage));
        sensorValues.add(String.valueOf(this.isChargingOrFull));
        sensorValues.add(String.valueOf(this.isUSBCharge));
        sensorValues.add(String.valueOf(this.isACCharge));

        for (String value : sensorValues) {
            sensorData.append(",").append(value);
        }

        return sensorData.toString();
    }

    private String replaceNaNByNull(float value) {
        if (Float.isNaN(Math.abs(value)))
            return "";
        else
            return Float.toString(value);
    }

    boolean isInStandBy() {
        //TODO: implement
        return false;
    }

    void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    void setLinearAccelerationX(float accelerationX) {
        this.linearAccelerationX = accelerationX;
    }

    void setLinearAccelerationY(float accelerationY) {
        this.linearAccelerationY = accelerationY;
    }

    void setLinearAccelerationZ(float accelerationZ) {
        this.linearAccelerationZ = accelerationZ;
    }

    void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    void setRotationZ(float rotationZ) {
        this.rotationZ = rotationZ;
    }

    void setRotationScalar(float rotationScalar) {
        this.rotationScalar = rotationScalar;
    }

    void setGPSLatitude(float latitude) {
        this.gpsLatitude = latitude;
    }

    void setGPSLongitude(float longitude) {
        this.gpsLongitude = longitude;
    }

    void setGPSAccuracy(float accuracy) {
        this.gpsAccuracy = accuracy;
    }

    void setBatteryTemperature(float batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    void setAmbientTemperature(float ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    void setVoltage(float voltage) {
        this.voltage = voltage;
    }

    void setCurrent(float current) {
        this.current = current;
    }

    void setDischargeCurrent(float dischargeCurrent) {
        this.dischargeCurrent = dischargeCurrent;
    }

    void setBatteryPercentage(float batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    void setIsUSBCharge(Boolean isUSBCharge) {
        this.isUSBCharge = isUSBCharge;
    }

    void setIsACCharge(Boolean isACCharge) {
        this.isACCharge = isACCharge;
    }

    void setIsChargingOrFull(Boolean isChargingOrFull) {
        this.isChargingOrFull = isChargingOrFull;
    }

    void setIMEI(String IMEI) {
        this.imei = IMEI;
    }
}
