package com.sensordc.sensors;

interface Rule<T> {
    boolean validate(T input);
}

class DischargeRule implements Rule<Measurement> {

    private static final float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
    private static final float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;

    @Override
    public boolean validate(Measurement measurement) {
        return !Float.isNaN(measurement.values[0]) && measurement.values[0] <=
                DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD && measurement.values[0] >=
                DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD;

    }

}

class ChargingRule implements Rule<Measurement> {
    private static final float CURRENT_INACTIVE_THRESHOLD = 50f;

    public boolean validate(Measurement measurement) {
        return !Float.isNaN(measurement.values[0]) && measurement.values[0] >= CURRENT_INACTIVE_THRESHOLD;
    }
}

class StandByMeasurementsRule implements Rule<Boolean> {
    private int executionCount = 0;

    @Override
    public boolean validate(Boolean anySensorActive) {
        if (!anySensorActive) {
            executionCount++;
        } else {
            executionCount = 0;
        }
        
        return executionCount <= 5;
    }
}

class CooldownRule implements Rule<Boolean> {

    private long lastActiveTimeInMillis = -1;
    private int activeMeasurements = 0;
    private int inactiveMeasurements = 0;

    @Override
    public boolean validate(Boolean anySensorActive) {
        if (lastActiveTimeInMillis == -1) {
            return false;
        }
        if (anySensorActive) {
            lastActiveTimeInMillis = System.currentTimeMillis();
            activeMeasurements++;
            return true;
        } else {
            inactiveMeasurements++;
            boolean wasActiveLongEnough = activeMeasurements / (float) (activeMeasurements + inactiveMeasurements) >
                    0.5 && activeMeasurements + inactiveMeasurements > 60;
            int FIVE_MINUTES = 5 * 60 * 1000;
            return System.currentTimeMillis() - lastActiveTimeInMillis < FIVE_MINUTES && wasActiveLongEnough;
        }
    }
}

class AccelerationRule implements Rule<Measurement> {
    @Override
    public boolean validate(Measurement input) {
        return Math.sqrt(Math.pow(input.values[0], 2) + Math.pow(input.values[1], 2) + Math.pow(input.values[2], 2))
                > 1;
    }
}

class BlockingRule implements Rule<Measurement> {

    @Override
    public boolean validate(Measurement input) {
        return false;
    }
}