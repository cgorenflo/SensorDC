package com.sensordc;

interface CustomListener {
    SensorValues getCurrentValues();

    Boolean hasBeenUpdatedSinceLastRetrieval();

}
