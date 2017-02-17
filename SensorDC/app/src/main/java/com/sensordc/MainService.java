package com.sensordc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Toast;
import com.sensordc.logging.SensorDCLog;

public class MainService extends IntentService {

    private static final String TAG = MainService.class.getSimpleName();
    private final Handler uiHandler = new Handler();

    public MainService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            createToast("Start broadcast registrations.");
            SetAlarms();
            createToast("Registrations successful");
        } catch (Exception e) {
            createToast("Registrations failed");
            SensorDCLog.e(TAG, "Broadcast alarm setup failed.", e);
        } finally {
            MainReceiver.completeWakefulIntent(intent);
        }
    }

    private void createToast(final String message) {
        this.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SetAlarms() {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);

        setDataCollectionAlarm(alarmManager);
        setDataUploadAlarm(alarmManager);

        SensorDCLog.i(TAG, "All alarms set");
    }

    private void setDataCollectionAlarm(AlarmManager alarmManager) {
        SensorDCLog.i(TAG, "Setting data collection alarm");
        setRepeatingAlarm(alarmManager, DataCollectionAlarmReceiver.class,
                getResources().getInteger(R.integer.dataCollectionIntervalInMS), 101);
    }

    private void setDataUploadAlarm(AlarmManager alarmManager) {
        SensorDCLog.i(TAG, "Setting data upload alarm");
        setRepeatingAlarm(alarmManager, DataUploadAlarmReceiver.class,
                getResources().getInteger(R.integer.dataUploadIntervalInMS), 102);
    }

    private void setRepeatingAlarm(AlarmManager alarmManager, Class<?> receivingBroadcast, int alarmInterval,
                                   int intentID) {
        Intent intent = new Intent(this, receivingBroadcast);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, intentID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), alarmInterval,
                pendingIntent);
    }
}
