package com.sensordc.logging;

import com.sensordc.sensors.SensorKit;

abstract class OutputFormatter {

    @SuppressWarnings("SameReturnValue")
    public abstract String getVersionLabel();

    public abstract String createHeader();

    public abstract String format(String currentTimeStamp, SensorKit sensorKit);
}
