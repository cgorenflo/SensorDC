package com.sensordc;

import android.content.*;
import android.content.pm.PackageInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;
import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.TemperatureSensorPhidget;
import com.phidgets.event.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class DataCollectionAlarm extends BroadcastReceiver implements SensorEventListener {

    private static final String PREFS_NAME = "SensorDCPrefs";
    public static long SENSING_WINDOW;// MILLISECONDS
    public static long SENSING_FREQUENCY;// MILLISECONDS
    private static double DISCHARGE_CURRENT_RANGE_LOW = 490.00;
    private static double DISCHARGE_CURRENT_RANGE_HIGH = 510.00;
    private static String TAG = "sensordccollectionalarm";
    private static long minTime_loc_gps /* in ms */;
    private static float minDistance_loc_gps /* in m */;
    private static long minTime_loc_net /* in ms */;
    private static float minDistance_loc_net /* in m */;
    final long milliseconds = 1000000;
    //android api sensor delay
    final int Sensor_Delay = SensorManager.SENSOR_DELAY_GAME;
    TemperatureSensorPhidget device;
    InterfaceKitPhidget interfacekit;
    long alarmFireTs, lastLoggedTs;
    private LocationManager locationManager;
    private LocationListener locationListener_gps, locationListener_net;
    private int versionCode = 0;
    private SensorManager mSensorManager;
    private Sensor s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13;
    private PowerManager.WakeLock wl;
    private double lat_gps = Double.NaN, long_gps = Double.NaN;
    private double lat_net = Double.NaN, long_net = Double.NaN;
    private float maccx = Float.NaN, maccy = Float.NaN, maccz = Float.NaN;
    private float magx = Float.NaN, magy = Float.NaN, magz = Float.NaN;
    private float gyrx = Float.NaN, gyry = Float.NaN, gyrz = Float.NaN;
    private float mpressure = Float.NaN, light = Float.NaN, proximity = Float.NaN;
    private float gravity = Float.NaN;
    private float linaccx = Float.NaN, linaccy = Float.NaN, linaccz = Float.NaN;
    private int msteps = 0;
    private double phidgettemperature = Double.NaN;
    private double phidgetambienttemperature = Double.NaN;
    private double phidgetvoltage = -Double.NaN;
    private double phidgetcurrent = -Double.NaN;
    private double phidgetdischargecurrent = -Double.NaN;
    private String significantMotionTS = "NULL";
    private String ipaddresses = "NULL";
    private String phoneBatteryStatus = "NULL";
    private AttachListener phidgetattachlistener;
    private DetachListener phidgetdetachlistener;
    private SensorChangeListener phidgetchangelistener;
    private Settings settings;

    private void PopulateParameters(Intent intent) {
        SENSING_WINDOW = intent.getExtras().getLong("period_sense");
        SENSING_FREQUENCY = intent.getExtras().getLong("period_record");
        minDistance_loc_gps = intent.getExtras().getFloat("location_gps_senstivity");
        minDistance_loc_net = intent.getExtras().getFloat("location_net_senstivity");
        minTime_loc_net = intent.getExtras().getLong("location_net_timesenstivity");
        minTime_loc_gps = intent.getExtras().getLong("location_gps_timesenstivity");
    }

    private void RegisterSensorListeners(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        s1 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (s1 != null)
            mSensorManager.registerListener(this, s1, Sensor_Delay);

        s2 = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (s2 != null)
            mSensorManager.registerListener(this, s2, Sensor_Delay);

        s3 = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (s3 != null)
            mSensorManager.registerListener(this, s3, Sensor_Delay);

        s4 = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (s4 != null)
            mSensorManager.registerListener(this, s4, Sensor_Delay);

        s5 = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (s5 != null)
            mSensorManager.registerListener(this, s5, Sensor_Delay);

        s6 = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (s6 != null)
            mSensorManager.registerListener(this, s6, Sensor_Delay);

        s7 = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (s7 != null)
            mSensorManager.registerListener(this, s7, Sensor_Delay);

        s8 = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (s8 != null)
            mSensorManager.registerListener(this, s8, Sensor_Delay);

        s11 = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (s11 != null)
            mSensorManager.registerListener(this, s11, Sensor_Delay);

        //commented out sensor code left in so that missing sensor types are still visible in the code

        // s9 =
        // mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        // mSensorManager.registerListener(this,
        // s9,Sensor_Delay);
        // s10 = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        // mSensorManager.registerListener(this,
        // s10,Sensor_Delay);
        // s12 =
        // mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        // s13 =
        // mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
            wl.acquire();
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in Datacollectionalarm acquirelock " + e + Log.getStackTraceString(e));
        }

        PopulateParameters(intent);
        this.settings = new Settings(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
        alarmFireTs = System.nanoTime();
        lastLoggedTs = 0;

        HandleReboot();
        Log.d("broadcastreceiver", "alarm received");

        updateVersion(context);
        getIpAddresses();
        getBatteryStatus(context);
        initPhidget(context);
        prepareGPS(context);

        try {
            RegisterSensorListeners(context);
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in Datacollectionalarm sensormanager " + e + Log.getStackTraceString(e));
        }

    }

    private void Uninitialize() {
        try {
            this.locationManager.removeUpdates(locationListener_net);
            this.locationManager.removeUpdates(locationListener_gps);
            this.mSensorManager.unregisterListener(this);

            interfacekit.removeAttachListener(phidgetattachlistener);
            interfacekit.removeDetachListener(phidgetdetachlistener);
            interfacekit.removeSensorChangeListener(phidgetchangelistener);

            interfacekit.close();
            com.phidgets.usb.Manager.Uninitialize();

            SensorDCLog.e(TAG, "Uninitializing all context ");

        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in Uninitialize " + e + Log.getStackTraceString(e));
        }

        try {
            wl.release();
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in Uninitialize releaselock " + e + Log.getStackTraceString(e));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void UpdateValue(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER: {
                maccx = event.values[0];
                maccy = event.values[1];
                maccz = event.values[2];
                break;
            }

            case Sensor.TYPE_MAGNETIC_FIELD: {
                magx = event.values[0];
                magy = event.values[1];
                magz = event.values[2];
                break;
            }

            case Sensor.TYPE_GYROSCOPE: {
                gyrx = event.values[0];
                gyry = event.values[1];
                gyrz = event.values[2];
                break;
            }

            case Sensor.TYPE_PRESSURE: {
                mpressure = event.values[0];
                break;
            }

            case Sensor.TYPE_LIGHT: {
                light = event.values[0];
                break;
            }

            case Sensor.TYPE_PROXIMITY: {
                proximity = event.values[0];
                break;
            }

            case Sensor.TYPE_GRAVITY: {
                gravity = event.values[0];
                break;
            }

            case Sensor.TYPE_LINEAR_ACCELERATION: {
                linaccx = event.values[0];
                linaccy = event.values[1];
                linaccz = event.values[2];
                break;
            }

            default:
        }

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            // nano time is relative, and useful
            // only for measuring duration
            long nowTs = System.nanoTime();
            String nowTSAbsolute = SensorDCLog.getCurrentTimeStamp();
            UpdateValue(event);

            //if discharge and charge current have been initialized, and are within a specified range, this means
            //that the bike is not being used, therefore, end the data collection rightaway
            if (phidgetdischargecurrent != -Double.NaN && phidgetdischargecurrent >= DISCHARGE_CURRENT_RANGE_LOW &&
                    phidgetdischargecurrent <= DISCHARGE_CURRENT_RANGE_HIGH && phidgetcurrent != -Double.NaN &&
                    phidgetcurrent <= 50) {
                SensorDCLog.data(nowTSAbsolute, GetCurrentData().toString(), this.getClass());
                Log.i("sensordc", "Uninitializing because discharge current " + phidgetdischargecurrent + " is in " +
                        "range [" + DISCHARGE_CURRENT_RANGE_LOW + "," + DISCHARGE_CURRENT_RANGE_HIGH + "]");

                SensorDCLog.DumpDataLogsToDisk();
                Uninitialize();
                return;
            }

            if (nowTs - lastLoggedTs < SENSING_FREQUENCY * milliseconds)//this interrupt is within
            {                                                            //sensingfrequency since last logged
                return;
            }

            if ((nowTs - alarmFireTs) <= SENSING_WINDOW * milliseconds) {
                SensorDCLog.data(nowTSAbsolute, GetCurrentData().toString(), this.getClass());
                lastLoggedTs = nowTs;
                Log.d("sensordc", "ms : " + milliseconds + " " + SENSING_WINDOW + " " + SENSING_FREQUENCY + " " +
                        nowTs / milliseconds);
            } else {
                SensorDCLog.DumpDataLogsToDisk();
                Uninitialize();
            }

        } catch (Exception e) {
            SensorDCLog.e(TAG, "Exception in onsensorevent changed " + e + Log.getStackTraceString(e));
        }

    }

    private void prepareGPS(Context context) {

        try {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationListener_gps = new LocationListener() {
                public void onLocationChanged(Location location) {
                    lat_gps = location.getLatitude();
                    long_gps = location.getLongitude();
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            locationListener_net = new LocationListener() {
                public void onLocationChanged(Location location) {
                    lat_net = location.getLatitude();
                    long_net = location.getLongitude();
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime_loc_gps,
                                                   minDistance_loc_gps, locationListener_gps);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime_loc_net,
                                                   minDistance_loc_net, locationListener_net);

            Location last_gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last_gps != null) {
                lat_gps = last_gps.getLatitude();
                long_gps = last_gps.getLongitude();
            }

            Location last_net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (last_net != null) {
                lat_net = last_net.getLatitude();
                long_net = last_net.getLongitude();
            }

        } catch (Exception e) {
            SensorDCLog.e(TAG, "prepareGPS " + e + Log.getStackTraceString(e));
        }

    }

    private void initPhidget(Context context) {
        try {
            com.phidgets.usb.Manager.Initialize(context);

            // Interface kit (voltage, temperature sensor) stuff
            interfacekit = new InterfaceKitPhidget();
            phidgetattachlistener = new AttachListener() {
                public void attached(final AttachEvent ae) {
                    PhidgetInterfaceKitAttachDetachRunnable handler = new PhidgetInterfaceKitAttachDetachRunnable(ae.getSource(), true);
                    synchronized (handler) {
                        handler.run();
                    }
                }
            };

            phidgetdetachlistener = new DetachListener() {
                public void detached(final DetachEvent ae) {
                    PhidgetInterfaceKitAttachDetachRunnable handler = new PhidgetInterfaceKitAttachDetachRunnable(ae.getSource(), false);
                    synchronized (handler) {
                        phidgetcurrent = Double.NaN;
                        phidgetvoltage = Double.NaN;
                        phidgetambienttemperature = Double.NaN;
                        phidgettemperature = Double.NaN;

                        handler.run();
                    }
                }
            };

            phidgetchangelistener = new SensorChangeListener() {
                public void sensorChanged(SensorChangeEvent se) {

                    int index = se.getIndex();
                    int value = se.getValue();
                    try {
                        // set to finest granularity
                        interfacekit.setSensorChangeTrigger(index, 1);
                    } catch (Exception e) {
                        SensorDCLog.e(TAG, "phidget sensorChanged setSensorChangedTrigger to 1 for index " + index +
                                " " + e);
                    }

                    if (index == 0)
                        phidgetcurrent = value;
                    if (index == 1)
                        phidgetvoltage = (((float) value / 200.00) - 2.5) / (0.0681);
                    if (index == 2)
                        phidgetambienttemperature = interpolateTemperature(value, settings.getT1ambient().getValue(),
                                                                           settings.getT2ambient().getValue(),
                                                                           settings.getV1battery().getValue(),
                                                                           settings.getV2battery().getValue());
                    if (index == 3)
                        phidgettemperature = interpolateTemperature(value, settings.getT1battery().getValue(), settings.getT2battery().getValue(), settings.getV1battery().getValue(), settings.getV2battery().getValue());
                    if (index == 4)
                        phidgetdischargecurrent = value;
                }
            };

            interfacekit.addAttachListener(phidgetattachlistener);
            interfacekit.addDetachListener(phidgetdetachlistener);
            interfacekit.addSensorChangeListener(phidgetchangelistener);
            interfacekit.open(-1);

        } catch (PhidgetException e) {
            SensorDCLog.e(TAG, "initPhidget " + e + Log.getStackTraceString(e));
        } catch (Exception e) {
            SensorDCLog.e(TAG, "initPhidget " + e + Log.getStackTraceString(e));
        }

    }

    // Milad's temperature interpolation
    private float interpolateTemperature(int val, float t1, float t2, float v1, float v2) {
        float a = (t1 - t2) / (v1 - v2);
        float b = (t2 * v1 - t1 * v2) / (v1 - v2);
        return a * val + b;
    }

    private void getIpAddresses() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                ipaddresses = "";
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        ipaddresses = inetAddress.getHostAddress().toString() + ";" + ipaddresses;
                    }
                }
            }
        } catch (Exception e) {
            SensorDCLog.e(TAG, " getIpAddresses " + e + Log.getStackTraceString(e));
        }

    }

    private void getBatteryStatus(Context context) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager
                    .BATTERY_STATUS_FULL;

            // How are we charging?
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float) scale;
            phoneBatteryStatus = isCharging + ":" + usbCharge + ":" + acCharge + ":" + batteryPct;
        } catch (Exception e) {
            SensorDCLog.e(TAG, " getBatteryStatus " + e + Log.getStackTraceString(e));
        }
    }

    private SensorData GetCurrentData() {
        return new SensorData(versionCode, lat_gps, long_gps, lat_net, long_net, maccx, maccy, maccz, magx, magy,
                              magz, gyrx, gyry, gyrz, mpressure, light, proximity, gravity, linaccx, linaccy,
                              linaccz, msteps, phidgettemperature, phidgetambienttemperature, phidgetvoltage,
                              phidgetcurrent, significantMotionTS, ipaddresses, phoneBatteryStatus,
                              phidgetdischargecurrent);
    }

    private int updateVersion(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = pInfo.versionCode;
            return versionCode;

        } catch (Exception e) {
            SensorDCLog.e(TAG, " getVersion " + e + Log.getStackTraceString(e));
            return 0;
        }
    }

    private void HandleReboot() {
        try {
            Time today = new Time(Time.getCurrentTimezone());
            today.setToNow();
            if (today.minute == 20)//
            {
                String nowTSAbsolute = SensorDCLog.getCurrentTimeStamp();
                SensorDCLog.data(nowTSAbsolute, "Reboot", this.getClass());
                SensorDCLog.DumpDataLogsToDisk();

                final String command = "reboot";
                Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                proc.waitFor();
            }

        } catch (Exception e) {
            Log.e(TAG, "reboot " + e);
            SensorDCLog.e(TAG, "reboot " + e);
        }
    }

}