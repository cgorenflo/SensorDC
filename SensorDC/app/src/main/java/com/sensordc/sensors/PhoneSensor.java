package com.sensordc.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.sensordc.logging.SensorDCLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class PhoneSensor implements SensorEventListener, WebikeSensor {
    private static final String TAG = PhoneSensor.class.getSimpleName();
    private final int sensorType;
    private final SensorManager sensorManager;
    private final int defaultValueCount;
    private final List<Rule<Measurement>> activeStateRules;
    private Boolean updated;
    private SensorEvent sensorEvent;
    private boolean isRegistered;

    PhoneSensor(SensorManager sensorManager, int sensorType, int defaultValueCount) {
        super();
        this.sensorManager = sensorManager;
        this.sensorType = sensorType;
        this.defaultValueCount = defaultValueCount;
        updated = false;
        isRegistered = false;
        activeStateRules = new ArrayList<>();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.sensorEvent = sensorEvent;
        this.updated = true;
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {
    }

    public void initialize() {
        Sensor sensor = this.sensorManager.getDefaultSensor(this.sensorType);
        registerSensorListener(sensor);
    }

    private void registerSensorListener(Sensor sensor) {
        if (sensor != null) {
            this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
            isRegistered = true;
        } else {
            SensorDCLog.e(TAG, String.format(Locale.CANADA, "The sensor of type %d could not be initialized",
                    sensorType));
        }
    }

    public void stop() {
        if (isRegistered) {
            this.sensorManager.unregisterListener(this);
            SensorDCLog.i(TAG, String.format(Locale.CANADA, "Sensor %d unregistered.", sensorType));
        }
    }

    public Measurement measure() {
        //sensorEvent might change while executing this method, so store a local reference to current values
        SensorEvent event = sensorEvent;
        if (!updated) {
            SensorDCLog.i(TAG, String.format(Locale.CANADA, "No new values for sensor of type %d.", sensorType));
            return Measurement.None(defaultValueCount);
        }
        Measurement m = new Measurement();
        m.timestamp = event.timestamp;
        m.values = event.values;
        m.activityFound = false;

        for (Rule<Measurement> rule : activeStateRules) {
            if (rule.validate(m)) {
                m.activityFound = true;
            }
        }
        return m;
    }

    public void addActiveStateRule(Rule<Measurement> activeStateRule) {
        activeStateRules.add(activeStateRule);
    }
}
