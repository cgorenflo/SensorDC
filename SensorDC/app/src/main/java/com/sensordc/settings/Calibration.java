package com.sensordc.settings;

public class Calibration {
    public final float T1;
    public final float T2;
    public final float V1;
    public final float V2;

    Calibration(float t1, float t2, float v1, float v2) {
        this.T1 = t1;
        this.T2 = t2;
        this.V1 = v1;
        this.V2 = v2;
    }
}
