package com.sensordc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;


public class SensorDCBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();

            Intent startServiceIntent = new Intent(context, SensorDCService.class);

            SensorDCLog.i("SensorDCBroadcastReceiver ", " starting service ");
            ComponentName ret = context.startService(startServiceIntent);
            if (ret == null) {
                SensorDCLog.e("SensorDCBroadcastReceiver ", "startService=" + null);
            }

            SensorDCService.SetAlarm(context);
            wl.release();

        } catch (Exception e) {
            SensorDCLog.e("SensorDCBroadcastReceiver  ", " " + e);
        }

    }

}
