package com.sensordc.sensors;

import android.content.Context;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Calibration;
import com.sensordc.settings.Settings;
import org.jetbrains.annotations.NotNull;
import rx.functions.Func1;

class PhidgetBoard {

    private static final String TAG = PhidgetBoard.class.getSimpleName();
    private final Context context;
    PhidgetSensor currentSensor;
    PhidgetSensor dischargeCurrentSensor;
    PhidgetSensor voltageSensor;
    PhidgetSensor ambientTemperatureSensor;
    PhidgetSensor batteryTemperatureSensor;
    private Settings settings;
    private InterfaceKitPhidget phidget;


    PhidgetBoard(Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
    }

    void initialize() {
        SensorDCLog.d(TAG, "Initializing phidget sensors.");
        try {
            com.phidgets.usb.Manager.Initialize(this.context);

            this.phidget = new InterfaceKitPhidget();
            initializeSensors();

            this.phidget.openAny();

        } catch (Exception e) {
            SensorDCLog.e(TAG, "Phidget sensor initialization failed.", e);
            stop();
        }
    }

    private float interpolateTemperature(int value, float t1Calibration, float t2Calibration, float v1Calibration,
                                         float v2Calibration) {
        float a = (t1Calibration - t2Calibration) / (v1Calibration - v2Calibration);
        float b = (t2Calibration * v1Calibration - t1Calibration * v2Calibration) / (v1Calibration - v2Calibration);
        return a * value + b;
    }

    private void initializeSensors() {
        this.currentSensor = new PhidgetSensor(0, phidget);
        this.currentSensor.addActiveStateRule(new ChargingRule());

        this.voltageSensor = new PhidgetSensor(1, phidget);
        this.voltageSensor.setTransformation(new Func1<Integer, Float>() {
            @Override
            public Float call(Integer value) {
                return ((value / 200f) - 2.5f) / 0.0681f;
            }
        });

        final Calibration ambientCal = this.settings.getAmbientCalibration();

        this.ambientTemperatureSensor = new PhidgetSensor(2, phidget);
        this.ambientTemperatureSensor.setTransformation(getTemperatureTransformation(ambientCal));

        Calibration batteryCal = this.settings.getBatteryCalibration();
        this.batteryTemperatureSensor = new PhidgetSensor(3, phidget);
        this.batteryTemperatureSensor.setTransformation(getTemperatureTransformation(batteryCal));

        this.dischargeCurrentSensor = new PhidgetSensor(4, phidget);
        this.dischargeCurrentSensor.addActiveStateRule(new DischargeRule());
    }

    @NotNull
    private Func1<Integer, Float> getTemperatureTransformation(final Calibration calibration) {
        return new Func1<Integer, Float>() {
            @Override
            public Float call(Integer value) {
                return interpolateTemperature(value, calibration.T1, calibration.T2, calibration.V1, calibration.V2);
            }
        };
    }

    void stop() {
        try {
            this.phidget.close();
            SensorDCLog.i(TAG, "Phidget stopped.");
        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, "Phidget stop failed.", e);
        } finally {
            com.phidgets.usb.Manager.Uninitialize();
        }
    }
}
