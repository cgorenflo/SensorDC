package com.sensordc.sensors;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.sensordc.logging.SensorDCLog;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


class PhidgetSensor implements WebikeSensor {
    private static final String TAG = PhidgetSensor.class.getSimpleName();
    private List<Rule<Measurement>> activeStateRules;
    private int sensorNumber;
    private InterfaceKitPhidget phidget;
    private Func1<Integer, Float> transformation;

    PhidgetSensor(int sensorNumber, InterfaceKitPhidget phidget) {
        this.sensorNumber = sensorNumber;
        this.phidget = phidget;
        activeStateRules = new ArrayList<>();
        transformation = null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public Measurement measure() {
        try {
            Measurement m = new Measurement();
            float value = phidget.getSensorValue(sensorNumber);
            if (transformation != null) {
                value = transformation.call((int) value);
            }
            m.values = new float[]{value};
            m.timestamp = System.currentTimeMillis();
            m.activityFound = activityFound(m);
            return m;
        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, String.format(Locale.CANADA, "Phidget sensor %d failed to record a value",
                    sensorNumber), e);
        }
        return Measurement.None(1);
    }

    private boolean activityFound(Measurement measurement) {
        boolean activityFound = !activeStateRules.isEmpty();
        for (Rule<Measurement> rule : activeStateRules) {
            if (rule.validate(measurement)) {
                activityFound = true;
            }
        }
        return activityFound;
    }

    @Override
    public void stop() {

    }

    @Override
    public void addActiveStateRule(Rule<Measurement> activeStateRule) {

        activeStateRules.add(activeStateRule);
    }

    void setTransformation(Func1<Integer, Float> transformation) {

        this.transformation = transformation;
    }
}