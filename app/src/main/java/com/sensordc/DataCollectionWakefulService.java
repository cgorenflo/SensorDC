package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.telephony.TelephonyManager;

public class DataCollectionWakefulService extends IntentService {

    private static final String TAG = DataCollectionWakefulService.class.getSimpleName();
    private static final long SENSOR_RECORDING_DELAY_IN_MS = 1000;
    private static final int CONSECUTIVE_SLEEP_CYCLE_THRESHOLD = 30;
    private static final String PREFS_NAME = "SensorDCPrefs";

    public DataCollectionWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long minTimeBetweenGPSUpdates = intent.getExtras().getLong("location_gps_timesenstivity");
        float minDistanceBetweenGPSUpdates = intent.getExtras().getFloat("location_gps_senstivity");

        // If unregistering the sensor listeners fails, the service needs to at least give up the wake lock,
        // therefore those steps are not done in the same try-finally block
        try {
            Sensors sensors = new Sensors((SensorManager) this.getSystemService(SENSOR_SERVICE),
                    (LocationManager) this.getSystemService(LOCATION_SERVICE), new CustomBatteryManager(this),
                    (TelephonyManager) getSystemService(TELEPHONY_SERVICE),
                    new PhidgetManager(this, new Settings(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))));

            sensors.initializeSensors(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates);
            try {

                logSensorDataInIntervals(sensors);

            } catch (InterruptedException e) {
                SensorDCLog.e(TAG, e.getMessage());
            } finally {
                SensorDCLog.DumpDataLogsToDisk();
                sensors.stop();
            }

        } catch (Exception e) {
            SensorDCLog.e(TAG, e.getMessage());
        } finally {
            DataCollectionAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void logSensorDataInIntervals(Sensors sensors) throws InterruptedException {
        Boolean isInStandby = false;
        int consecutiveSleepCycles = 0;
        while (!isInStandby && consecutiveSleepCycles < CONSECUTIVE_SLEEP_CYCLE_THRESHOLD) {
            if (sensors.areAllUpdated()) {
                SensorData sensorData = sensors.getCurrentSensorData();
                isInStandby = sensorData.isInStandBy();
                consecutiveSleepCycles = 0;

                SensorDCLog.data(SensorDCLog.getCurrentTimeStamp(), sensorData.toString(), this.getClass());

            } else {
                consecutiveSleepCycles += 1;
                SensorDCLog.i("Not all sensors were updated in the period of one second.");
            }

            Thread.sleep(SENSOR_RECORDING_DELAY_IN_MS);
        }
    }
}
