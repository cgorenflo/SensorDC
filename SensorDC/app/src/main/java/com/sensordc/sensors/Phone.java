package com.sensordc.sensors;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import com.sensordc.logging.SensorDCLog;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Phone {
    private static final String TAG = Phone.class.getSimpleName();
    private final SensorManager sensorManager;
    private final LocationManager locationManager;
    private final Battery battery;
    public Observable<Phone> updated;
    private PhoneSensor linearAcceleration;
    private PhoneSensor rotationVector;
    private PhoneLocationListener locationListener;
    private String imei;
    private float[] acc;

    Phone(String imei, SensorManager sensorManager, LocationManager locationManager, Battery battery) {
        this.imei = imei;
        this.sensorManager = sensorManager;
        this.locationManager = locationManager;
        this.battery = battery;

    }

    void initialize(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates, final long measurementDelay) {
        SensorDCLog.d(TAG, "Initializing phone sensors.");
        Sensor linearAcceleration = this.sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor rotationVector = this.sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        this.linearAcceleration = new PhoneSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        this.rotationVector = new PhoneSensor(Sensor.TYPE_ROTATION_VECTOR);

        registerSensorListener(linearAcceleration, this.linearAcceleration, Sensor.TYPE_LINEAR_ACCELERATION);
        registerSensorListener(rotationVector, this.rotationVector, Sensor.TYPE_ROTATION_VECTOR);

        registerLocationListener(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        battery.initialize(measurementDelay);

        Observable<Phone> accelerationUpdated = Observable.interval(measurementDelay, TimeUnit.MILLISECONDS).flatMap
                (new Func1<Long, Observable<Phone>>() {
            @Override
            public Observable<Phone> call(Long interval) {
//                setLinearAcceleration(linearAcceleration.getCurrentValues());
                setLinearAcceleration(new float[]{interval.floatValue(), interval.floatValue(), interval.floatValue()});
                return Observable.just(Phone.this);
            }
        });

        updated = battery.updated.withLatestFrom(accelerationUpdated, new Func2<Battery, Phone, Phone>() {
            @Override
            public Phone call(Battery battery, Phone phone) {
                return Phone.this;
            }
        }).sample(measurementDelay, TimeUnit.MILLISECONDS);
    }

    private void registerSensorListener(Sensor sensor, SensorEventListener listener, int sensorType) {
        if (sensor != null) {
            this.sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            SensorDCLog.e(TAG, String.format(Locale.CANADA, "The sensor of type %d could not be initialized",
                    sensorType));
        }
    }

    private void registerLocationListener(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates) {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestProvider = this.locationManager.getBestProvider(criteria, true);

        if (bestProvider != null) {
            this.locationListener = new PhoneLocationListener(this.locationManager, bestProvider,
                    minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        } else {
            SensorDCLog.e(TAG, "No location provider found");
        }
    }

    void stop() {
        this.sensorManager.unregisterListener(this.linearAcceleration);
        this.sensorManager.unregisterListener(this.rotationVector);
        this.locationManager.removeUpdates(this.locationListener);
    }

    float[] getLinearAcceleration() {
        return acc;
    }

    private void setLinearAcceleration(float[] values) {
        this.acc = values;
    }

    float[] getRotationVector() {
        return this.rotationVector.getCurrentValues();
    }

    float[] getLocation() {
        return this.locationListener.getCurrentValues();
    }


    @SuppressLint("HardwareIds")
    String getIMEI() {
        return this.imei;
    }

    float getBatteryPercentage() {
        return this.battery.getBatteryPercentage();
    }
}
