package com.sensordc;

import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;


class PhidgetSensorListener implements SensorChangeListener {
    private static final String TAG = PhidgetSensorListener.class.getSimpleName();
    private static final long TIMESTAMP_NOT_SET = -1;
    private final Settings settings;
    private final Object sensorChangeLock = new Object();
    private SensorValues current;
    private SensorValues voltage;
    private SensorValues ambientTemperature;
    private SensorValues batteryTemperature;
    private SensorValues dischargeCurrent;
    private long currentLastReceived;
    private long dischargeCurrentLastReceived;
    private long voltageLastReceived;
    private long ambientTemperatureLastReceived;
    private long batteryTemperatureLastReceived;

    PhidgetSensorListener(Settings settings) {
        this.currentLastReceived = TIMESTAMP_NOT_SET;
        this.dischargeCurrentLastReceived = TIMESTAMP_NOT_SET;
        this.voltageLastReceived = TIMESTAMP_NOT_SET;
        this.ambientTemperatureLastReceived = TIMESTAMP_NOT_SET;
        this.batteryTemperatureLastReceived = TIMESTAMP_NOT_SET;

        this.settings = settings;
        clearValues();
    }

    void clearValues() {
        this.current = SensorValues.None(1);
        this.dischargeCurrent = SensorValues.None(1);
        this.voltage = SensorValues.None(1);
        this.batteryTemperature = SensorValues.None(1);
        this.ambientTemperature = SensorValues.None(1);
    }

    public void sensorChanged(SensorChangeEvent event) {
        synchronized (this.sensorChangeLock) {
            int index = event.getIndex();
            int value = event.getValue();

            Calibration ambientCal = this.settings.getAmbientCalibration();
            Calibration batteryCal = this.settings.getBatteryCalibration();

            switch (index) {
                case 0:
                    this.current = new SensorValues(System.currentTimeMillis(), value);
                    break;
                case 1:
                    this.voltage = new SensorValues(System.currentTimeMillis(), calculateVoltage(value));
                    break;
                case 2:
                    this.ambientTemperature = new SensorValues(System.currentTimeMillis(),
                            interpolateTemperature(value, ambientCal.T1, ambientCal.T2, ambientCal.V1, ambientCal.V2));
                    break;
                case 3:
                    this.batteryTemperature = new SensorValues(System.currentTimeMillis(),
                            interpolateTemperature(value, batteryCal.T1, batteryCal.T2, batteryCal.V1, batteryCal.V2));
                    break;
                case 4:
                    this.dischargeCurrent = new SensorValues(System.currentTimeMillis(), value);
                    break;
                default:
                    SensorDCLog.d(TAG, "Phidget sensor index out of bounds");
                    break;
            }
        }
    }

    //this transformation is given in the specs sheet of the voltage sensor
    private float calculateVoltage(int value) {
        return ((value / 200f) - 2.5f) / 0.0681f;
    }

    private float interpolateTemperature(int value, float t1Calibration, float t2Calibration, float v1Calibration,
                                         float v2Calibration) {
        float a = (t1Calibration - t2Calibration) / (v1Calibration - v2Calibration);
        float b = (t2Calibration * v1Calibration - t1Calibration * v2Calibration) / (v1Calibration - v2Calibration);
        return a * value + b;
    }

    float getCurrent() {
        // value might change in between, therefore store in local variable
        SensorValues current = this.current;

        if (this.currentLastReceived == current.getTime()) {
            SensorDCLog.i(TAG, "Value of current sensor was not updated since last retrieval.");
        }

        this.currentLastReceived = current.getTime();
        return current.getValues()[0];
    }

    float getDischargeCurrent() {
        // value might change in between, therefore store in local variable
        SensorValues dischargeCurrent = this.dischargeCurrent;

        if (this.dischargeCurrentLastReceived == dischargeCurrent.getTime()) {
            SensorDCLog.i(TAG, "Value of discharge current sensor was not updated since last retrieval.");
        }
        this.dischargeCurrentLastReceived = dischargeCurrent.getTime();
        return dischargeCurrent.getValues()[0];
    }

    float getVoltage() {
        // value might change in between, therefore store in local variable
        SensorValues voltage = this.voltage;

        if (this.voltageLastReceived == voltage.getTime()) {
            SensorDCLog.i(TAG, "Value of voltage sensor was not updated since last retrieval.");
        }
        this.voltageLastReceived = voltage.getTime();
        return voltage.getValues()[0];
    }

    float getAmbientTemperature() {
        // value might change in between, therefore store in local variable
        SensorValues ambientTemperature = this.ambientTemperature;

        if (this.ambientTemperatureLastReceived == ambientTemperature.getTime()) {
            SensorDCLog.i(TAG, "Value of ambient temperature sensor was not updated since last retrieval.");
        }
        this.ambientTemperatureLastReceived = ambientTemperature.getTime();
        return ambientTemperature.getValues()[0];
    }

    float getBatteryTemperature() {
        // value might change in between, therefore store in local variable
        SensorValues batteryTemperature = this.batteryTemperature;

        if (this.batteryTemperatureLastReceived == batteryTemperature.getTime()) {
            SensorDCLog.i(TAG, "Value of battery temperature sensor was not updated since last retrieval.");
        }
        this.batteryTemperatureLastReceived = batteryTemperature.getTime();
        return batteryTemperature.getValues()[0];
    }
}