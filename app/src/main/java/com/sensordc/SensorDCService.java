package com.sensordc;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SensorDCService<E> extends Service {

    // If you want to change any of these params, go to values->strings.xml
    private static String TAG = "sensordcservice";
    private static long period_poll; // in ms
    private static long period_upload, period_sense, period_record; // in ms
    private static int reboot_frequency_hours;
    private static float location_net_senstivity, location_gps_senstivity;
    private static long location_net_timesenstivity, location_gps_timesenstivity;

    private static String remotehost;
    private static String remoteuser;
    private static int remoteport;
    private static byte[] pubkey;
    private static byte[] privkey;
    private static String deviceID;

    public static void SetAutoBootAndDisableBootAudio() {

        try {
            Log.d(TAG, "SetAutoBoot ");

            final String command1 = "mount -o remount,rw /system";
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command1});
            proc.waitFor();

            final String command2 = "echo '#!/system/bin/sh' > /sdcard/playlpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command2});
            proc.waitFor();

            final String command3 = "echo '/system/bin/reboot' >> /sdcard/playlpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command3});
            proc.waitFor();


            // We have now constructed a reboot script at /sdcard/playlpm
            // We now replace /system/bin/playlpm with /sdcard/playlpm
            final String command4 = "cp /system/bin/playlpm /sdcard/playlpm.bak";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command4});
            proc.waitFor();

            final String command5 = "cp /sdcard/playlpm /system/bin/playlpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command5});
            proc.waitFor();

            final String command6 = "chmod 0755 /system/bin/playlpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command6});
            proc.waitFor();

            final String command7 = "chown root.shell /system/bin/playlpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command7});
            proc.waitFor();

            // The above steps work on Android 4.3 but on Android 4.4
            // For Android 4.4 we need the following steps
            //We replace /system/bin/lpm with /sdcard/playlpm

            final String command8 = "cp /system/bin/lpm /sdcard/lpm.bak";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command8});
            proc.waitFor();

            final String command9 = "cp /sdcard/playlpm /system/bin/lpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command9});
            proc.waitFor();

            final String command10 = "chmod 0755 /system/bin/lpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command10});
            proc.waitFor();

            final String command11 = "chown root.shell /system/bin/lpm";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command11});
            proc.waitFor();


            // Disabling boot up sound
            final String command12 = "mv /system/media/audio/ui/PowerOn.ogg /system/media/audio/ui/PowerOn.ogg" + "" +
                    ".disabled";
            proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command12});
            proc.waitFor();

        } catch (Exception e) {
            Log.e(TAG, "SetAutoBoot " + e);
            SensorDCLog.e(TAG, "SetAutoBoot " + e);
        }
    }

    public static void SetAlarm(Context context) {
        try {

            SetAutoBootAndDisableBootAudio();
            SensorDCLog.i(TAG, "Setting Recurring Alarms");

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, DataCollectionAlarm.class);
            i.setClass(context, DataCollectionAlarm.class);
            i.putExtra("period_poll", period_poll);
            i.putExtra("period_sense", period_sense);
            i.putExtra("period_record", period_record);
            i.putExtra("reboot_frequency_hours", reboot_frequency_hours);
            i.putExtra("location_net_senstivity", location_net_senstivity);
            i.putExtra("location_gps_senstivity", location_gps_senstivity);
            i.putExtra("location_net_timesenstivity", location_net_timesenstivity);
            i.putExtra("location_gps_timesenstivity", location_gps_timesenstivity);

            PendingIntent pi = PendingIntent.getBroadcast(context, 101, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), period_poll, pi);

            Intent i1 = new Intent(context, DataUploadAlarmReceiver.class);
            i1.putExtra("deviceID", deviceID);
            i1.putExtra("remoteHost", remotehost);
            i1.putExtra("remoteUser", remoteuser);
            i1.putExtra("publicKey", pubkey);
            i1.putExtra("privateKey", privkey);

            i1.putExtra("remotePort", remoteport);
            i1.setClass(context, DataUploadAlarmReceiver.class);

            PendingIntent pi1 = PendingIntent.getBroadcast(context, 102, i1, PendingIntent.FLAG_UPDATE_CURRENT);

            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 30000 + System.currentTimeMillis(), period_upload, pi1);

            ComponentName receiver = new ComponentName(context, SensorDCBroadcastReceiver.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager
                    .DONT_KILL_APP);

            Log.i(TAG, "Both recurring alarms set ");
        } catch (Exception e) {
            SensorDCLog.e(TAG, "SetAlarm " + e + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        try {
            populateParameters();
            Log.i(TAG, "created service");

        } catch (Exception e) {
            SensorDCLog.e(TAG, "onCreate " + e);
        }
    }

    private byte[] ReadFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    private void populateParameters() {
        try {
            Resources resources = this.getResources();

            remotehost = resources.getString(R.string.remotehost);
            remoteuser = resources.getString(R.string.remoteuser);
            remoteport = Integer.parseInt(resources.getString(R.string.remoteport));

            InputStream pubkeyis = resources.openRawResource(R.raw.publickey);
            pubkey = ReadFromInputStream(pubkeyis);
            InputStream privkeyis = resources.openRawResource(R.raw.privatekey);
            privkey = ReadFromInputStream(privkeyis);


            period_poll = Long.parseLong(resources.getString(R.string.period_poll));
            period_sense = Long.parseLong(resources.getString(R.string.period_sense));
            period_record = Long.parseLong(resources.getString(R.string.period_record));
            location_net_senstivity = Float.parseFloat(resources.getString(R.string.location_net_senstivity));
            location_gps_senstivity = Float.parseFloat(resources.getString(R.string.location_gps_senstivity));
            location_net_timesenstivity = Long.parseLong(resources.getString(R.string.location_net_timesenstivity));
            location_gps_timesenstivity = Long.parseLong(resources.getString(R.string.location_gps_timesenstivity));

            period_upload = Long.parseLong(resources.getString(R.string.period_upload));

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            deviceID = telephonyManager.getDeviceId();
            Log.i(TAG, "params set");

        } catch (Exception e) {
            SensorDCLog.e(TAG, "populateParameters " + e);
        }
    }

    public void onDestroy() {
        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
        SensorDCLog.e(TAG, "onDestroy ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        populateParameters();
        SetAlarm(this);
        return START_STICKY;
    }
}
