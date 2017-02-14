package com.sensordc;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import com.sensordc.databinding.ActivityDebugBinding;

public class DebugActivity extends Activity {
    private static final String TAG = DebugActivity.class.getSimpleName();
    private DataViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            bindSettingsToViews();
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Could not show debug screen.", e);
        }
    }

    private void bindSettingsToViews() {
        ActivityDebugBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_debug);
        this.viewModel = new DataViewModel();
        binding.setDisplaydata(this.viewModel);
    }
}
