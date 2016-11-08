package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataCollectionWakefulService extends IntentService {

    private static final String TAG = DataCollectionWakefulService.class.getSimpleName();

    public DataCollectionWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long minTimeBetweenGPSUpdates = R.integer.minDistanceBetweenGPSUpdates;
        float minDistanceBetweenGPSUpdates = R.integer.minTimeBetweenGPSUpdatesInMS;

        // If unregistering the sensor listeners fails, the service needs to at least release the wake lock,
        // therefore those steps are not done in the same try-finally block
        try {
            handleDataCollection(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        } catch (Exception e) {
            SensorDCLog.e(TAG, e.getMessage());
        } finally {
            DataCollectionAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void handleDataCollection(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates) {
        Settings settings = new Settings(
                getSharedPreferences(getResources().getString(R.string.settingPreferenceName), MODE_PRIVATE));

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        CustomBatteryManager batteryManager = new CustomBatteryManager(this);
        PhoneSensors phoneSensors = new PhoneSensors(sensorManager, locationManager, batteryManager);

        PhidgetSensors phidgetSensors = new PhidgetSensors(new PhidgetManager(this, settings));

        SensorDataCollector sensorDataCollector = new SensorDataCollector(telephonyManager, phoneSensors,
                phidgetSensors);

        try {
            sensorDataCollector.initializeSensors(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
            logSensorDataInIntervals(sensorDataCollector);
        } finally {
            SensorDCLog.DumpDataLogsToDisk();
            sensorDataCollector.stop();
        }
    }

    private void logSensorDataInIntervals(SensorDataCollector sensorDataCollector) {
        SensorData sensorData = SensorData.Initialize();
        while (!sensorData.isInStandBy()) {
            sensorData = sensorDataCollector.getCurrentSensorData();
            SensorDCLog.data(SensorDCLog.getCurrentTimeStamp(), sensorData.toString(), this.getClass());

            try {
                Thread.sleep(getResources().getInteger(R.integer.sensorRecordingDelayInMS));
            } catch (InterruptedException e) {
                SensorDCLog.e(TAG, Log.getStackTraceString(e));
            }
        }
    }
}
