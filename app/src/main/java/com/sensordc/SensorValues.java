package com.sensordc;

class SensorValues {
    private final long time;
    private final float[] values;

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
