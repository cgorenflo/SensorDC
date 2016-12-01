package com.sensordc;

class SensorValues {
    private final long time;
    private final float[] values;

    SensorValues(long time, float value) {
        this(time, new float[]{value});
    }

    SensorValues(long time, float[] values) {
        this.time = time;
        this.values = values;
    }

    static SensorValues None(int numberOfValues) {
        float[] values = new float[numberOfValues];
        for (int i = 0; i < numberOfValues; i++) {
            values[i] = Float.NaN;
        }
        return new SensorValues(System.currentTimeMillis(), values);
    }

    long getTime() {
        return this.time;
    }

    float[] getValues() {
        if (this.values == null)
            return new float[]{Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN};
        else
            return this.values;
    }
}
