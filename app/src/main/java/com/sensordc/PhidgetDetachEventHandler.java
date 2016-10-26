package com.sensordc;

import com.phidgets.Phidget;

class PhidgetDetachEventHandler implements Runnable {
    Phidget device;

    public PhidgetDetachEventHandler(Phidget device) {
        this.device = device;
    }

    public void run() {
        SensorDCLog.i("phidget detach event handler ", " detached ");
    }
}