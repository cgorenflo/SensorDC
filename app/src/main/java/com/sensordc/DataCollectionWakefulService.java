package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class DataCollectionWakefulService extends IntentService {

    private static final String TAG = DataCollectionWakefulService.class.getSimpleName();

    private final Handler uiHandler = new Handler();

    public DataCollectionWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int minDistanceBetweenGPSUpdates = getResources().getInteger(R.integer.minDistanceBetweenGPSUpdates);
        int minTimeBetweenGPSUpdates = getResources().getInteger(R.integer.minTimeBetweenGPSUpdatesInMS);

        try {
            createToast("Starting data collection");
            handleDataCollection(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
        } catch (Exception e) {
            createToast("Data collection failed");
            SensorDCLog.e(TAG, "Data collection failed.", e);
        } finally {
            createToast("Going to standby");
            DataCollectionAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void createToast(final String message) {
        this.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataCollectionWakefulService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDataCollection(int minTimeBetweenGPSUpdates, int minDistanceBetweenGPSUpdates) {
        SensorDCLog.d(TAG, "Creating sensor managers.");
        Settings settings = new Settings(
                getSharedPreferences(getResources().getString(R.string.settingPreferenceName), MODE_PRIVATE));

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        SensorManager sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        BatteryManager batteryManager = new BatteryManager(this);
        PhoneSensors phoneSensors = new PhoneSensors(sensorManager, locationManager, batteryManager);

        PhidgetSensors phidgetSensors = new PhidgetSensors(new PhidgetManager(this, settings));

        SensorDataCollector sensorDataCollector = new SensorDataCollector(telephonyManager, phoneSensors,
                phidgetSensors);

        try {
            sensorDataCollector.initializeSensors(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
            logSensorDataInIntervals(sensorDataCollector);
        } finally {
            SensorDCLog.flush();
            sensorDataCollector.stop();
        }
    }

    private void logSensorDataInIntervals(SensorDataCollector sensorDataCollector) {
        SensorDCLog.d(TAG, "Logging sensor values.");
        SensorData sensorData = SensorData.Initialize();
        while (!sensorData.isInStandBy()) {

            //wait first so sensors have a chance to update
            try {
                Thread.sleep(getResources().getInteger(R.integer.sensorRecordingDelayInMS));
            } catch (InterruptedException e) {
                SensorDCLog.e(TAG, "Data collection thread was interrupted, stopping data collection", e);
                break;
            }
            sensorData = sensorDataCollector.getCurrentSensorData();
            SensorDCLog.log(sensorData);
        }
    }
}
