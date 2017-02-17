package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.sensordc.logging.SensorDCLog;

public class MainReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = MainReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        SensorDCLog.i(TAG, "Starting main service");
        startWakefulService(context, new Intent(context, MainService.class));
    }
}
