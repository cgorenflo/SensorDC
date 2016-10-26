package com.sensordc;

import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.TemperatureSensorPhidget;

class PhidgetAttachEventHandler implements Runnable {
    Phidget device;

    public PhidgetAttachEventHandler(Phidget device) {
        this.device = device;
    }

    public void run() {
        try {

            ((TemperatureSensorPhidget) device).setTemperatureChangeTrigger(0, 0);
            SensorDCLog.i("phidget attach event handler ", " attached " + device.getDeviceName());

        } catch (PhidgetException e) {

            SensorDCLog.e("phidget attach event handler ", "" + e);
        } catch (Exception e) {
            SensorDCLog.e("phidget attach event handler ", "" + e);
        }

    }
}
