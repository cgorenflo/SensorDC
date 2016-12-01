package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class DataCollectionAlarmReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = DataCollectionAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorDCLog.i(TAG, "Starting data collection service.");
        startWakefulService(context, intent.setClass(context, DataCollectionWakefulService.class));
    }
}
