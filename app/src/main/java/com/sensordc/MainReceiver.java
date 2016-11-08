package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class MainReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = MainReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            SensorDCLog.i(TAG, "Starting SensorDC service");
            startWakefulService(context, new Intent(context, SensorDCService.class));
        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
    }
}
