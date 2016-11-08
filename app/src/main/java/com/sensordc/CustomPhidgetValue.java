package com.sensordc;

class CustomPhidgetValue {
    private static final long TIMESTAMP_NOT_SET = -1;
    private long lastRetrievedTimeStamp;
    private SensorValues value;

    CustomPhidgetValue() {
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
    }

    SensorValues getCurrentValues() {
        // Location might change in the meantime, so store reference locally
        SensorValues currentValue = this.value;

        if (currentValue == null)
            return null;

        this.lastRetrievedTimeStamp = currentValue.getTime();
        return currentValue;
    }

    Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.value.getTime();
    }

    void clear() {
        setValue(Float.NaN);
    }

    void setValue(float value) {
        this.value = new SensorValues(System.currentTimeMillis(), new float[]{value});
    }
}
