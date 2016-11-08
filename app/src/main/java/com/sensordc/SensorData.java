package com.sensordc;

import java.util.ArrayList;

class SensorData {
    private static final Float CURRENT_INACTIVE_THRESHOLD = 50f;
    private static final Float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
    private static final Float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;
    private int versionCode;
    private String deviceID;
    private Float gpsLatitude;
    private Float gpsLongitude;
    private Float linearAccelerationX;
    private Float linearAccelerationY;
    private Float linearAccelerationZ;
    private Float rotationX;
    private Float rotationY;
    private Float rotationZ;
    private Float rotationScalar;
    private Float gpsAccuracy;
    private Float batteryTemperature;
    private Float ambientTemperature;
    private Float voltage;
    private Float current;
    private Float dischargeCurrent;
    private Float batteryPercentage;
    private Boolean isUSBCharge;
    private Boolean isACCharge;
    private Boolean isChargingOrFull;

    static SensorData Initialize() {
        return new SensorData() {
            @Override
            boolean isInStandBy() {
                return false;
            }
        };
    }

    public String toString() {
        StringBuilder sensorData = new StringBuilder(this.deviceID);

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

        boolean currentShowsStandBy = this.current != Float.NaN && this.current <= CURRENT_INACTIVE_THRESHOLD;
        //this.dischargeCurrent != Float.NaN is erroneously interpreted as constant by the lint
        //noinspection ConstantConditions
        boolean dischargeCurrentShowsStandBy = this.dischargeCurrent != Float.NaN &&
                                               this.dischargeCurrent >= DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD &&
                                               this.dischargeCurrent <= DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD;

        //first part is erroneously interpreted as constant by the lint
        //noinspection ConstantConditions
        return ((this.current == Float.NaN && this.dischargeCurrent == Float.NaN) ||
                (currentShowsStandBy && dischargeCurrentShowsStandBy));
    }

    void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    void setLinearAccelerationX(Float accelerationX) {
        this.linearAccelerationX = accelerationX;
    }

    void setLinearAccelerationY(Float accelerationY) {
        this.linearAccelerationY = accelerationY;
    }

    void setLinearAccelerationZ(Float accelerationZ) {
        this.linearAccelerationZ = accelerationZ;
    }

    void setRotationX(Float rotationX) {
        this.rotationX = rotationX;
    }

    void setRotationY(Float rotationY) {
        this.rotationY = rotationY;
    }

    void setRotationZ(Float rotationZ) {
        this.rotationZ = rotationZ;
    }

    void setRotationScalar(Float rotationScalar) {
        this.rotationScalar = rotationScalar;
    }

    void setGPSLatitude(Float latitude) {
        this.gpsLatitude = latitude;
    }

    void setGPSLongitude(Float longitude) {
        this.gpsLongitude = longitude;
    }

    void setGPSAccuracy(Float accuracy) {
        this.gpsAccuracy = accuracy;
    }

    void setBatteryTemperature(Float batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    void setAmbientTemperature(Float ambientTemperature) {
        this.ambientTemperature = ambientTemperature;
    }

    void setVoltage(Float voltage) {
        this.voltage = voltage;
    }

    void setCurrent(Float current) {
        this.current = current;
    }

    void setDischargeCurrent(Float dischargeCurrent) {
        this.dischargeCurrent = dischargeCurrent;
    }

    void setBatteryPercentage(Float batteryPercentage) {
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

    void setIMEI(String imei) {
        this.deviceID = imei;
    }
}
