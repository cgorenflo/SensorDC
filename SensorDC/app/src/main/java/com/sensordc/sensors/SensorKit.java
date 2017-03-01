package com.sensordc.sensors;

import com.sensordc.logging.SensorDCLog;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SensorKit {
    private static final String TAG = SensorKit.class.getSimpleName();
    public final Observable<SensorKit> updated;
    private final PhidgetBoard phidgetBoard;
    private final String deviceID;
    private final List<LocationSensor> location;
    private final List<Rule<Boolean>> activeStateRules;
    private final HashMap<String, Measurement> currentMeasurements;
    private float batteryPercentage;
    private boolean isChargingOrFull;
    private boolean isInitialized;
    private boolean anySensorShowsActiveState;
    private PhoneSensor acceleration;
    private PhoneSensor rotation;
    private Battery battery;

    SensorKit(PhidgetBoard phidgetBoard, String deviceID, final long measurementDelay) {
        this.phidgetBoard = phidgetBoard;
        this.deviceID = deviceID;
        this.location = new ArrayList<>();
        this.isInitialized = false;
        anySensorShowsActiveState = false;
        activeStateRules = new ArrayList<>();
        currentMeasurements = new HashMap<>();

        updated = Observable.defer(new Func0<Observable<SensorKit>>() {
            @Override
            public Observable<SensorKit> call() {
                return Observable.interval(measurementDelay, TimeUnit.MILLISECONDS).doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        initialize();
                    }
                }).map(new Func1<Long, SensorKit>() {
                    @Override
                    public SensorKit call(Long aLong) {
                        currentMeasurements.clear();
                        anySensorShowsActiveState = false;
                        measure();

                        for (Measurement m : currentMeasurements.values()) {
                            anySensorShowsActiveState |= m.activityFound;
                        }

                        return SensorKit.this;
                    }

                    private void measure() {
                        setGps();
                        setBattery();

                        currentMeasurements.put("acceleration", acceleration.measure());
                        currentMeasurements.put("rotation", rotation.measure());
                        currentMeasurements.put("ambientTemperature", SensorKit.this.phidgetBoard
                                .ambientTemperatureSensor.measure());
                        currentMeasurements.put("batteryTemperature", SensorKit.this.phidgetBoard
                                .batteryTemperatureSensor.measure());
                        currentMeasurements.put("dischargeCurrent", SensorKit.this.phidgetBoard
                                .dischargeCurrentSensor.measure());
                        currentMeasurements.put("current", SensorKit.this.phidgetBoard.currentSensor.measure());
                        currentMeasurements.put("voltage", SensorKit.this.phidgetBoard.voltageSensor.measure());
                    }

                    private void setBattery() {
                        battery.measure();
                        batteryPercentage = battery.getBatteryPercentage();
                        isChargingOrFull = battery.getIsChargingOrFull();
                    }

                    private void setGps() {
                        currentMeasurements.put("gps", Measurement.None(3));

                        for (LocationSensor sensor : location) {
                            Measurement loc = sensor.measure();
                            if (!Float.isNaN(getGpsAccuracy()) && !Float.isNaN(loc.values[2]) && loc.values[2] <
                                    getGpsAccuracy() || Float.isNaN(getGpsAccuracy())) {
                                currentMeasurements.put("gps", loc);
                            }
                        }
                    }
                }).doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        stop();
                    }
                }).takeWhile(new Func1<SensorKit, Boolean>() {
                    @Override
                    public Boolean call(SensorKit sensorKit) {
                        return foundActivity();
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

    private boolean foundActivity() {
        boolean kitIsActive = !activeStateRules.isEmpty();
        for (Rule<Boolean> activeStateRule : activeStateRules) {
            if (activeStateRule.validate(anySensorShowsActiveState)) {
                kitIsActive = true;
            }
        }

        return this.isInitialized && (anySensorShowsActiveState || kitIsActive);
    }

    private void initialize() {
        SensorDCLog.i(TAG, "Initializing sensors.");

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

    void addActiveStateRule(Rule<Boolean> activeStateRule) {
        activeStateRules.add(activeStateRule);
    }

    public String getDeviceID() {
        return deviceID;
    }

    public float getGpsLatitude() {
        return getValue("gps", 0);
    }

    public float getGpsLongitude() {
        return getValue("gps", 1);
    }

    public float getGpsAccuracy() {
        return getValue("gps", 2);
    }

    public float getLinearAccelerationX() {
        return getValue("acceleration", 0);
    }

    public float getLinearAccelerationY() {
        return getValue("acceleration", 1);
    }

    public float getLinearAccelerationZ() {
        return getValue("acceleration", 2);
    }

    public float getRotationX() {
        return getValue("rotation", 0);
    }

    public float getRotationY() {
        return getValue("rotation", 1);
    }

    public float getRotationZ() {
        return getValue("rotation", 2);
    }

    public float getRotationScalar() {
        return getValue("rotation", 3);
    }

    public float getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isChargingOrFull() {
        return isChargingOrFull;
    }

    public float getBatteryTemperature() {
        return getValue("batteryTemperature", 0);
    }

    public float getAmbientTemperature() {
        return getValue("ambientTemperature", 0);
    }

    public float getVoltage() {
        return getValue("voltage", 0);
    }

    public float getCurrent() {
        return getValue("current", 0);
    }

    public float getDischargeCurrent() {
        return getValue("dischargeCurrent", 0);
    }

    private float getValue(String key, int index) {
        if (currentMeasurements.containsKey(key)) {
            return currentMeasurements.get(key).values[index];
        } else {
            return Float.NaN;
        }
    }
}
