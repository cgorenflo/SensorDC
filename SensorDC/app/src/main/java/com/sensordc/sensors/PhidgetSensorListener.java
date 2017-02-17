package com.sensordc.sensors;

import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Calibration;
import com.sensordc.settings.Settings;


public class PhidgetSensorListener implements SensorChangeListener {
    private static final String TAG = PhidgetSensorListener.class.getSimpleName();
    private static final long TIMESTAMP_NOT_SET = -1;
    private final Settings settings;
    private final Object sensorChangeLock = new Object();
    private PhidgetSensor current;
    private PhidgetSensor voltage;
    private PhidgetSensor ambientTemperature;
    private PhidgetSensor batteryTemperature;
    private PhidgetSensor dischargeCurrent;
    private long currentLastReceived;
    private long dischargeCurrentLastReceived;
    private long voltageLastReceived;
    private long ambientTemperatureLastReceived;
    private long batteryTemperatureLastReceived;

    public PhidgetSensorListener(Settings settings) {
        this.currentLastReceived = TIMESTAMP_NOT_SET;
        this.dischargeCurrentLastReceived = TIMESTAMP_NOT_SET;
        this.voltageLastReceived = TIMESTAMP_NOT_SET;
        this.ambientTemperatureLastReceived = TIMESTAMP_NOT_SET;
        this.batteryTemperatureLastReceived = TIMESTAMP_NOT_SET;

        this.settings = settings;
        clearValues();
    }

    public void clearValues() {
        this.current = PhidgetSensor.None(1);
        this.dischargeCurrent = PhidgetSensor.None(1);
        this.voltage = PhidgetSensor.None(1);
        this.batteryTemperature = PhidgetSensor.None(1);
        this.ambientTemperature = PhidgetSensor.None(1);
    }

    public void sensorChanged(SensorChangeEvent event) {
        synchronized (this.sensorChangeLock) {
            int index = event.getIndex();
            int value = event.getValue();

            Calibration ambientCal = this.settings.getAmbientCalibration();
            Calibration batteryCal = this.settings.getBatteryCalibration();

            switch (index) {
                case 0:
                    this.current = new PhidgetSensor(System.currentTimeMillis(), value);
                    break;
                case 1:
                    this.voltage = new PhidgetSensor(System.currentTimeMillis(), calculateVoltage(value));
                    break;
                case 2:
                    this.ambientTemperature = new PhidgetSensor(System.currentTimeMillis(),
                            interpolateTemperature(value, ambientCal.T1, ambientCal.T2, ambientCal.V1, ambientCal.V2));
                    break;
                case 3:
                    this.batteryTemperature = new PhidgetSensor(System.currentTimeMillis(),
                            interpolateTemperature(value, batteryCal.T1, batteryCal.T2, batteryCal.V1, batteryCal.V2));
                    break;
                case 4:
                    this.dischargeCurrent = new PhidgetSensor(System.currentTimeMillis(), value);
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

    public float getCurrent() {
        // value might change in between, therefore store in local variable
        PhidgetSensor current = this.current;

        if (this.currentLastReceived == current.getTime()) {
            SensorDCLog.i(TAG, "Value of current sensor was not updated since last retrieval.");
        }

        this.currentLastReceived = current.getTime();
        return current.getValues()[0];
    }

    public float getDischargeCurrent() {
        // value might change in between, therefore store in local variable
        PhidgetSensor dischargeCurrent = this.dischargeCurrent;

        if (this.dischargeCurrentLastReceived == dischargeCurrent.getTime()) {
            SensorDCLog.i(TAG, "Value of discharge current sensor was not updated since last retrieval.");
        }
        this.dischargeCurrentLastReceived = dischargeCurrent.getTime();
        return dischargeCurrent.getValues()[0];
    }

    public float getVoltage() {
        // value might change in between, therefore store in local variable
        PhidgetSensor voltage = this.voltage;

        if (this.voltageLastReceived == voltage.getTime()) {
            SensorDCLog.i(TAG, "Value of voltage sensor was not updated since last retrieval.");
        }
        this.voltageLastReceived = voltage.getTime();
        return voltage.getValues()[0];
    }

    public float getAmbientTemperature() {
        // value might change in between, therefore store in local variable
        PhidgetSensor ambientTemperature = this.ambientTemperature;

        if (this.ambientTemperatureLastReceived == ambientTemperature.getTime()) {
            SensorDCLog.i(TAG, "Value of ambient temperature sensor was not updated since last retrieval.");
        }
        this.ambientTemperatureLastReceived = ambientTemperature.getTime();
        return ambientTemperature.getValues()[0];
    }

    public float getBatteryTemperature() {
        // value might change in between, therefore store in local variable
        PhidgetSensor batteryTemperature = this.batteryTemperature;

        if (this.batteryTemperatureLastReceived == batteryTemperature.getTime()) {
            SensorDCLog.i(TAG, "Value of battery temperature sensor was not updated since last retrieval.");
        }
        this.batteryTemperatureLastReceived = batteryTemperature.getTime();
        return batteryTemperature.getValues()[0];
    }
}