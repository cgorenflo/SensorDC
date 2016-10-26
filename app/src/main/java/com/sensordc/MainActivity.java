package com.sensordc;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.sensordc.databinding.ActivityMainBinding;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "SensorDCPrefs";
    private static final String TAG = "sensordcmainactivity";
    private Settings settings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            restartSensorDCService();
            bindSettingsToViews();
        } catch (Exception e) {
            SensorDCLog.i(TAG, " main activity onCreate " + e);
        }
    }

    private void bindSettingsToViews() {
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.settings = new Settings(getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        binding.setSettings(settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void restartSensorDCService() {
        Intent startServiceIntent = new Intent(this, SensorDCService.class);

        boolean wasStopped = this.stopService(startServiceIntent);
        SensorDCLog.i(TAG, " stopping service return value: " + wasStopped);

        SensorDCLog.i(TAG, " starting service ");
        ComponentName serviceName = this.startService(startServiceIntent);
        String logMessage = "startService return value: " + serviceName;
        if (serviceName == null) {
            SensorDCLog.e(TAG, logMessage);
        } else {
            SensorDCLog.i(TAG, logMessage);
        }
    }


    public void clickSave(View saveButton) {
        this.settings.save();
        ShowToastNotification();
    }

    private void ShowToastNotification() {
        Toast toast = Toast.makeText(this, "Saved. Good job!", Toast.LENGTH_SHORT);
        toast.show();
    }
}
