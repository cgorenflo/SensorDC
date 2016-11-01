package com.sensordc;

class SensorValues {
    private long time;
    private float[] values;

    SensorValues(long time, float[] values) {
        this.time = time;
        this.values = values;
    }

    long getTime() {
        return this.time;
    }

    float[] getValues() {
        return this.values;
    }
}
