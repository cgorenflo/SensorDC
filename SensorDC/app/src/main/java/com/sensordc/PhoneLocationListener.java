package com.sensordc;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

class PhoneLocationListener implements LocationListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private static final String TAG = PhoneLocationListener.class.getSimpleName();
    private long lastRetrievedTimeStamp;
    private SensorValues location;

    PhoneLocationListener(LocationManager locationManager, String bestProvider, long minTimeBetweenGPSUpdates,
                          float minDistanceBetweenGPSUpdates) {
        super();
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;

        registerListener(locationManager, bestProvider, minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        Location lastKnownLocation = locationManager.getLastKnownLocation(bestProvider);
        if (lastKnownLocation != null) {
            setLocation(lastKnownLocation);
        }
    }

    private void registerListener(LocationManager locationManager, String bestProvider, long minTimeBetweenGPSUpdates,
                                  float minDistanceBetweenGPSUpdates) {
        locationManager.requestLocationUpdates(bestProvider, minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates,
                this);
    }

    private void setLocation(Location location) {
        this.location = new SensorValues(location.getTime(),
                new float[]{(float) location.getLatitude(), (float) location.getLongitude(), location.getAccuracy()});
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

    SensorValues getCurrentValues() {
        // Location might change in the meantime, so store reference locally
        SensorValues currentLocation = this.location;

        if (currentLocation == null) {
            SensorDCLog.e(TAG, "Could not retrieve current location.");
            return SensorValues.None(3);
        }

        if (!hasBeenUpdatedSinceLastRetrieval()) {
            SensorDCLog.i(TAG, "Location was not updated since last retrieval.");
        }

        this.lastRetrievedTimeStamp = currentLocation.getTime();
        return currentLocation;
    }

    private Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.location.getTime();
    }
}
