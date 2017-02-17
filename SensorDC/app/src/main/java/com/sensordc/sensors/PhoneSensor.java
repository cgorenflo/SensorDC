package com.sensordc.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.sensordc.logging.SensorDCLog;

import java.util.Locale;

public class PhoneSensor implements SensorEventListener {
    private static final String TAG = PhoneSensor.class.getSimpleName();
    private final int sensorType;
    private SensorManager sensorManager;
    private int defaultResultLength;
    private Boolean updated;
    private float[] values;

    PhoneSensor(SensorManager sensorManager, int sensorType, int defaultResultLength) {
        super();
        this.sensorManager = sensorManager;
        this.sensorType = sensorType;
        this.defaultResultLength = defaultResultLength;
        updated = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.updated = true;
        this.values = sensorEvent.values;
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {
    }

    void initialize() {
        Sensor sensor = this.sensorManager.getDefaultSensor(this.sensorType);
        registerSensorListener(sensor);
    }

    private void registerSensorListener(Sensor sensor) {
        if (sensor != null) {
            this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            SensorDCLog.e(TAG, String.format(Locale.CANADA, "The sensor of type %d could not be initialized",
                    sensorType));
        }
    }

    void stop() {
        this.sensorManager.unregisterListener(this);
    }

    public float[] getValues() {
        if (!updated) {
            SensorDCLog.i(TAG, String.format(Locale.CANADA, "No new values for sensor of type %d.", sensorType));
            float[] val = new float[defaultResultLength];
            for (int i = 0; i < val.length; i++) {
                val[i] = Float.NaN;
            }
            return val;
        }
        return values;
    }
}
