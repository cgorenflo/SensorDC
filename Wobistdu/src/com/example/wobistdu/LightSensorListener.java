package com.example.wobistdu;

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

public class LightSensorListener implements SensorEventListener {

	float lightlevel;
	Long tsSend;
	Context context;
	private String TAG = "Wobistdu";
	Service service;
	
	public static long DELAY_BETWEEN_SMS ; // in seconds
	public static float LIGHT_LEVEL_THRESHOLD ; // in seconds
	public static String LIGHT_SMS_RECEIVER ;
	
	

	public LightSensorListener(Context context,Service service) {
		tsSend = (long) 0.0;
		this.context=context;
		this.service=service;
		DELAY_BETWEEN_SMS=Long.parseLong(context.getString(R.string.DELAY_BETWEEN_SMS));
		LIGHT_LEVEL_THRESHOLD=Float.parseFloat(context.getString(R.string.LIGHT_LEVEL_THRESHOLD));
		LIGHT_SMS_RECEIVER=context.getString(R.string.LIGHT_SMS_RECEIVER);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		try {
			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				lightlevel = event.values[0];
				
				
				
				if (lightlevel > LIGHT_LEVEL_THRESHOLD
						&& ((System.currentTimeMillis() / 1000) - tsSend) > DELAY_BETWEEN_SMS) {
					Toast.makeText(context, "Beaconing ", 
							   Toast.LENGTH_SHORT).show();
					
					boolean sent = SmsListener.SendLocationBySMS(
							context.getApplicationContext(), LIGHT_SMS_RECEIVER);
					if (sent) {
						tsSend = System.currentTimeMillis() / 1000;
					}
				}

				return;
			}

		} catch (Exception e) {
			Log.e(TAG, " onSensorChanged " + e);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}