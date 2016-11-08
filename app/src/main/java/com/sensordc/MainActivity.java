package com.sensordc;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.sensordc.databinding.ActivityMainBinding;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "SensorDCPrefs";
    private static final String TAG = MainActivity.class.getSimpleName();
    private Settings settings;

    public void clickSave(View saveButton) {
        this.settings.save();
        showToastNotification();
    }

    private void showToastNotification() {
        Toast toast = Toast.makeText(this, "Saved. Good job!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            sendBroadcast(new Intent(this, MainReceiver.class));
            bindSettingsToViews();
        } catch (Exception e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void bindSettingsToViews() {
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.settings = new Settings(getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        binding.setSettings(this.settings);
    }
}
