package com.sensordc.sensors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.telephony.TelephonyManager;
import com.sensordc.logging.SensorDCLog;
import com.sensordc.settings.Settings;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.List;

import static android.content.Context.*;

public class DeviceFactory {


    private final String TAG = DeviceFactory.class.getSimpleName();
    private final PhidgetBoard phidgetBoard;
    private final TelephonyManager telephonyManager;
    private final SensorManager sensorManager;
    private final LocationManager locationManager;
    private final Battery battery;
    private boolean useActivityRules;


    public DeviceFactory(Context context, Settings settings, boolean useActivityRules) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        this.sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        this.locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        this.battery = new Battery(context);
        this.useActivityRules = useActivityRules;

        PhidgetSensor sensor = new PhidgetSensor(settings);

        if (useActivityRules) {
            Rule<Measurement> dischargeRule = new Rule<>(new Func1<Measurement, Boolean>() {

                private static final float DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD = 490.00f;
                private static final float DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD = 510.00f;

                @Override
                public Boolean call(Measurement measurement) {
                    return !Float.isNaN(measurement.values[0]) && measurement.values[0] <=
                            DISCHARGE_CURRENT_INACTIVE_LOWER_THRESHOLD && measurement.values[0] >=
                            DISCHARGE_CURRENT_INACTIVE_HIGHER_THRESHOLD;

                }
            });
            sensor.addDischargeRule(dischargeRule);

            Rule<Measurement> chargeRule = new Rule<>(new Func1<Measurement, Boolean>() {
                private static final float CURRENT_INACTIVE_THRESHOLD = 50f;

                @Override
                public Boolean call(Measurement measurement) {
                    return !Float.isNaN(measurement.values[0]) && measurement.values[0] >= CURRENT_INACTIVE_THRESHOLD;
                }
            });
            sensor.addChargeRule(chargeRule);
        }
        this.phidgetBoard = new PhidgetBoard(context, sensor);
    }

    @SuppressLint("HardwareIds")
    public SensorKit assembleSensorKit(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates, final long
            measurementDelay) {
        SensorKit kit = new SensorKit(phidgetBoard, telephonyManager.getDeviceId(), measurementDelay);
        kit.addAccelerationSensor(new PhoneSensor(sensorManager, Sensor.TYPE_LINEAR_ACCELERATION, 3));
        kit.addRotationSensor(new PhoneSensor(sensorManager, Sensor.TYPE_ROTATION_VECTOR, 4));
        kit.addBatterySensor(battery);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        List<String> providers = this.locationManager.getProviders(criteria, true);
        if (!providers.isEmpty()) {
            for (String provider : providers) {
                kit.addLocationSensor(new LocationSensor(this.locationManager, provider, minTimeBetweenGPSUpdates,
                        minDistanceBetweenGPSUpdates));
            }

        } else {
            SensorDCLog.e(TAG, "No location provider found");
        }
        if (useActivityRules) {
            Rule<HashMap<String, Measurement>> fiveSecondRule = new Rule<>(new Func1<HashMap<String, Measurement>,
                    Boolean>() {

                private int executionCount = 0;

                @Override
                public Boolean call(HashMap<String, Measurement> allMeasurements) {
                    if (allMeasurements == null) {
                        executionCount = 0;
                        return false;
                    }
                    executionCount++;
                    return executionCount > 5;
                }
            });
            kit.addActiveStateRule(fiveSecondRule);
        }

        return kit;
    }
}
