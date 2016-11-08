package com.sensordc;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.IOException;

public class HandleRebootReceiver extends WakefulBroadcastReceiver {

    private static String TAG = HandleRebootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            HandleReboot();
        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void HandleReboot() throws IOException, InterruptedException {
        SensorDCLog.DumpDataLogsToDisk();

        Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
        process.waitFor();
    }
}