package com.sensordc;

abstract class OutputFormatter {

    abstract String getVersionLabel();

    abstract String createHeader();

    abstract String format(String currentTimeStamp, SensorData data);
}

class CSVFormatter extends OutputFormatter {

    @Override
    String getVersionLabel() {
        return "v3";
    }

    @Override
    String createHeader() {
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
    String format(String currentTimeStamp, SensorData data) {
        CSVLine sensorData = new CSVLine();
        sensorData.addColumn(currentTimeStamp);
        sensorData.addColumn(data.deviceID);
        sensorData.addColumn(data.versionCode);
        sensorData.addColumn(data.gpsLatitude);
        sensorData.addColumn(data.gpsLongitude);
        sensorData.addColumn(data.gpsAccuracy);
        sensorData.addColumn(data.current);
        sensorData.addColumn(data.dischargeCurrent);
        sensorData.addColumn(data.voltage);
        sensorData.addColumn(data.linearAccelerationX);
        sensorData.addColumn(data.linearAccelerationY);
        sensorData.addColumn(data.linearAccelerationZ);
        sensorData.addColumn(data.rotationX);
        sensorData.addColumn(data.rotationY);
        sensorData.addColumn(data.rotationZ);
        sensorData.addColumn(data.rotationScalar);
        sensorData.addColumn(data.batteryTemperature);
        sensorData.addColumn(data.ambientTemperature);
        sensorData.addColumn(data.batteryPercentage);
        sensorData.addColumn(data.isChargingOrFull);

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
