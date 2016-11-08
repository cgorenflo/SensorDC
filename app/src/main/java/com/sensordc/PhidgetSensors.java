package com.sensordc;

import android.util.Log;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.*;

import java.util.Locale;

class PhidgetSensors {

    private static final String TAG = PhidgetSensors.class.getSimpleName();
    private final PhidgetManager manager;
    private final CustomPhidgetValue[] allPhidgetSensors;
    private final Object sensorChangeLock = new Object();
    private final CustomPhidgetValue batteryTemperature;
    private final CustomPhidgetValue ambientTemperature;
    private final CustomPhidgetValue voltage;
    private final CustomPhidgetValue dischargeCurrent;
    private final CustomPhidgetValue current;
    private InterfaceKitPhidget phidget;
    private AttachListener phidgetAttachListener;
    private DetachListener phidgetDetachListener;
    private SensorChangeListener phidgetChangeListener;

    PhidgetSensors(PhidgetManager manager) {
        this.manager = manager;
        this.current = new CustomPhidgetValue();
        this.dischargeCurrent = new CustomPhidgetValue();
        this.voltage = new CustomPhidgetValue();
        this.batteryTemperature = new CustomPhidgetValue();
        this.ambientTemperature = new CustomPhidgetValue();

        this.allPhidgetSensors = new CustomPhidgetValue[]{this.current, this.dischargeCurrent, this.voltage,
                                                          this.ambientTemperature, this.batteryTemperature};
    }

    void initialize() {
        try {
            com.phidgets.usb.Manager.Initialize(this.manager.getContext());

            this.phidget = new InterfaceKitPhidget();
            initializeListeners();

            this.phidget.addAttachListener(this.phidgetAttachListener);
            this.phidget.addDetachListener(this.phidgetDetachListener);
            this.phidget.addSensorChangeListener(this.phidgetChangeListener);

            this.phidget.openAny();

        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
            stop();
        }
    }

    private void initializeListeners() {
        this.phidgetAttachListener = new AttachListener() {
            public void attached(final AttachEvent event) {
                try {
                    setSensorSensitivityToMax((InterfaceKitPhidget) event.getSource());
                    SensorDCLog.i(TAG, String.format(Locale.CANADA, "%s attached", event.getSource().getDeviceName()));
                } catch (PhidgetException e) {
                    SensorDCLog.e("phidget attach event handler ", "" + e);
                }
            }

            private void setSensorSensitivityToMax(InterfaceKitPhidget phidget) throws PhidgetException {
                phidget.setSensorChangeTrigger(0, 1);
                phidget.setSensorChangeTrigger(1, 1);
                phidget.setSensorChangeTrigger(2, 1);
                phidget.setSensorChangeTrigger(3, 1);
                phidget.setSensorChangeTrigger(4, 1);
            }
        };

        this.phidgetDetachListener = new DetachListener() {
            public void detached(final DetachEvent event) {
                PhidgetSensors.this.current.clear();
                PhidgetSensors.this.dischargeCurrent.clear();
                PhidgetSensors.this.voltage.clear();
                PhidgetSensors.this.ambientTemperature.clear();
                PhidgetSensors.this.batteryTemperature.clear();

                SensorDCLog.i("phidget interface kit detach event handler ", " detached ");
            }
        };

        this.phidgetChangeListener = new SensorChangeListener() {
            public void sensorChanged(SensorChangeEvent event) {
                PhidgetSensors phidgetSensors = PhidgetSensors.this;
                synchronized (phidgetSensors.sensorChangeLock) {
                    int index = event.getIndex();
                    int value = event.getValue();

                    //Reassign to shorter reference to make the following code more readable
                    CustomPhidgetValue current = phidgetSensors.current;
                    CustomPhidgetValue voltage = phidgetSensors.voltage;
                    CustomPhidgetValue ambientTemperature = phidgetSensors.ambientTemperature;
                    CustomPhidgetValue batteryTemperature = phidgetSensors.batteryTemperature;
                    CustomPhidgetValue dischargeCurrent = phidgetSensors.dischargeCurrent;

                    Calibration ambientCal = phidgetSensors.manager.getSettings().getAmbientCalibration();
                    Calibration batteryCal = phidgetSensors.manager.getSettings().getBatteryCalibration();

                    switch (index) {
                        case 0:
                            current.setValue(value);
                            break;
                        case 1:
                            voltage.setValue(calculateVoltage(value));
                            break;
                        case 2:
                            ambientTemperature.setValue(
                                    interpolateTemperature(value, ambientCal.T1, ambientCal.T2, ambientCal.V1,
                                            ambientCal.V2));
                            break;
                        case 3:
                            batteryTemperature.setValue(
                                    interpolateTemperature(value, batteryCal.T1, batteryCal.T2, batteryCal.V1,
                                            batteryCal.V2));
                            break;
                        case 4:
                            dischargeCurrent.setValue(value);
                            break;
                        default:
                            SensorDCLog.e(TAG, "Phidget sensor index out of bounds");
                            break;
                    }
                }
            }

            //this transformation is given in the specs sheet of the voltage sensor
            private float calculateVoltage(int value) {
                return ((value / 200f) - 2.5f) / 0.0681f;
            }


            private float interpolateTemperature(int value, float t1Calibration, float t2Calibration,
                                                 float v1Calibration, float v2Calibration) {
                float a = (t1Calibration - t2Calibration) / (v1Calibration - v2Calibration);
                float b = (t2Calibration * v1Calibration - t1Calibration * v2Calibration) /
                          (v1Calibration - v2Calibration);
                return a * value + b;
            }
        };
    }

    void stop() {
        try {
            this.phidget.removeAttachListener(this.phidgetAttachListener);
            this.phidget.removeDetachListener(this.phidgetDetachListener);
            this.phidget.removeSensorChangeListener(this.phidgetChangeListener);

            this.phidget.close();
        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        } finally {
            com.phidgets.usb.Manager.Uninitialize();
        }
    }

    boolean areAllUpdated() {
        for (CustomPhidgetValue value : this.allPhidgetSensors) {
            if (!value.hasBeenUpdatedSinceLastRetrieval())
                return false;
        }

        return true;
    }

    float getBatteryTemperature() {
        return this.batteryTemperature.getCurrentValues().getValues()[0];
    }

    float getAmbientTemperature() {
        return this.ambientTemperature.getCurrentValues().getValues()[0];
    }

    float getVoltage() {
        return this.voltage.getCurrentValues().getValues()[0];
    }

    float getCurrent() {
        return this.current.getCurrentValues().getValues()[0];
    }

    float getDischargeCurrent() {
        return this.dischargeCurrent.getCurrentValues().getValues()[0];
    }
}
