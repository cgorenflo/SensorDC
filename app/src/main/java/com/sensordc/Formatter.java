package com.sensordc;

abstract class OutputFormatter {

    abstract String createHeader();

    abstract String format(String currentTimeStamp, SensorData data);
}

class CSVFormatter extends OutputFormatter {
    @Override
    String createHeader() {
        //StringBuilder for better readability
        //noinspection StringBufferReplaceableByString
        StringBuilder header = new StringBuilder("timestamp");
        header.append(',').append("IMEI");
        header.append(',').append("code_version");
        header.append(',').append("latitude");
        header.append(',').append("longitude");
        header.append(',').append("gps_accuracy");
        header.append(',').append("charging_current");
        header.append(',').append("discharge_current");
        header.append(',').append("voltage");
        header.append(',').append("linear_acceleration_x");
        header.append(',').append("linear_acceleration_y");
        header.append(',').append("linear_acceleration_z");
        header.append(',').append("rotation_x");
        header.append(',').append("rotation_y");
        header.append(',').append("rotation_z");
        header.append(',').append("rotation_scalar");
        header.append(',').append("battery_temperature");
        header.append(',').append("ambient_temperature");
        header.append(',').append("phone_battery_percentage");
        header.append(',').append("phone_charging_or_full");
        header.append(',').append("phone_is_AC_charge");
        header.append(',').append("phone_is_USB_charge");

        return header.toString();
    }

    @Override
    String format(String currentTimeStamp, SensorData data) {
        //StringBuilder for better readability
        //noinspection StringBufferReplaceableByString
        StringBuilder sensorData = new StringBuilder(currentTimeStamp);
        sensorData.append(',').append(data.deviceID);
        sensorData.append(',').append(data.versionCode);
        sensorData.append(',').append(data.gpsLatitude);
        sensorData.append(',').append(data.gpsLongitude);
        sensorData.append(',').append(data.gpsAccuracy);
        sensorData.append(',').append(data.current);
        sensorData.append(',').append(data.dischargeCurrent);
        sensorData.append(',').append(data.voltage);
        sensorData.append(',').append(data.linearAccelerationX);
        sensorData.append(',').append(data.linearAccelerationY);
        sensorData.append(',').append(data.linearAccelerationZ);
        sensorData.append(',').append(data.rotationX);
        sensorData.append(',').append(data.rotationY);
        sensorData.append(',').append(data.rotationZ);
        sensorData.append(',').append(data.rotationScalar);
        sensorData.append(',').append(data.batteryTemperature);
        sensorData.append(',').append(data.ambientTemperature);
        sensorData.append(',').append(data.batteryPercentage);
        sensorData.append(',').append(data.isChargingOrFull);
        sensorData.append(',').append(data.isACCharge);
        sensorData.append(',').append(data.isUSBCharge);

        return sensorData.toString();
    }
}
