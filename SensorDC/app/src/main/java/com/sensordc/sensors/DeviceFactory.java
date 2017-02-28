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
            sensor.addDischargeRule(new DischargeRule());
            sensor.addChargeRule(new ChargingRule());
        }
        this.phidgetBoard = new PhidgetBoard(context, sensor);
    }

    @SuppressLint("HardwareIds")
    public SensorKit assembleSensorKit(long minTimeBetweenGPSUpdates, float minDistanceBetweenGPSUpdates, final long
            measurementDelay) {
        SensorKit kit = new SensorKit(phidgetBoard, telephonyManager.getDeviceId(), measurementDelay);
        PhoneSensor accelerationSensor = new PhoneSensor(sensorManager, Sensor.TYPE_LINEAR_ACCELERATION, 3);
        accelerationSensor.addActiveStateRule(new AccelerationRule());
        kit.addAccelerationSensor(accelerationSensor);
        PhoneSensor rotationSensor = new PhoneSensor(sensorManager, Sensor.TYPE_ROTATION_VECTOR, 4);
        rotationSensor.addActiveStateRule(new BlockingRule());
        kit.addRotationSensor(rotationSensor);
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
            kit.addActiveStateRule(new StandByMeasurementsRule());
            kit.addActiveStateRule(new CooldownRule());
        }

        return kit;
    }
}
