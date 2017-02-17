package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.sensors.DeviceFactory;
import com.sensordc.sensors.SensorKit;
import com.sensordc.settings.Settings;
import rx.functions.Action1;


public class DataCollectionWakefulService extends IntentService {

    private static final String TAG = DataCollectionWakefulService.class.getSimpleName();

    public DataCollectionWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            createToast("Starting data collection");
            handleDataCollection();
        } catch (Exception e) {
            createToast("Data collection failed");
            SensorDCLog.e(TAG, "Data collection failed.", e);
        } finally {
            createToast("Going to standby");
            DataCollectionAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void createToast(final String message) {
        // Handler is needed to post a toast from a background thread to the UI thread
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataCollectionWakefulService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDataCollection() {
        SensorDCLog.d(TAG, "Creating sensor managers.");
        Settings settings = new Settings(
                getSharedPreferences(getResources().getString(R.string.settingPreferenceName), MODE_PRIVATE));

        DeviceFactory factory = new DeviceFactory(this, settings);

        int minDistanceBetweenGPSUpdates = getResources().getInteger(R.integer.minDistanceBetweenGPSUpdates);
        int minTimeBetweenGPSUpdates = getResources().getInteger(R.integer.minTimeBetweenGPSUpdatesInMS);
        int delay = getResources().getInteger(R.integer.sensorRecordingDelayInMS);
        SensorKit sensorKit = factory.assembleSensorKit(minTimeBetweenGPSUpdates, minDistanceBetweenGPSUpdates, delay);

        SensorDCLog.d(TAG, "Logging sensor values.");

        sensorKit.updated.toBlocking().forEach(new Action1<SensorKit>() {
            @Override
            public void call(SensorKit sensorKit1) {
                SensorDCLog.log(sensorKit1);
            }
        });
        SensorDCLog.flush();
    }
}
