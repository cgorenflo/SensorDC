package com.sensordc.sensors;

public class Measurement {
    public float[] values;
    long timestamp;
    boolean activityFound = false;

    static Measurement None(int defaultValueCount) {
        Measurement m = new Measurement();
        m.timestamp = -1;

        m.values = new float[defaultValueCount];
        for (int i = 0; i < m.values.length; i++) {
            m.values[i] = Float.NaN;
        }

        return m;
    }
}
