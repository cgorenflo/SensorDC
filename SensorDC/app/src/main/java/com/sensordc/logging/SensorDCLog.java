package com.sensordc.logging;

import android.os.Environment;
import android.util.Log;
import com.sensordc.sensors.SensorKit;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class SensorDCLog {

    private static final List<String> dataLogs = new ArrayList<>();
    private static final String TAG = SensorDCLog.class.getSimpleName();
    private static String DataLogDirectory =
            Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sensordc" + File.separator +
            "data";
    private static OutputFormatter formatter = new CSVFormatter();

    public static void w(String tag, String message, Throwable e) {
        Log.w(tag, message, e);
    }

    public static void log(SensorKit sensorKit) {
        String output = formatter.format(getCurrentTimeStamp("yyyy-MM-dd HH:mm:ss.SSS"), sensorKit);
        synchronized (dataLogs) {
            i(TAG, "Logging sensor readings.");
            d(TAG, output);

            dataLogs.add(output);
        }
    }

    private static String getCurrentTimeStamp(String pattern) {
        SimpleDateFormat canadianDateFormat = new SimpleDateFormat(pattern, Locale.CANADA);
        return canadianDateFormat.format(Calendar.getInstance().getTime());
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void d(String tag, String message) {
        Log.d(tag, message);
    }

    public static void flush() {
        i(TAG, "Writing log to storage.");
        synchronized (dataLogs) {
            WriteToFile(dataLogs, tryCreateLogFile(tryCreatePath(), getCurrentFileName()));
            dataLogs.clear();
        }
    }

    private static void WriteToFile(List<String> logEntries, File logFile) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(logFile, true));
            for (String logEntry : logEntries) {
                writer.append(logEntry);
                writer.newLine();
            }
        } catch (IOException exception) {
            e(TAG, "Could not write to log file " + logFile.getName(), exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException exception) {
                    e(TAG, "Closing the log file writer failed", exception);
                }
            }
        }
    }

    @NotNull
    private static File tryCreateLogFile(File path, String filename) {
        File logFile = new File(path, filename);
        try {
            if (logFile.createNewFile())
                WriteHeader(logFile);
        } catch (IOException exception) {
            e(TAG, "Could not create a new log file.", exception);
        } catch (SecurityException exception) {
            e(TAG, "Not allowed to create log file", exception);
        }
        return logFile;
    }

    @NotNull
    private static File tryCreatePath() {
        File path = new File(getDataLogDirectory());
        try {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        } catch (SecurityException exception) {
            e(TAG, "Not allowed to create missing local directories for log file", exception);
        }
        return path;
    }

    public static String getCurrentFileName() {
        return "data" + formatter.getVersionLabel() + "." + getCurrentTimeStamp("yyyy-MM-dd-HH") + ".log";
    }

    public static void e(String tag, String message, Throwable e) {
        Log.e(tag, message, e);
    }

    private static void WriteHeader(File logFile) {
        ArrayList<String> header = new ArrayList<>();
        header.add(formatter.createHeader());
        WriteToFile(header, logFile);
    }

    public static String getDataLogDirectory() {
        return DataLogDirectory;
    }

    public static String getCurrentFileName_GPSLogger() {
        return "gps-" + getCurrentTimeStamp("yyyy-MM-dd-HH") + ".txt";
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }
}
