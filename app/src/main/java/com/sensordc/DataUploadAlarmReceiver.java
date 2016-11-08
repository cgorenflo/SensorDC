package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class DataUploadAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = DataUploadAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SensorDCLog.i(TAG, "Starting data upload service");
            startWakefulService(context, intent.setClass(context, DataUploadWakefulService.class));
        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
    }
}
