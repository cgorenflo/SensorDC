package com.sensordc;

import com.phidgets.Phidget;


class PhidgetInterfaceKitAttachDetachRunnable implements Runnable {
    Phidget phidget;
    boolean attach;

    public PhidgetInterfaceKitAttachDetachRunnable(Phidget phidget, boolean attach) {
        this.phidget = phidget;
        this.attach = attach;
    }

    public void run() {

        if (attach) {
            try {
                SensorDCLog.i("phidget interface kit attach event handler ", " attached " + phidget.getDeviceName() +
                        " " + phidget.getDeviceID());
            } catch (Exception e) {

                SensorDCLog.i("phidget interface kit attach event handler ", " attached ");
            }
            // do nothing here for now
        } else {
            SensorDCLog.i("phidget interface kit detach event handler ", " detached ");

        }
        // do nothing for now
    }
}
