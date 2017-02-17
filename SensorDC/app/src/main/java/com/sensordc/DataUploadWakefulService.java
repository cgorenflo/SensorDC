package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import com.sensordc.logging.SensorDCLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataUploadWakefulService extends IntentService {

    private static final String TAG = DataUploadWakefulService.class.getSimpleName();
    private final Handler uiHandler = new Handler();

    public DataUploadWakefulService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            createToast("Starting data upload");
            handleUpload();
            createToast("Data upload successful");
        } catch (Exception e) {
            createToast("Data upload failed");
            SensorDCLog.e(TAG, "Data upload failed.", e);
        } finally {
            DataUploadAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void createToast(final String message) {
        this.uiHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataUploadWakefulService.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpload() {
        SensorDCLog.i(TAG, "Reading ssh connection settings.");

        Resources resources = this.getResources();

        String remoteHost = resources.getString(R.string.remoteHost);
        String remoteUser = resources.getString(R.string.remoteUser);
        int remotePort = resources.getInteger(R.integer.remotePort);

        byte[] publicKey = ReadKey(resources.openRawResource(R.raw.publickey));
        byte[] privateKey = ReadKey(resources.openRawResource(R.raw.privatekey));

        String remoteDirectory = getRemoteDirectoryName();
        String localDirectory = SensorDCLog.getDataLogDirectory();

        SensorDCLog.i(TAG, String.format(Locale.CANADA,
                "Upload starting with %s@%s:%s from local directory %s to remote directory %s", remoteUser, remoteHost,
                remotePort, localDirectory, remoteDirectory));
        SFTPConnector connector = new SFTPConnector(remoteHost, remotePort, remoteUser, privateKey, publicKey);

        upload(connector, localDirectory, remoteDirectory);
    }

    private byte[] ReadKey(InputStream inputStream) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            SensorDCLog.e(TAG, "Could not read private or public key.", e);
            return null;
        }

        return stream.toByteArray();
    }

    private String getRemoteDirectoryName() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //the logs are stored remotely in directories that are named like the correspondent device ID
        return telephonyManager.getDeviceId();
    }

    private void upload(SFTPConnector connector, String localDirectory, String remoteDirectory) {
        try {

            List<File> filesToUpload = getFilesToUpload(localDirectory);
            connector.upload(remoteDirectory, filesToUpload);
            Log.i(TAG, "Upload successful.");

        } catch (Exception e) {
            SensorDCLog.e(TAG, "Upload failed ", e);
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
            if (file.isFile() && !file.getName().equals(currentLogFileName) &&
                !file.getName().equals(currentGPSLogFileName))
                filesToUpload.add(file);
        }

        return filesToUpload;
    }

}
