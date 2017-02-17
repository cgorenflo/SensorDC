package com.sensordc.logging;

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
        sensorData.addColumn(sensorKit.deviceID);
        sensorData.addColumn(sensorKit.versionCode);
        sensorData.addColumn(sensorKit.gpsLatitude);
        sensorData.addColumn(sensorKit.gpsLongitude);
        sensorData.addColumn(sensorKit.gpsAccuracy);
        sensorData.addColumn(sensorKit.current);
        sensorData.addColumn(sensorKit.dischargeCurrent);
        sensorData.addColumn(sensorKit.voltage);
        sensorData.addColumn(sensorKit.linearAccelerationX);
        sensorData.addColumn(sensorKit.linearAccelerationY);
        sensorData.addColumn(sensorKit.linearAccelerationZ);
        sensorData.addColumn(sensorKit.rotationX);
        sensorData.addColumn(sensorKit.rotationY);
        sensorData.addColumn(sensorKit.rotationZ);
        sensorData.addColumn(sensorKit.rotationScalar);
        sensorData.addColumn(sensorKit.batteryTemperature);
        sensorData.addColumn(sensorKit.ambientTemperature);
        sensorData.addColumn(sensorKit.batteryPercentage);
        sensorData.addColumn(sensorKit.isChargingOrFull);

        return sensorData.toString();
    }


    private class CSVHeader {
        private StringBuilder header = new StringBuilder();

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
        private StringBuilder line = new StringBuilder();

        void addColumn(int versionCode) {
            addColumn(String.valueOf(versionCode));
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
