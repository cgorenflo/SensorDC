package com.sensordc.logging;

import com.sensordc.sensors.SensorKit;

public abstract class OutputFormatter {

    public abstract String getVersionLabel();

    public abstract String createHeader();

    public abstract String format(String currentTimeStamp, SensorKit sensorKit);
}
