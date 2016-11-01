package com.sensordc;

class BatteryStatus {
    private Boolean isChargingOrFull;
    private Boolean isUSBCharge;
    private Boolean isACCharge;
    private float batteryPercentage;


    Boolean getIsChargingOrFull() {
        return this.isChargingOrFull;
    }

    void setChargingOrFull(Boolean isChargingOrFull) {
        this.isChargingOrFull = isChargingOrFull;
    }

    Boolean getIsUSBCharge() {
        return this.isUSBCharge;
    }

    void setIsUSBCharge(Boolean isUSBCharge) {
        this.isUSBCharge = isUSBCharge;
    }

    Boolean getIsACCharge() {
        return this.isACCharge;
    }

    void setIsACCharge(Boolean isACCharge) {
        this.isACCharge = isACCharge;
    }

    float getBatteryPercentage() {
        return this.batteryPercentage;
    }

    void setBatteryPercentage(float batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }
}
