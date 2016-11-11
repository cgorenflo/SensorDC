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
            SensorDCLog.e(TAG, "Reboot failed.", e);
        }
    }

    private void HandleReboot() throws IOException, InterruptedException {
        SensorDCLog.i(TAG, "Rebooting.");
        SensorDCLog.flush();

        Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot"});
        process.waitFor();
    }
}