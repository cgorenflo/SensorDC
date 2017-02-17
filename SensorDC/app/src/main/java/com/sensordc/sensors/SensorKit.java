package com.sensordc.sensors;

import com.sensordc.logging.SensorDCLog;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SensorKit {
    private static final String TAG = SensorKit.class.getSimpleName();
    private static final float CURRENT_INACTIVE_THRESHOLD = 50f;
    private static final float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
    private static final float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;
    private final PhidgetBoard phidgetBoard;
    public Observable<SensorKit> updated;
    public int versionCode;
    public String deviceID;
    public float gpsLatitude;
    public float gpsLongitude;
    public float gpsAccuracy;
    public float linearAccelerationX;
    public float linearAccelerationY;
    public float linearAccelerationZ;
    public float rotationX;
    public float rotationY;
    public float rotationZ;
    public float rotationScalar;
    public float batteryPercentage;
    public Boolean isChargingOrFull;
    public float batteryTemperature;
    public float ambientTemperature;
    public float voltage;
    public float current;
    public float dischargeCurrent;
    private boolean isInitialized;
    private PhoneSensor acceleration;
    private PhoneSensor rotation;
    private Battery battery;
    private List<LocationSensor> location;

    SensorKit(PhidgetBoard phidgetBoard, final long measurementDelay) {
        this.phidgetBoard = phidgetBoard;
        this.location = new ArrayList<>();
        this.isInitialized = false;

        updated = Observable.defer(new Func0<Observable<SensorKit>>() {
            @Override
            public Observable<SensorKit> call() {
                return Observable.interval(measurementDelay, TimeUnit.MILLISECONDS).doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        initialize(measurementDelay);
                    }
                }).map(new Func1<Long, SensorKit>() {
                    @Override
                    public SensorKit call(Long aLong) {
                        float[] acc = acceleration.getValues();
                        linearAccelerationX = acc[0];
                        linearAccelerationY = acc[1];
                        linearAccelerationZ = acc[2];

                        float[] rot = rotation.getValues();
                        rotationX = rot[0];
                        rotationY = rot[1];
                        rotationZ = rot[2];
                        rotationScalar = rot[3];


                        return SensorKit.this;
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        stop();
                    }
                }).takeWhile(new Func1<SensorKit, Boolean>() {
                    @Override
                    public Boolean call(SensorKit sensorKit) {
                        return !isInStandBy();
                    }
                });
            }
        });
    }

    void addLocationSensor(LocationSensor sensor) {
        location.add(sensor);
    }

    void addRotationSensor(PhoneSensor sensor) {
        rotation = sensor;
    }

    void addBatterySensor(Battery sensor) {
        battery = sensor;
    }

    void addAccelerationSensor(PhoneSensor sensor) {
        acceleration = sensor;
    }

    private boolean isInStandBy() {
        if (!this.isInitialized)
            return false;

        boolean currentShowsStandBy = !Float.isNaN(this.current) && this.current <= CURRENT_INACTIVE_THRESHOLD;

        boolean dischargeCurrentShowsStandBy = !Float.isNaN(this.dischargeCurrent) && this.dischargeCurrent >=
                DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD && this.dischargeCurrent <=
                DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD;

        return ((Float.isNaN(this.current) && Float.isNaN(this.dischargeCurrent)) || (currentShowsStandBy &&
                dischargeCurrentShowsStandBy));
    }

    private void initialize(final long measurementDelay) {
        SensorDCLog.i(TAG, "Initializing sensors.");

        battery.initialize(measurementDelay);
        acceleration.initialize();
        rotation.initialize();
        for (LocationSensor l : location) {
            l.initialize();
        }
        this.phidgetBoard.initialize();

        this.isInitialized = true;
    }

    private void stop() {
        SensorDCLog.i(TAG, "Unregistering sensor listeners.");
        acceleration.stop();
        rotation.stop();
        for (LocationSensor l : location) {
            l.stop();
        }
        this.phidgetBoard.stop();
    }
}
