package com.sensordc.logging;

import com.sensordc.BuildConfig;
import com.sensordc.sensors.SensorKit;

public class CSVFormatter extends OutputFormatter {

    @Override
    public String getVersionLabel() {
        return "v3";
    }

    @Override
    public String createHeader() {
        CSVHeader header = new CSVHeader();
        header.addColumn("timestamp");
        header.addColumn("IMEI");
        header.addColumn("code_version");
        header.addColumn("latitude");
        header.addColumn("longitude");
        header.addColumn("gps_accuracy");
        header.addColumn("charging_current");
        header.addColumn("discharge_current");
        header.addColumn("voltage");
        header.addColumn("linear_acceleration_x");
        header.addColumn("linear_acceleration_y");
        header.addColumn("linear_acceleration_z");
        header.addColumn("rotation_x");
        header.addColumn("rotation_y");
        header.addColumn("rotation_z");
        header.addColumn("rotation_scalar");
        header.addColumn("battery_temperature");
        header.addColumn("ambient_temperature");
        header.addColumn("phone_battery_percentage");
        header.addColumn("phone_charging_or_full");

        return header.toString();
    }

    @Override
    public String format(String currentTimeStamp, SensorKit sensorKit) {
        CSVLine sensorData = new CSVLine();
        sensorData.addColumn(currentTimeStamp);
        sensorData.addColumn(sensorKit.getDeviceID());
        sensorData.addColumn(BuildConfig.VERSION_CODE);
        sensorData.addColumn(sensorKit.getGpsLatitude());
        sensorData.addColumn(sensorKit.getGpsLongitude());
        sensorData.addColumn(sensorKit.getGpsAccuracy());
        sensorData.addColumn(sensorKit.getCurrent());
        sensorData.addColumn(sensorKit.getDischargeCurrent());
        sensorData.addColumn(sensorKit.getVoltage());
        sensorData.addColumn(sensorKit.getLinearAccelerationX());
        sensorData.addColumn(sensorKit.getLinearAccelerationY());
        sensorData.addColumn(sensorKit.getLinearAccelerationZ());
        sensorData.addColumn(sensorKit.getRotationX());
        sensorData.addColumn(sensorKit.getRotationY());
        sensorData.addColumn(sensorKit.getRotationZ());
        sensorData.addColumn(sensorKit.getRotationScalar());
        sensorData.addColumn(sensorKit.getBatteryTemperature());
        sensorData.addColumn(sensorKit.getAmbientTemperature());
        sensorData.addColumn(sensorKit.getBatteryPercentage());
        sensorData.addColumn(sensorKit.isChargingOrFull());

        return sensorData.toString();
    }


    private class CSVHeader {
        private final StringBuilder header = new StringBuilder();

        @Override
        public String toString() {
            return this.header.toString();
        }

        void addColumn(String columnName) {
            if (this.header.length() > 0) {
                this.header.append(",");
            }
            if (columnName != null) {
                this.header.append(columnName);
            }
        }
    }

    private class CSVLine {
        private final StringBuilder line = new StringBuilder();

        @SuppressWarnings("SameParameterValue")
        void addColumn(int columnName) {
            addColumn(String.valueOf(columnName));
        }

        void addColumn(String columnName) {
            addSeparatorIfNecessary();
            if (columnName != null) {
                this.line.append(columnName);
            }
        }

        void addSeparatorIfNecessary() {
            if (this.line.length() > 0) {
                this.line.append(",");
            }
        }

        void addColumn(Boolean columnName) {
            addSeparatorIfNecessary();
            if (columnName != null) {
                this.line.append(columnName);
            }
        }

        void addColumn(Float columnName) {
            addSeparatorIfNecessary();
            if (columnName != null) {
                this.line.append(replaceNaNWithEmptyString(columnName));
            }
        }

        private String replaceNaNWithEmptyString(float value) {
            if (Float.isNaN(value))
                return "";
            else
                return Float.toString(value);
        }

        @Override
        public String toString() {
            return this.line.toString();
        }
    }
}
