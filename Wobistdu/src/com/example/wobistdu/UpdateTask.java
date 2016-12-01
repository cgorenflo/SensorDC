package com.example.wobistdu;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

class UpdateTask extends TimerTask {
    Context context;
    private String TAG = "Wobistdu";

    // eduroam info
    private String EduroamWifiSSID;
    private String WifiIdentity;
    private String WifiPassword;
    private String APPLIST_LINK;


    public UpdateTask(Context context) {
        this.context = context;
        EduroamWifiSSID = context.getString(R.string.WifiSSID);
        WifiIdentity = context.getString(R.string.WifiIdentity);
        WifiPassword = context.getString(R.string.WifiPassword);
        APPLIST_LINK = context.getString(R.string.APPLIST_LINK);

    }


    private void configureWifi() {
        boolean eduroamInRange = false;

        try {

            WifiManager wManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wManager.setWifiEnabled(true);


            List<ScanResult> results = wManager.getScanResults();
            for (ScanResult res : results) {
                if (res.SSID.equals(EduroamWifiSSID)) {
                    eduroamInRange = true;
                }
            }

            //if eduroam not in range. keep scanning
            if (!eduroamInRange) {
                wManager.startScan();
                return;
            }

            //if eduroam in range then lets see if it has been configured
            List<WifiConfiguration> allNets = wManager.getConfiguredNetworks();
            int netID = -1;

            if (allNets != null) {
                for (WifiConfiguration net : allNets) {
                    if (net.SSID.contains(EduroamWifiSSID)) {
                        netID = net.networkId;

                    }
                }
            }

            // if not configured, lets configure it
            if (netID == -1) {

                // Required network config
                WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = '"' + EduroamWifiSSID + '"';
                wifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                enterpriseConfig.setIdentity(WifiIdentity);
                enterpriseConfig.setPassword(WifiPassword);
                enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
                wifiConfig.enterpriseConfig = enterpriseConfig;

                netID = wManager.addNetwork(wifiConfig);
                wManager.disconnect();
            }


            wManager.enableNetwork(netID, true);


        } catch (Exception e) {
            Log.e(TAG, " configureWifi " + e);
        }
    }


    @Override
    public void run() {
        try {
            configureWifi();
            List<APKInfo> applist = getAppList(APPLIST_LINK);
            for (APKInfo apk : applist) {
                handleAppUpdate(apk);
            }
        } catch (Exception e) {
            Log.e(TAG, "run in service " + e);
        }
    }

    private List<APKInfo> getAppList(String applistdownloadlink) {
        List<APKInfo> retVal = new ArrayList<APKInfo>();
        try {
            String line;
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            URI website = new URI(applistdownloadlink);
            request.setURI(website);
            HttpResponse response = httpclient.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            while ((line = in.readLine()) != null) {
                String[] parts = line.split(",");
                String downloadedFilePath = Environment.getExternalStorageDirectory() + File.separator + parts[0];
                retVal.add(new APKInfo(parts[0], parts[1], parts[2], parts[3], downloadedFilePath));
            }

        } catch (Exception e) {
            Log.e(TAG, "readAppList " + e + " applistlink: " + applistdownloadlink);
        }
        return retVal;
    }

    private void handleAppUpdate(APKInfo apkinfo) {
        try {
            if (!download(apkinfo.APKfilepath, apkinfo.APKdownloadLink)) {
                return;
            }

            int downloadedVersion = getDownloadedVersion(apkinfo.APKfilepath);
            int currentVersion = getCurrentVersion(apkinfo.APKpackageName);

            Log.e(TAG, apkinfo.APKfilepath + " updatetask currentVersion, downloadedVersion=" + currentVersion + ","
                    + downloadedVersion);

            //Toast.makeText(context, apkinfo.APKfilepath
            //	+ " updatetask currentVersion, downloadedVersion="
            //+ currentVersion + "," + downloadedVersion, Toast.LENGTH_SHORT).show();;


            if (downloadedVersion > currentVersion) {
                if (installApk(apkinfo.APKfilepath) && !apkinfo.MainActivityPath.equals("none")) {
                    launchMainActivity(apkinfo.MainActivityPath);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "handleAppUpdate " + e + " handleAppUpdate: " + apkinfo.toString());
        }
    }

    private int getDownloadedVersion(String apkpath) {
        try {

            final PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkpath, 0);
            if (info != null) {
                return info.versionCode;
            } else {
                Log.e(TAG, " null from archive info. apkpath: " + apkpath);
                return -1;
            }
        } catch (Exception e) {
            Log.e(TAG, "getDownloadedVersion " + e + " apk path: " + apkpath);
            return -1;
        }
    }

    private int getCurrentVersion(String packageName) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionCode;
        } catch (Exception e) {
            Log.e(TAG, "getCurrentVersion " + e);
            return -1;
        }
    }

    private Boolean download(String target, String downloadlink) {

        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            URI uri = new URI(downloadlink);
            request.setURI(uri);
            HttpResponse response = httpclient.execute(request);

            File file = new File(target);
            if (file.exists()) {
                file.delete();
            }


            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = response.getEntity().getContent();
            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, len);
            }
            fileOutput.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "download " + e + e.getStackTrace());
            return false;
        }
    }

    private boolean installApk(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) {
                try {
                    final String command = "pm install -r " + file.getAbsolutePath();
                    Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
                    proc.waitFor();
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, " installapk " + e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "installApk " + e);
        }
        return false;
    }

    private boolean launchMainActivity(String mainactivity) {
        try {
            final String command = "am start -n " + mainactivity;
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            proc.waitFor();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "launchMainActivity " + e);
        }
        return false;
    }


    private class APKInfo {
        String APKfilename;
        String APKfilepath;
        String APKdownloadLink;
        String APKpackageName;
        String MainActivityPath;

        public String getAPKfilename() {
            return APKfilename;
        }

        public String getAPKfilepath() {
            return APKfilepath;
        }

        public String getAPKdownloadLink() {
            return APKdownloadLink;
        }

        public String getAPKpackageName() {
            return APKpackageName;
        }

        public APKInfo(String apkfilename, String apkpackagename, String apkdownloadlink, String MainActivityPath,
                       String apkfilepath) {
            this.APKdownloadLink = apkdownloadlink;
            this.APKfilename = apkfilename;
            this.APKfilepath = apkfilepath;
            this.APKpackageName = apkpackagename;
            this.MainActivityPath = MainActivityPath;
        }

        public String getMainActivityPath() {
            return MainActivityPath;
        }

        public String toString() {
            return APKdownloadLink + "," + APKfilename + "," + APKfilepath + "," + APKpackageName + "," +
                    MainActivityPath;
        }
    }
}
