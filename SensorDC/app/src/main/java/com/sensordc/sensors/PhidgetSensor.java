package com.sensordc.sensors;

class PhidgetSensor {
    private final long time;
    private final float[] values;

    PhidgetSensor(long time, float value) {
        this(time, new float[]{value});
    }

    PhidgetSensor(long time, float[] values) {
        this.time = time;
        this.values = values;
    }

    static PhidgetSensor None(int numberOfValues) {
        float[] values = new float[numberOfValues];
        for (int i = 0; i < numberOfValues; i++) {
            values[i] = Float.NaN;
        }
        return new PhidgetSensor(System.currentTimeMillis(), values);
    }

    long getTime() {
        return this.time;
    }

    float[] getValues() {
        return this.values;
    }
}
