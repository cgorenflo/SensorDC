package com.sensordc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Locale;

class PhoneSensorListener implements SensorEventListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private static final String TAG = PhoneSensorListener.class.getSimpleName();
    private final int sensorType;
    private long lastRetrievedTimeStamp;
    private SensorValues sensorEvent;

    PhoneSensorListener(int sensorType) {
        super();
        this.sensorType = sensorType;
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
        this.sensorEvent = SensorValues.None(5);
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

        if (!hasBeenUpdatedSinceLastRetrieval()) {
            SensorDCLog.i(TAG,
                    String.format(Locale.CANADA, "Value of sensor type %s was not updated since last retrieval.",
                            this.sensorType));
        }

        this.lastRetrievedTimeStamp = currentValues.getTime();
        return currentValues;
    }

    private Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.sensorEvent.getTime();
    }
}
