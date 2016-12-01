package com.sensordc;

class SensorData {
    private static final float CURRENT_INACTIVE_THRESHOLD = 50f;
    private static final float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
    private static final float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;
    int versionCode;
    String deviceID;
    float gpsLatitude;
    float gpsLongitude;
    float linearAccelerationX;
    float linearAccelerationY;
    float linearAccelerationZ;
    float rotationX;
    float rotationY;
    float rotationZ;
    float rotationScalar;
    float gpsAccuracy;
    float batteryTemperature;
    float ambientTemperature;
    float voltage;
    float current;
    float dischargeCurrent;
    float batteryPercentage;
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

        boolean currentShowsStandBy = !Float.isNaN(this.current) && this.current <= CURRENT_INACTIVE_THRESHOLD;

        boolean dischargeCurrentShowsStandBy = !Float.isNaN(this.dischargeCurrent) &&
                                               this.dischargeCurrent >= DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD &&
                                               this.dischargeCurrent <= DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD;

        return ((Float.isNaN(this.current) && Float.isNaN(this.dischargeCurrent)) ||
                (currentShowsStandBy && dischargeCurrentShowsStandBy));
    }
}
