package com.sensordc.sensors;

public interface WebikeSensor {
    void initialize();

    Measurement measure();

    void stop();

    void addActiveStateRule(Rule<Measurement> activeStateRule);
}
