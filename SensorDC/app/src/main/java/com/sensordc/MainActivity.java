package com.sensordc;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.sensordc.databinding.ActivityMainBinding;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Settings;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "SensorDCPrefs";
    private static final String TAG = MainActivity.class.getSimpleName();
    private Settings settings;

    public void clickSave(View saveButton) {
        SensorDCLog.i(TAG, "Save calibration settings.");
        this.settings.save();
        showToastNotification();
    }

    public void clickDebug(View debugButton) {
        Intent intent = new Intent(this, DebugActivity.class);
        startActivity(intent);
    }

    private void showToastNotification() {
        Toast toast = Toast.makeText(this, "Saved. Good job!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            sendBroadcast(new Intent(this, MainReceiver.class));
        try {
            bindSettingsToViews();
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Could not show calibration settings.", e);
        }
    }

    private void bindSettingsToViews() {
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        this.settings = new Settings(getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        binding.setSettings(this.settings);
    }
}
