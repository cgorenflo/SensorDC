package com.sensordc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class PackageReplacedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            Intent startServiceIntent = new Intent(context, SensorDCService.class);
            if (intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) {
                boolean retVal = context.stopService(startServiceIntent);
                SensorDCLog.i("PackageReplacedReceiver ", " stopping service retVal: " + retVal);
            }

            SensorDCLog.i("PackageReplacedReceiver ", " starting service ");
            ComponentName ret = context.startService(startServiceIntent);
            if (ret == null) {
                SensorDCLog.e("PackageReplacedReceiver ", "startService retVal: " + ret);
            } else {
                SensorDCLog.e("PackageReplacedReceiver ", "startService retVal " + ret);
            }


        } catch (Exception e) {
            SensorDCLog.e("PackageReplacedReceiver  ", " " + e);
        }
    }


}
