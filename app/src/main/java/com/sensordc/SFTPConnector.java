package com.sensordc;

import android.util.Log;
import com.jcraft.jsch.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class SFTPConnector {
    // 0600 in octal system
    private static final int OWNER_CAN_READ_AND_WRITE_PERMISSION = 384;
    private static final String TAG = SFTPConnector.class.getSimpleName();
    private final String remoteHost;
    private final int remotePort;
    private final String remoteUser;

    private final JSch ssh;
    private Session session;
    private ChannelSftp channel;

    SFTPConnector(String remoteHost, int remotePort, String remoteUser, byte[] privateKey, byte[] publicKey) {

        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.remoteUser = remoteUser;

        JSch.setConfig("StrictHostKeyChecking", "no");
        this.ssh = new JSch();
        try {
            this.ssh.addIdentity(remoteUser, privateKey, publicKey, null);
        } catch (JSchException e) {
            SensorDCLog.e(TAG, Log.getStackTraceString(e));
        }
    }

    void upload(String remoteDirectoryPath, List<File> filesToUpload) {
        try {
            connect();

            tryMakeRemoteDirectory(remoteDirectoryPath);

            List<DataFileInfo> remoteFiles = getRemoteFilesInfo(remoteDirectoryPath);
            for (File file : filesToUpload) {
                if (!remoteFiles.contains(new DataFileInfo(file.getName(), file.length()))) {
                    // To show that the upload is only complete when the permissions are set correctly,
                    // files get the suffix ".uploading" until everything is done
                    String finalFilePath = remoteDirectoryPath + "/" + file.getName();
                    String temporaryFilePath = finalFilePath + ".uploading";

                    upload(file, temporaryFilePath);
                    setPermissions(temporaryFilePath);
                    rename(temporaryFilePath, finalFilePath);

                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        } catch (SftpException | JSchException e) {
            SensorDCLog.e(TAG, "Data upload failed.", e);
        } finally {
            disconnect();
        }
    }

    private void connect() throws JSchException {
        if (this.session == null) {
            this.session = this.ssh.getSession(this.remoteUser, this.remoteHost, this.remotePort);
        }
        this.session.connect();

        // Create new channel, in case existing session was disconnected
        this.channel = (ChannelSftp) this.session.openChannel("sftp");
        this.channel.connect();
    }

    private void tryMakeRemoteDirectory(String directoryName) {
        // Checking if the directory exists introduces additional network traffic and
        // the exception has to be handled anyway, so no checking if it already exists
        try {
            this.channel.mkdir(directoryName);
        } catch (SftpException e) {
            SensorDCLog.w(TAG, "Creating remote directory failed. Disregard if already exists.", e);
        }
    }

    private List<DataFileInfo> getRemoteFilesInfo(String remoteDirectoryPath) throws SftpException {
        List<DataFileInfo> remoteFiles = new ArrayList<>();

        @SuppressWarnings("unchecked") List<ChannelSftp.LsEntry> remoteDirectory = this.channel.ls(remoteDirectoryPath);
        for (ChannelSftp.LsEntry file : remoteDirectory) {
            long fileSize = file.getAttrs().getSize();
            remoteFiles.add(new DataFileInfo(file.getFilename(), fileSize));
        }
        return remoteFiles;
    }

    private void upload(File file, String temporaryFilePath) throws SftpException {
        this.channel.put(file.getAbsolutePath(), temporaryFilePath);
    }

    private void setPermissions(String temporaryFilePath) throws SftpException {
        this.channel.chmod(OWNER_CAN_READ_AND_WRITE_PERMISSION, temporaryFilePath);
    }

    private void rename(String sourcePath, String targetPath) throws SftpException {
        this.channel.rename(sourcePath, targetPath);
    }

    private void disconnect() {
        if (this.channel != null) {
            this.channel.disconnect();
        }
        if (this.session != null) {
            this.session.disconnect();
        }
    }
}
