package com.sensordc;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.jcraft.jsch.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DataUploadWakefulService extends IntentService {

    private static final String TAG = DataUploadWakefulService.class.getSimpleName();

    public DataUploadWakefulService() {
        super(DataUploadWakefulService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {

            String remotehost = intent.getExtras().getString("remotehost");
            String remoteuser = intent.getExtras().getString("remoteuser");
            byte[] publicKey = intent.getExtras().getByteArray("pubkey");
            byte[] privateKey = intent.getExtras().getByteArray("privkey");

            int remoteport = intent.getExtras().getInt("remoteport");
            String deviceID = intent.getExtras().getString("deviceID");


            String localdir = SensorDCLog.DataLogDirectory;
            String remotedir = deviceID;

            Log.i(TAG, "upload service initialized to " + remotehost + "," + remoteport + "," + remoteuser + "," + "," +
                    "" + "" + "" + remotedir + "," + localdir);

            Toast.makeText(this, "trying upload", Toast.LENGTH_LONG).show();
            PerformUpload(remotehost, remoteuser, remoteport, remotedir, localdir, publicKey, privateKey);
            Toast.makeText(this, "upload done", Toast.LENGTH_LONG).show();
        } finally {
            DataUploadAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    private void PerformUpload(String remotehost, String remoteuser, int remoteport, String remotedir, String
            localdir, byte[] publicKey, byte[] privateKey) {

        //configureWifi();
        try {
            Log.i(TAG, "perform upload begins initialized to " + remotehost + "," + remoteport + "," + remoteuser +
                    "," + remotedir + "," + localdir + "," + remoteport);
            JSch ssh = new JSch();
            JSch.setConfig("StrictHostKeyChecking", "no");
            Session session = ssh.getSession(remoteuser, remotehost, remoteport);

            ssh.addIdentity(remoteuser, privateKey, publicKey, null);

            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp sftp = (ChannelSftp) channel;

            // make directory remote if it does not exist
            this.mkdir(sftp, remotedir);


            // list files in remote directory
            List<DataFileInfo> lsr = new ArrayList<DataFileInfo>();
            List<ChannelSftp.LsEntry> ls_remote = sftp.ls(remotedir);
            for (ChannelSftp.LsEntry l : ls_remote) {
                SftpATTRS attrs = l.getAttrs();
                lsr.add(new DataFileInfo(l.getFilename(), attrs.getSize()));
            }


            channel.disconnect();
            session.disconnect();


            List<File> ls_local = ListDir(localdir);
            List<File> toUpload = new ArrayList<File>();

            String currentlogfilename = SensorDCLog.getCurrentFileName();
            for (File f : ls_local) {
                DataFileInfo local = new DataFileInfo(f.getName(), f.length());
                if (!lsr.contains(local) && !f.getName().equals(currentlogfilename)) {
                    toUpload.add(f);
                }
            }


            session = ssh.getSession(remoteuser, remotehost, remoteport);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;


            for (File f : toUpload) {
                sftp.put(f.getAbsolutePath(), remotedir + "/" + f.getName() + ".uploading");
                sftp.chmod(384, remotedir + "/" + f.getName() + ".uploading");
                sftp.rename(remotedir + "/" + f.getName() + ".uploading", remotedir + "/" + f.getName());
            }


            //Now lets delete things locally, keep 24 files as a buffer
            String currentfilename1 = SensorDCLog.getCurrentFileName();
            //special handling for gps-logger app.
            String currentfilename2 = SensorDCLog.getCurrentFileName_GPSLogger();
            for (int i = 0; i < ls_local.size(); i++) {
                // dont delete files that are current being written to
                if (!ls_local.get(i).getName().equals(currentfilename1) && !ls_local.get(i).getName().equals
                        (currentfilename2))
                    ls_local.get(i).delete();
            }


            channel.disconnect();
            session.disconnect();
            Log.i(TAG, "upload completed to " + remotehost + "," + remoteport + "," + remoteuser + "," + remotedir +
                    "," + localdir + "," + remoteport);
        } catch (SftpException e) {
            SensorDCLog.e(TAG, "Datauploadalarm sftp exception " + e + e.getMessage());
        } catch (Exception e) {
            SensorDCLog.e(TAG, "Datauploadalarm " + e + e.getMessage());
        }
    }


    private List<File> ListDir(String path) {
        List<File> retVal = new ArrayList<File>();
        File f = new File(path);
        File[] files = f.listFiles();
        for (File file : files) {
            retVal.add(file);
        }

        return retVal;
    }

    private void mkdir(ChannelSftp sftp, String dir) {
        SftpATTRS attrs = null;
        try {
            String currentDirectory = sftp.pwd();
            attrs = sftp.stat(currentDirectory + "/" + dir);
        } catch (Exception e) {
            // do nothing
        }

        if (attrs != null) {
            return;
        } else {

            try {
                sftp.mkdir(dir);
            } catch (SftpException e) {
                SensorDCLog.e(TAG, " Datauploadalarm mkdir " + e);
            }
        }
    }
}
