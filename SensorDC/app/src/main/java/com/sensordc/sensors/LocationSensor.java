package com.sensordc.sensors;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.sensordc.logging.SensorDCLog;

class LocationSensor implements LocationListener {
    private static final String TAG = LocationSensor.class.getSimpleName();
    private final LocationManager locationManager;
    private final String provider;
    private final long minTimeBetweenGPSUpdates;
    private final float minDistanceBetweenGPSUpdates;
    private Location location;
    private boolean updated;

    LocationSensor(LocationManager locationManager, String provider, long minTimeBetweenGPSUpdates, float
            minDistanceBetweenGPSUpdates) {
        super();
        this.locationManager = locationManager;
        this.provider = provider;
        this.minTimeBetweenGPSUpdates = minTimeBetweenGPSUpdates;
        this.minDistanceBetweenGPSUpdates = minDistanceBetweenGPSUpdates;
        updated = false;
    }

    void initialize() {
        locationManager.requestLocationUpdates(provider, minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates, this);
    }

    void stop() {
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        updated = true;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    Measurement measure() {
        // Location might change while executing this method, so store reference locally
        Location currentLocation = this.location;

        if (currentLocation == null) {
            SensorDCLog.e(TAG, "Could not retrieve current location.");
            return Measurement.None(3);
        }

        if (!updated) {
            SensorDCLog.i(TAG, "Location was not updated since last retrieval.");
            return Measurement.None(3);
        }

        updated = false;

        Measurement m = new Measurement();
        m.values = new float[3];
        m.values[0] = (float) currentLocation.getLatitude();
        m.values[1] = (float) currentLocation.getLongitude();
        m.values[2] = currentLocation.getAccuracy();

        m.timestamp = currentLocation.getTime();

        return m;
    }

}
