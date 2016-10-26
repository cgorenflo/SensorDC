package com.sensordc;

import java.util.ArrayList;

public class SensorData {

    private int versionCode;
    private double lat_gps = 0, long_gps = 0;
    private double lat_net = 0, long_net = 0;

    private float maccx = 0, maccy = 0, maccz = 0;
    private float magx = 0, magy = 0, magz = 0;
    private float gyrx = 0, gyry = 0, gyrz = 0;
    private float mpressure = 0, light = 0, proximity = 0;
    private float gravity = (float) 0.00;
    private float linaccx = 0, linaccy = 0, linaccz = 0;
    private int msteps = 0;

    private String significantMotionTS;

    private double phidgettemperature = Double.MIN_VALUE;
    private double phidgetambienttemperature = Double.MIN_VALUE;
    private double phidgetvoltage = Double.MIN_VALUE;
    private double phidgetcurrent = Double.MIN_VALUE;
    private double phidgetdischargecurrent = Double.MIN_VALUE;

    private String ipaddresses = "";
    private String phonebatterystatus = "";

    public SensorData(int versionCode, double lat_gps, double long_gps, double lat_net, double long_net, float maccx,
                      float maccy, float maccz, float magx, float magy, float magz, float gyrx, float gyry, float
                              gyrz, float mpressure, float light, float proximity, float gravity, float linaccx,
                      float linaccy, float linaccz, int msteps, double phidtemperature, double
                              phidgetambienttemperature, double phidgetvoltage, double phidgetcurrent, String
                              significantMotionTS, String ipaddresses, String phonebatterystatus, double
                              phidgetdischargecurrent) {
        this.versionCode = versionCode;
        this.lat_gps = lat_gps;
        this.long_gps = long_gps;
        this.lat_net = lat_net;
        this.long_net = long_net;

        this.maccx = maccx;
        this.maccy = maccy;
        this.maccz = maccz;

        this.magx = magx;
        this.magy = magy;
        this.magz = magz;

        this.gyrx = gyrx;
        this.gyry = gyry;
        this.gyrz = gyrz;

        this.mpressure = mpressure;
        this.light = light;
        this.proximity = proximity;
        this.gravity = gravity;
        this.linaccx = linaccx;
        this.linaccy = linaccy;
        this.linaccz = linaccz;
        this.msteps = msteps;

        this.phidgettemperature = phidtemperature;
        this.phidgetambienttemperature = phidgetambienttemperature;
        this.phidgetvoltage = phidgetvoltage;
        this.phidgetcurrent = phidgetcurrent;
        this.phidgetdischargecurrent = phidgetdischargecurrent;

        this.significantMotionTS = significantMotionTS;
        this.ipaddresses = ipaddresses;
        this.phonebatterystatus = phonebatterystatus;
    }

    public String toString() {
        ArrayList<String> sensorValues = new ArrayList<String>();
        String firstSensorValue = replaceNaNByNull(versionCode);
        sensorValues.add(replaceNaNByNull(lat_gps));
        sensorValues.add(replaceNaNByNull(long_gps));
        sensorValues.add(replaceNaNByNull(lat_net));
        sensorValues.add(replaceNaNByNull(long_net));
        sensorValues.add(replaceNaNByNull(maccx));
        sensorValues.add(replaceNaNByNull(maccy));
        sensorValues.add(replaceNaNByNull(maccz));
        sensorValues.add(replaceNaNByNull(magx));
        sensorValues.add(replaceNaNByNull(magy));
        sensorValues.add(replaceNaNByNull(magz));
        sensorValues.add(replaceNaNByNull(gyrx));
        sensorValues.add(replaceNaNByNull(gyry));
        sensorValues.add(replaceNaNByNull(gyrz));
        sensorValues.add(replaceNaNByNull(mpressure));
        sensorValues.add(replaceNaNByNull(light));
        sensorValues.add(replaceNaNByNull(gravity));
        sensorValues.add(replaceNaNByNull(linaccx));
        sensorValues.add(replaceNaNByNull(linaccy));
        sensorValues.add(replaceNaNByNull(linaccz));
        sensorValues.add(Integer.toString(msteps));
        sensorValues.add(replaceNaNByNull(phidgettemperature));
        sensorValues.add(replaceNaNByNull(phidgetambienttemperature));
        sensorValues.add(replaceNaNByNull(phidgetvoltage));
        sensorValues.add(replaceNaNByNull(phidgetcurrent));
        sensorValues.add(significantMotionTS);
        sensorValues.add(replaceNaNByNull(proximity));
        sensorValues.add(ipaddresses);
        sensorValues.add(phonebatterystatus);
        sensorValues.add(replaceNaNByNull(phidgetdischargecurrent));

        StringBuilder sensorData = new StringBuilder(firstSensorValue);

        for (String value : sensorValues) {
            sensorData.append(",").append(value);
        }


        return sensorData.toString();
    }

    private String replaceNaNByNull(double value) {
        if (Double.isNaN(Math.abs(value)))
            return "";
        else
            return Double.toString(value);
    }

    private String replaceNaNByNull(float value) {
        if (Float.isNaN(Math.abs(value)))
            return "";
        else
            return Float.toString(value);
    }
}
