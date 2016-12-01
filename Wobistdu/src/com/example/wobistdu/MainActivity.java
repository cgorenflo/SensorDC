package com.example.wobistdu;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	

	private static String TAG = "Wobistdu";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences settings = getSharedPreferences("wobistdu", 0);
		String passcode = settings.getString("passcode", SmsListener.onetofive);
		TextView a = (TextView) findViewById(R.id.textView2);
		a.setText(passcode+"\nService running: "+isMyServiceRunning() );
	}
	
	private boolean isMyServiceRunning() {
		String retVal ="none";
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (service.service.getClassName().contains("WobistduService")) {
	            return true;
	        }
	    }
	    return false;
	}

	
}