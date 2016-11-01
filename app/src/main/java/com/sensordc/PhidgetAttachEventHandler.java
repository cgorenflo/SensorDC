package com.sensordc;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;

class PhidgetAttachEventHandler implements Runnable {
    private Phidget device;

    PhidgetAttachEventHandler(Phidget device) {
        this.device = device;
    }

    public void run() {
        try {
            setSensorSensitivityToMax((InterfaceKitPhidget) this.device);

            SensorDCLog.i("phidget attach event handler ", " attached " + this.device.getDeviceName());
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
}
