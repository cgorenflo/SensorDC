package com.sensordc;

class SensorData {
    private static final Float CURRENT_INACTIVE_THRESHOLD = 50f;
    private static final Float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
    private static final Float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;
    int versionCode;
    String deviceID;
    Float gpsLatitude;
    Float gpsLongitude;
    Float linearAccelerationX;
    Float linearAccelerationY;
    Float linearAccelerationZ;
    Float rotationX;
    Float rotationY;
    Float rotationZ;
    Float rotationScalar;
    Float gpsAccuracy;
    Float batteryTemperature;
    Float ambientTemperature;
    Float voltage;
    Float current;
    Float dischargeCurrent;
    Float batteryPercentage;
    Boolean isUSBCharge;
    Boolean isACCharge;
    Boolean isChargingOrFull;

    static SensorData Initialize() {
        return new SensorData() {
            @Override
            boolean isInStandBy() {
                return false;
            }
        };
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
        return ((this.current.isNaN() && this.dischargeCurrent.isNaN()) ||
                (currentShowsStandBy && dischargeCurrentShowsStandBy));
    }
}
