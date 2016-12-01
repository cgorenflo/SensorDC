package com.example.wobistdu;

import java.util.Timer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class WobistduService<E> extends Service {

	public static long period;
	private static String TAG = "Wobistdu";



	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		super.onCreate();
		try {
			
			period=Long.parseLong(this.getString(R.string.AppUpdatePeriod));
			
			Timer timer1 = new Timer("updater", true);
			UpdateTask event1 = new UpdateTask(this);
			timer1.scheduleAtFixedRate(event1, 1000, period);
			
			
			SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			Sensor lightsensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
			LightSensorListener l = new LightSensorListener(getApplicationContext(),this);
			sm.registerListener(l, lightsensor,
					SensorManager.SENSOR_DELAY_NORMAL);
			

		} catch (Exception e) {
			Log.e(TAG, " periodicaction oncreate " + e);
			e.printStackTrace();
		}
	}

	
}
