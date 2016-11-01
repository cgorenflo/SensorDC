package com.sensordc;

import android.util.Log;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.event.*;

class PhidgetSensors {

    private static final String TAG = PhidgetSensors.class.getSimpleName();
    private final PhidgetManager manager;
    private final CustomPhidgetValue[] allPhidgetSensors;
    private final Object attachLock = new Object();
    private final Object sensorChangeLock = new Object();
    private CustomPhidgetValue batteryTemperature;
    private CustomPhidgetValue ambientTemperature;
    private CustomPhidgetValue voltage;
    private CustomPhidgetValue dischargeCurrent;
    private InterfaceKitPhidget phidget;
    private AttachListener phidgetAttachListener;
    private DetachListener phidgetDetachListener;
    private SensorChangeListener phidgetChangeListener;
    private CustomPhidgetValue current;

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

    void stop() {
        try {

            this.phidget.removeAttachListener(this.phidgetAttachListener);
            this.phidget.removeDetachListener(this.phidgetDetachListener);
            this.phidget.removeSensorChangeListener(this.phidgetChangeListener);

            this.phidget.close();
            com.phidgets.usb.Manager.Uninitialize();

        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
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
        }
    }

    private void initializeListeners() {
        this.phidgetAttachListener = new AttachListener() {
            public void attached(final AttachEvent event) {
                PhidgetAttachEventHandler handler = new PhidgetAttachEventHandler(event.getSource());

                synchronized (PhidgetSensors.this.attachLock) {
                    handler.run();
                }
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
                synchronized (PhidgetSensors.this.sensorChangeLock) {
                    int index = event.getIndex();
                    int value = event.getValue();
                    Settings settings = PhidgetSensors.this.manager.getSettings();

                    //Reassign to shorter reference to make the following code more readable
                    CustomPhidgetValue current = PhidgetSensors.this.current;
                    CustomPhidgetValue voltage = PhidgetSensors.this.voltage;
                    CustomPhidgetValue ambientTemperature = PhidgetSensors.this.ambientTemperature;
                    CustomPhidgetValue batteryTemperature = PhidgetSensors.this.batteryTemperature;
                    CustomPhidgetValue dischargeCurrent = PhidgetSensors.this.dischargeCurrent;


                    if (index == 0)
                        current.setValue(value);
                    if (index == 1)
                        voltage.setValue(calculateVoltage(value));
                    if (index == 2) {
                        ambientTemperature.setValue(interpolateTemperature(value, settings.getT1ambient().getValue(),
                                settings.getT2ambient().getValue(), settings.getV1ambient().getValue(),
                                settings.getV2ambient().getValue()));
                    }
                    if (index == 3) {
                        batteryTemperature.setValue(interpolateTemperature(value, settings.getT1battery().getValue(),
                                settings.getT2battery().getValue(), settings.getV1battery().getValue(),
                                settings.getV2battery().getValue()));
                    }
                    if (index == 4) {
                        dischargeCurrent.setValue(value);
                    }
                }
            }

            private float calculateVoltage(int value) {
                return ((value / 200f) - 2.5f) / 0.0681f;
            }


            private float interpolateTemperature(int value, float t1Calibration, float t2Calibration,
                                                 float v1Calibration, float v2Calibration) {
                float a = (t1Calibration - t2Calibration) / (v1Calibration - v2Calibration);
                float b = (t2Calibration * v1Calibration - t1Calibration * v2Calibration) / (v1Calibration -
                        v2Calibration);
                return a * value + b;
            }
        };
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
