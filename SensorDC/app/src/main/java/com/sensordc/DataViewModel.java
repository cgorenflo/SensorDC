package com.sensordc;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataViewModel extends BaseObservable {

    DataViewModel() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay
                (new Runnable() {
                    public void run() {
                        notifyPropertyChanged(BR.latestUpdate);
                    }
                }, 0, 1, TimeUnit.SECONDS);
    }

    @Bindable
    public String getLatestUpdate() {
        return DateFormat.getDateTimeInstance().format(new Date());
    }

    @Bindable
    public String getCodeVersion() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }
}
