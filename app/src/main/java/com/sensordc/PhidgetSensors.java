package com.sensordc;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;

import java.util.Locale;

class PhidgetSensors {

    private static final String TAG = PhidgetSensors.class.getSimpleName();
    private final PhidgetManager phidgetManager;
    private InterfaceKitPhidget phidget;
    private AttachListener phidgetAttachListener;
    private DetachListener phidgetDetachListener;
    private PhidgetSensorListener phidgetChangeListener;

    PhidgetSensors(PhidgetManager phidgetManager) {
        this.phidgetManager = phidgetManager;
    }

    void initialize() {
        SensorDCLog.d(TAG, "Initializing phidget sensors.");
        try {
            com.phidgets.usb.Manager.Initialize(this.phidgetManager.getContext());

            this.phidget = new InterfaceKitPhidget();
            initializeListeners();

            this.phidget.addAttachListener(this.phidgetAttachListener);
            this.phidget.addDetachListener(this.phidgetDetachListener);
            this.phidget.addSensorChangeListener(this.phidgetChangeListener);

            this.phidget.openAny();

        } catch (Exception e) {
            SensorDCLog.e(TAG, "Phidget sensor initialization failed.", e);
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
                    SensorDCLog.e(TAG, "Phidget attach event failed.", e);
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

        this.phidgetChangeListener = new PhidgetSensorListener(this.phidgetManager.getSettings());

        this.phidgetDetachListener = new DetachListener() {
            public void detached(final DetachEvent event) {
                PhidgetSensors.this.phidgetChangeListener.clearValues();
                SensorDCLog.i(TAG, "Phidget detached.");
            }
        };
    }

    void stop() {
        try {
            this.phidget.removeAttachListener(this.phidgetAttachListener);
            this.phidget.removeDetachListener(this.phidgetDetachListener);
            this.phidget.removeSensorChangeListener(this.phidgetChangeListener);

            this.phidget.close();
            SensorDCLog.i(TAG, "Phidget stopped.");
        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, "Phidget stop failed.", e);
        } finally {
            com.phidgets.usb.Manager.Uninitialize();
        }
    }

    float getBatteryTemperature() {
        return this.phidgetChangeListener == null ? Float.NaN : this.phidgetChangeListener.getBatteryTemperature();
    }

    float getAmbientTemperature() {
        return this.phidgetChangeListener == null ? Float.NaN : this.phidgetChangeListener.getAmbientTemperature();
    }

    float getVoltage() {
        return this.phidgetChangeListener == null ? Float.NaN : this.phidgetChangeListener.getVoltage();
    }

    float getCurrent() {
        return this.phidgetChangeListener.getCurrent();
    }

    float getDischargeCurrent() {
        return this.phidgetChangeListener == null ? Float.NaN : this.phidgetChangeListener.getDischargeCurrent();
    }
}
