package com.sensordc;

class CustomPhidgetValue implements CustomListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private long lastRetrievedTimeStamp;
    private SensorValues value;

    CustomPhidgetValue() {
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
    }

    @Override
    public SensorValues getCurrentValues() {
        // Location might change in the meantime, so store reference locally
        SensorValues returnedValue = this.value;

        if (returnedValue == null)
            return null;

        this.lastRetrievedTimeStamp = returnedValue.getTime();
        return returnedValue;
    }

    public Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.value.getTime();
    }

    void setValue(float value) {
        this.value = new SensorValues(System.currentTimeMillis(), new float[]{value});
    }

    public void clear() {
        setValue(Float.NaN);
    }
}
