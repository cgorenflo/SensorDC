package com.sensordc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

class CustomLocationListener implements LocationListener {
    private static final long TIMESTAMP_NOT_SET = -1;
    private long lastRetrievedTimeStamp;
    private SensorValues location;


    CustomLocationListener() {
        super();
        this.lastRetrievedTimeStamp = TIMESTAMP_NOT_SET;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = new SensorValues(location.getTime(),
                new float[]{(float) location.getLatitude(), (float) location.getLongitude(), location.getAccuracy()});
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

        if (currentLocation == null)
            return null;

        this.lastRetrievedTimeStamp = currentLocation.getTime();
        return currentLocation;
    }

    Boolean hasBeenUpdatedSinceLastRetrieval() {
        return this.lastRetrievedTimeStamp != this.location.getTime();
    }
}
