package com.sensordc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.Locale;

class PhoneSensorListener implements SensorEventListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private static final String TAG = PhoneSensorListener.class.getSimpleName();
    private final int sensorType;
    private final int numberOfValues;
    private long lastRetrievedTimeStamp;
    private SensorValues sensorEvent;

    PhoneSensorListener(int sensorType) {
        this(sensorType, 5);
    }

    PhoneSensorListener(int sensorType, int numberOfValues) {
        super();
        this.sensorType = sensorType;
        this.numberOfValues = numberOfValues;
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
        this.sensorEvent = SensorValues.None(numberOfValues);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.sensorEvent = new SensorValues(sensorEvent.timestamp, sensorEvent.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    float[] getCurrentValues() {
        // Event might change in the meantime, so store reference locally
        SensorValues currentValues = this.sensorEvent;

        if (!hasBeenUpdatedSinceLastRetrieval()) {
            SensorDCLog.i(TAG,
                    String.format(Locale.CANADA, "Value of sensor type %s was not updated since last retrieval.",
                            this.sensorType));
        }

        this.lastRetrievedTimeStamp = currentValues.getTime();
        return currentValues.getValues() == null ? SensorValues.None(numberOfValues).getValues() : currentValues.getValues();
    }

    private Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.sensorEvent.getTime();
    }
}
