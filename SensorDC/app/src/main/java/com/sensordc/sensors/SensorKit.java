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
    private float batteryTemperature;
    private float ambientTemperature;
    private float voltage;
    private float current;
    private float dischargeCurrent;
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
        currentMeasurements.put("gps", Measurement.None(3));
        currentMeasurements.put("acceleration", Measurement.None(3));
        currentMeasurements.put("rotation", Measurement.None(4));

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
                        setAcceleration();
                        setRotation();
                        setGps();
                        setBattery();
                        setPhidgetMeasurement();

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
                        return foundActivity();
                    }
                });
            }
        });
    }

    private void setPhidgetMeasurement() {
        ambientTemperature = phidgetBoard.getAmbientTemperature();
        batteryTemperature = phidgetBoard.getBatteryTemperature();
        dischargeCurrent = phidgetBoard.getDischargeCurrent();
        current = phidgetBoard.getCurrent();
        voltage = phidgetBoard.getVoltage();
        anySensorShowsActiveState |= phidgetBoard.foundActivity();
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
            if (!Float.isNaN(getGpsAccuracy()) && !Float.isNaN(loc.values[2]) && loc.values[2] < getGpsAccuracy() ||
                    Float.isNaN(getGpsAccuracy())) {
                currentMeasurements.put("gps", loc);
                anySensorShowsActiveState |= loc.activityFound;
            }
        }
    }

    private void setRotation() {
        currentMeasurements.put("rotation", rotation.measure());
        anySensorShowsActiveState |= currentMeasurements.get("rotation").activityFound;
    }

    private void setAcceleration() {
        currentMeasurements.put("acceleration", acceleration.measure());
        anySensorShowsActiveState |= currentMeasurements.get("acceleration").activityFound;
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
        boolean kitIsActive = activeStateRules.isEmpty();
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
        return currentMeasurements.get("gps").values[0];
    }

    public float getGpsLongitude() {
        return currentMeasurements.get("gps").values[1];
    }

    public float getGpsAccuracy() {
        return currentMeasurements.get("gps").values[2];
    }

    public float getLinearAccelerationX() {
        return currentMeasurements.get("acceleration").values[0];
    }

    public float getLinearAccelerationY() {
        return currentMeasurements.get("acceleration").values[1];
    }

    public float getLinearAccelerationZ() {
        return currentMeasurements.get("acceleration").values[2];
    }

    public float getRotationX() {
        return currentMeasurements.get("rotation").values[0];
    }

    public float getRotationY() {
        return currentMeasurements.get("rotation").values[1];
    }

    public float getRotationZ() {
        return currentMeasurements.get("rotation").values[2];
    }

    public float getRotationScalar() {
        return currentMeasurements.get("rotation").values[3];
    }

    public float getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isChargingOrFull() {
        return isChargingOrFull;
    }

    public float getBatteryTemperature() {
        return batteryTemperature;
    }

    public float getAmbientTemperature() {
        return ambientTemperature;
    }

    public float getVoltage() {
        return voltage;
    }

    public float getCurrent() {
        return current;
    }

    public float getDischargeCurrent() {
        return dischargeCurrent;
    }
}
