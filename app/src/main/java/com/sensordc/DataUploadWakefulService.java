package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DataUploadWakefulService extends IntentService {

    private static final String TAG = DataUploadWakefulService.class.getSimpleName();

    public DataUploadWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            String remoteHost = intent.getExtras().getString("remoteHost");
            String remoteUser = intent.getExtras().getString("remoteUser");
            byte[] publicKey = intent.getExtras().getByteArray("publicKey");
            byte[] privateKey = intent.getExtras().getByteArray("privateKey");
            int remotePort = intent.getExtras().getInt("remotePort");
            String deviceID = intent.getExtras().getString("deviceID");

            Toast.makeText(this, "trying upload", Toast.LENGTH_LONG).show();
            PerformUpload(remoteHost, remoteUser, remotePort, deviceID, publicKey, privateKey);
            Toast.makeText(this, "upload done", Toast.LENGTH_LONG).show();
        } finally {
            DataUploadAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void PerformUpload(String remoteHost, String remoteUser, int remotePort, String remoteDirectory, byte[]
            publicKey, byte[] privateKey) {
        try {
            String localDirectory = SensorDCLog.DataLogDirectory;

            Log.i(TAG, "perform upload begins initialized to " + remoteHost + "," + remotePort + "," + remoteUser +
                    "," + remoteDirectory + "," + localDirectory);

            List<File> filesToUpload = getFilesToUpload(localDirectory);
            SFTPConnector connector = new SFTPConnector(remoteHost, remotePort, remoteUser, privateKey, publicKey);
            connector.upload(remoteDirectory, filesToUpload);

            Log.i(TAG, "upload completed to " + remoteHost + "," + remotePort + "," + remoteUser + "," +
                    remoteDirectory + "," + localDirectory);
        } catch (Exception e) {
            SensorDCLog.e(TAG, "PerformUpload " + e + e.getMessage());
        }
    }

    private List<File> getFilesToUpload(String localDirectory) {
        List<File> filesToUpload = new ArrayList<>();
        File directory = new File(localDirectory);

        //Now lets delete things locally, keep 24 files as a buffer
        String currentLogFileName = SensorDCLog.getCurrentFileName();
        //special handling for gps-logger app.
        String currentGPSLogFileName = SensorDCLog.getCurrentFileName_GPSLogger();

        for (File file : directory.listFiles()) {
            if (file.isFile() && !file.getName().equals(currentLogFileName) && !file.getName().equals
                    (currentGPSLogFileName))
                filesToUpload.add(file);
        }

        return filesToUpload;
    }
}
