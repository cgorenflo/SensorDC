package com.sensordc;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import com.sensordc.databinding.ActivityDebugBinding;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Settings;

public class DebugActivity extends Activity {
    private static final String TAG = DebugActivity.class.getSimpleName();
    private DataViewModel displaydata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            Settings settings = new Settings(getSharedPreferences(getResources().getString(R.string
                    .settingPreferenceName), MODE_PRIVATE));

            displaydata = new DataViewModel(this, settings);

            ActivityDebugBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_debug);
            binding.setDisplaydata(this.displaydata);
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Could not show debug screen.", e);
        }
    }

    @Override
    protected void onStop() {
        displaydata.stopUpdates();
        super.onStop();
    }
}
