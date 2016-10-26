package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DataUploadAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = DataUploadAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            startWakefulService(context, intent.setClass(context, DataUploadWakefulService.class));
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in dataupload alarm " + e + e.getMessage());
        }
    }
}
