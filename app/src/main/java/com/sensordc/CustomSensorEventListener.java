package com.sensordc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

class CustomSensorEventListener implements SensorEventListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private long lastRetrievedTimeStamp;
    private SensorValues sensorEvent;

    CustomSensorEventListener() {
        super();
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.sensorEvent = new SensorValues(sensorEvent.timestamp, sensorEvent.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    SensorValues getCurrentValues() {
        // Event might change in the meantime, so store reference locally
        SensorValues currentValues = this.sensorEvent;

        if (currentValues == null)
            return null;

        this.lastRetrievedTimeStamp = currentValues.getTime();
        return currentValues;
    }

    Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.sensorEvent.getTime();
    }
}
