package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataUploadWakefulService extends IntentService {

    private static final String TAG = DataUploadWakefulService.class.getSimpleName();

    public DataUploadWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Resources resources = this.getResources();

            String remoteHost = resources.getString(R.string.remoteHost);
            String remoteUser = resources.getString(R.string.remoteUser);
            int remotePort = resources.getInteger(R.integer.remotePort);

            byte[] publicKey = ReadFromInputStream(resources.openRawResource(R.raw.publickey));
            byte[] privateKey = ReadFromInputStream(resources.openRawResource(R.raw.privatekey));

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            //the logs are stored remotely in directories that are named like the correspondent device ID
            String remoteDirectory = telephonyManager.getDeviceId();
            String localDirectory = SensorDCLog.DataLogDirectory;

            SensorDCLog.i(TAG, String.format(Locale.CANADA,
                    "Upload starting with %s@%s:%s from local directory %s to remote directory %s", remoteUser,
                    remoteHost, remotePort, localDirectory, remoteDirectory));
            Toast.makeText(this, "trying upload", Toast.LENGTH_LONG).show();
            SFTPConnector connector = new SFTPConnector(remoteHost, remotePort, remoteUser, privateKey, publicKey);

            if (PerformUpload(connector, localDirectory, remoteDirectory)) {

                Log.i(TAG, "upload completed to " + remoteHost + "," + remotePort + "," + remoteUser + "," +
                           remoteDirectory + "," + localDirectory);
                Toast.makeText(this, "upload done", Toast.LENGTH_LONG).show();

            } else {

                Log.i(TAG,
                        "upload failed to " + remoteHost + "," + remotePort + "," + remoteUser + "," + remoteDirectory +
                        "," + localDirectory);
                Toast.makeText(this, "upload failed", Toast.LENGTH_LONG).show();

            }
        } finally {
            DataUploadAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private byte[] ReadFromInputStream(InputStream inputStream) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
            return null;
        }

        return stream.toByteArray();
    }

    private boolean PerformUpload(SFTPConnector connector, String localDirectory, String remoteDirectory) {
        try {

            List<File> filesToUpload = getFilesToUpload(localDirectory);

            connector.upload(remoteDirectory, filesToUpload);


        } catch (Exception e) {
            SensorDCLog.e(TAG, "PerformUpload " + e + e.getMessage());
            return false;
        }
        return true;
    }

    private List<File> getFilesToUpload(String localDirectory) {
        List<File> filesToUpload = new ArrayList<>();
        File directory = new File(localDirectory);

        //Now lets delete things locally, keep 24 files as a buffer
        String currentLogFileName = SensorDCLog.getCurrentFileName();
        //special handling for gps-logger app.
        String currentGPSLogFileName = SensorDCLog.getCurrentFileName_GPSLogger();

        for (File file : directory.listFiles()) {
            if (file.isFile() && !file.getName().equals(currentLogFileName) &&
                !file.getName().equals(currentGPSLogFileName))
                filesToUpload.add(file);
        }

        return filesToUpload;
    }

}
