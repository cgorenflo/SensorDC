package com.sensordc;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SensorDCLog {

    public static String DataLogDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + File
            .separator + "sensordc" + File.separator + "data";
    private static List<String> datalogs = new ArrayList<>();

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void i(String message) {
        Log.i("sensordclog", message);
    }

    public static void data(String ts, String data, Class cls) {
        synchronized (datalogs) {
            Log.i("sensordclog", data);
            datalogs.add(ts + "," + cls.getName() + "," + data);
        }
    }

    public static void DumpDataLogsToDisk() {
        synchronized (datalogs) {

            for (String line : datalogs) {
                WriteToFile(line + "\n");
            }
            datalogs.clear();
        }
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CANADA);
        return sdf.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH", Locale.CANADA);
        String date_hour = sdf.format(Calendar.getInstance().getTime());
        return "datav2." + date_hour + ".log";
    }

    public static String getCurrentFileName_GPSLogger() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int date = Calendar.getInstance().get(Calendar.DATE);
        String retVal = "gps-" + year + "-" + month + "-" + date + "-" + hour + ".txt";
        Log.d("gpslogger", retVal);
        return retVal;
    }

    private static void WriteToFile(String message) {
        try {
            String filename = getCurrentFileName();
            File path = new File(DataLogDirectory);
            path.mkdirs();

            File myFile = new File(path, filename);
            if (!myFile.exists()) {
                myFile.createNewFile();
            }

            try {
                byte[] data = message.getBytes();

                FileOutputStream fos = new FileOutputStream(myFile, true);
                BufferedOutputStream output = new BufferedOutputStream(fos);
                output.write(data);
                output.flush();
                output.close();
            } catch (FileNotFoundException e) {
                Log.e("sensordc", filename + " FileNotFoundException " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.e("sensordc", "" + e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

}
