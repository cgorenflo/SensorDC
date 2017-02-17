package com.sensordc.sensors;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.sensordc.logging.SensorDCLog;

public class LocationSensor implements LocationListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private static final String TAG = LocationSensor.class.getSimpleName();
    private long lastRetrievedTimeStamp;
    private PhidgetSensor location;
    private LocationManager locationManager;
    private String provider;
    private long minTimeBetweenGPSUpdates;
    private float minDistanceBetweenGPSUpdates;

    LocationSensor(LocationManager locationManager, String provider, long minTimeBetweenGPSUpdates, float
            minDistanceBetweenGPSUpdates) {
        super();
        this.locationManager = locationManager;
        this.provider = provider;
        this.minTimeBetweenGPSUpdates = minTimeBetweenGPSUpdates;
        this.minDistanceBetweenGPSUpdates = minDistanceBetweenGPSUpdates;
    }

    void initialize() {
        locationManager.requestLocationUpdates(provider, minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates, this);
    }

    private void setLocation(Location location) {
        this.location = new PhidgetSensor(location.getTime(), new float[]{(float) location.getLatitude(), (float)
                location.getLongitude(), location.getAccuracy()});
    }

    void stop() {
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
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

    float[] getCurrentValues() {
        // Location might change in the meantime, so store reference locally
        PhidgetSensor currentLocation = this.location;

        if (currentLocation == null) {
            SensorDCLog.e(TAG, "Could not retrieve current location.");
            return PhidgetSensor.None(3).getValues();
        }

        if (!hasBeenUpdatedSinceLastRetrieval()) {
            SensorDCLog.i(TAG, "Location was not updated since last retrieval.");
        }

        this.lastRetrievedTimeStamp = currentLocation.getTime();
        return currentLocation.getValues();
    }

    private Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.location.getTime();
    }
}
