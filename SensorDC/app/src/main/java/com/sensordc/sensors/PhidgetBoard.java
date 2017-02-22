package com.sensordc.sensors;

import android.content.Context;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.DetachEvent;
import com.phidgets.event.DetachListener;
import com.sensordc.logging.SensorDCLog;

import java.util.Locale;

class PhidgetBoard {

    private static final String TAG = PhidgetBoard.class.getSimpleName();
    private final Context context;
    private final PhidgetSensor phidgetSensor;
    private InterfaceKitPhidget phidget;
    private AttachListener phidgetAttachListener;
    private DetachListener phidgetDetachListener;


    PhidgetBoard(Context context, PhidgetSensor sensor) {
        this.context = context;
        this.phidgetSensor = sensor;
    }

    void initialize() {
        SensorDCLog.d(TAG, "Initializing phidget sensors.");
        try {
            com.phidgets.usb.Manager.Initialize(this.context);

            this.phidget = new InterfaceKitPhidget();
            initializeListeners();

            this.phidget.addAttachListener(this.phidgetAttachListener);
            this.phidget.addDetachListener(this.phidgetDetachListener);
            this.phidget.addSensorChangeListener(this.phidgetSensor);

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


        this.phidgetDetachListener = new DetachListener() {
            public void detached(final DetachEvent event) {
                PhidgetBoard.this.phidgetSensor.clearValues();
                SensorDCLog.i(TAG, "Phidget detached.");
            }
        };
    }

    void stop() {
        try {
            this.phidget.removeAttachListener(this.phidgetAttachListener);
            this.phidget.removeDetachListener(this.phidgetDetachListener);
            this.phidget.removeSensorChangeListener(this.phidgetSensor);

            this.phidget.close();
            SensorDCLog.i(TAG, "Phidget stopped.");
        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, "Phidget stop failed.", e);
        } finally {
            com.phidgets.usb.Manager.Uninitialize();
        }
    }

    float getBatteryTemperature() {
        return this.phidgetSensor.getBatteryTemperature().values[0];
    }

    float getAmbientTemperature() {
        return this.phidgetSensor.getAmbientTemperature().values[0];
    }

    float getVoltage() {
        return this.phidgetSensor.getVoltage().values[0];
    }

    float getCurrent() {
        return this.phidgetSensor.getCurrent().values[0];
    }

    float getDischargeCurrent() {
        return this.phidgetSensor.getDischargeCurrent().values[0];
    }

    boolean foundActivity() {
        return this.phidgetSensor.getCurrent().activityFound || this.phidgetSensor.getDischargeCurrent().activityFound;
    }
}
