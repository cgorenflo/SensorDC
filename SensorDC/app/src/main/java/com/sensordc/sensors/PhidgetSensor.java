package com.sensordc.sensors;

import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Calibration;
import com.sensordc.settings.Settings;

import java.util.ArrayList;
import java.util.List;


class PhidgetSensor implements SensorChangeListener {
    private static final String TAG = PhidgetSensor.class.getSimpleName();
    private static final long TIMESTAMP_NOT_SET = -1;
    private final Settings settings;
    private final Object sensorChangeLock = new Object();
    private Measurement current;
    private Measurement voltage;
    private Measurement ambientTemperature;
    private Measurement batteryTemperature;
    private Measurement dischargeCurrent;
    private long currentLastReceived;
    private long dischargeCurrentLastReceived;
    private long voltageLastReceived;
    private long ambientTemperatureLastReceived;
    private long batteryTemperatureLastReceived;
    private List<Rule<Measurement>> dischargeRules;
    private List<Rule<Measurement>> chargeRules;

    PhidgetSensor(Settings settings) {
        this.currentLastReceived = TIMESTAMP_NOT_SET;
        this.dischargeCurrentLastReceived = TIMESTAMP_NOT_SET;
        this.voltageLastReceived = TIMESTAMP_NOT_SET;
        this.ambientTemperatureLastReceived = TIMESTAMP_NOT_SET;
        this.batteryTemperatureLastReceived = TIMESTAMP_NOT_SET;
        dischargeRules = new ArrayList<>();
        chargeRules = new ArrayList<>();

        this.settings = settings;
        clearValues();
    }

    void clearValues() {
        this.current = Measurement.None(1);
        this.dischargeCurrent = Measurement.None(1);
        this.voltage = Measurement.None(1);
        this.batteryTemperature = Measurement.None(1);
        this.ambientTemperature = Measurement.None(1);
    }

    public void sensorChanged(SensorChangeEvent event) {
        synchronized (this.sensorChangeLock) {
            int index = event.getIndex();
            int value = event.getValue();

            Calibration ambientCal = this.settings.getAmbientCalibration();
            Calibration batteryCal = this.settings.getBatteryCalibration();

            Measurement m = new Measurement();
            m.timestamp = System.currentTimeMillis();
            switch (index) {
                case 0:
                    m.values = new float[]{value};
                    m.activityFound = anyIsActive(chargeRules, m);
                    this.current = m;
                    break;
                case 1:
                    m.values = new float[]{calculateVoltage(value)};
                    this.voltage = m;
                    break;
                case 2:

                    m.values = new float[]{interpolateTemperature(value, ambientCal.T1, ambientCal.T2, ambientCal.V1,
                            ambientCal.V2)};
                    this.ambientTemperature = m;
                    break;
                case 3:
                    m.values = new float[]{interpolateTemperature(value, batteryCal.T1, batteryCal.T2, batteryCal.V1,
                            batteryCal.V2)};
                    this.batteryTemperature = m;
                    break;
                case 4:
                    m.values = new float[]{value};
                    m.activityFound = anyIsActive(dischargeRules, m);
                    this.dischargeCurrent = m;
                    break;
                default:
                    SensorDCLog.d(TAG, "Phidget sensor index out of bounds");
                    break;
            }
        }
    }

    private boolean anyIsActive(List<Rule<Measurement>> rules, Measurement measurement) {
        if (rules.isEmpty())
            return true;

        for (Rule<Measurement> rule : rules) {
            if (rule.validate(measurement)) {
                return true;
            }
        }
        return false;
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

    Measurement getCurrent() {
        // value might change in between, therefore store in local variable
        Measurement current = this.current;

        if (this.currentLastReceived == current.timestamp) {
            SensorDCLog.i(TAG, "Value of current sensor was not updated since last retrieval.");
        }

        this.currentLastReceived = current.timestamp;
        return current;
    }

    Measurement getDischargeCurrent() {
        // value might change in between, therefore store in local variable
        Measurement dischargeCurrent = this.dischargeCurrent;

        if (this.dischargeCurrentLastReceived == dischargeCurrent.timestamp) {
            SensorDCLog.i(TAG, "Value of discharge current sensor was not updated since last retrieval.");
        }
        this.dischargeCurrentLastReceived = dischargeCurrent.timestamp;
        return dischargeCurrent;
    }

    Measurement getVoltage() {
        // value might change in between, therefore store in local variable
        Measurement voltage = this.voltage;

        if (this.voltageLastReceived == voltage.timestamp) {
            SensorDCLog.i(TAG, "Value of voltage sensor was not updated since last retrieval.");
        }
        this.voltageLastReceived = voltage.timestamp;
        return voltage;
    }

    Measurement getAmbientTemperature() {
        // value might change in between, therefore store in local variable
        Measurement ambientTemperature = this.ambientTemperature;

        if (this.ambientTemperatureLastReceived == ambientTemperature.timestamp) {
            SensorDCLog.i(TAG, "Value of ambient temperature sensor was not updated since last retrieval.");
        }
        this.ambientTemperatureLastReceived = ambientTemperature.timestamp;
        return ambientTemperature;
    }

    Measurement getBatteryTemperature() {
        // value might change in between, therefore store in local variable
        Measurement batteryTemperature = this.batteryTemperature;

        if (this.batteryTemperatureLastReceived == batteryTemperature.timestamp) {
            SensorDCLog.i(TAG, "Value of battery temperature sensor was not updated since last retrieval.");
        }
        this.batteryTemperatureLastReceived = batteryTemperature.timestamp;
        return batteryTemperature;
    }

    void addDischargeRule(Rule<Measurement> dischargeRule) {
        dischargeRules.add(dischargeRule);
    }

    void addChargeRule(Rule<Measurement> chargeRule) {
        chargeRules.add(chargeRule);
    }
}