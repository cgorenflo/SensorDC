package com.sensordc;

import android.content.SharedPreferences;

import java.util.ArrayList;

public class Settings {
    private static Setting t1ambient = new Setting("t1ambient", 25.0f);
    private static Setting t2ambient = new Setting("t2ambient", 45.0f);
    private static Setting v1ambient = new Setting("v1ambient", 596.0f);
    private static Setting v2ambient = new Setting("v2ambient", 636.0f);
    private static Setting t1battery = new Setting("t1battery", 25.0f);
    private static Setting t2battery = new Setting("t2battery", 45.0f);
    private static Setting v1battery = new Setting("v1battery", 596.0f);
    private static Setting v2battery = new Setting("v2battery", 636.0f);
    private final SharedPreferences storedSettings;
    private ArrayList<Setting> allSettings = populateAllSettings();

    Settings(SharedPreferences storedSettings) {
        this.storedSettings = storedSettings;
        load();
    }

    public static float getT1ambient() {
        return t1ambient.value;
    }

    public static void setT1ambient(String value) {
        t1ambient.value = Float.valueOf(value);
    }

    public static float getT2ambient() {
        return t2ambient.value;
    }

    public static float getV1ambient() {
        return v1ambient.value;
    }

    public static float getV2ambient() {
        return v2ambient.value;
    }

    public static float getT1battery() {
        return t1battery.value;
    }

    public static float getT2battery() {
        return t2battery.value;
    }

    public static float getV1battery() {
        return v1battery.value;
    }

    public static float getV2battery() {
        return v2battery.value;
    }

    private ArrayList<Setting> populateAllSettings() {
        ArrayList<Setting> allSets = new ArrayList<>();

        allSets.add(t1ambient);
        allSets.add(t2ambient);
        allSets.add(v1ambient);
        allSets.add(v2ambient);

        allSets.add(t1battery);
        allSets.add(t2battery);
        allSets.add(v1battery);
        allSets.add(v2battery);

        return allSets;
    }

    private void load() {
        for (Setting setting : allSettings) {
            setting.load(storedSettings);
        }
    }

    void save() {
        SharedPreferences.Editor editor = storedSettings.edit();
        for (Setting setting : allSettings) {
            setting.save(editor);
        }
        editor.apply();
    }

    private static class Setting {
        private final String settingName;
        private final float defaultValue;
        private float value;

        private Setting(String settingName, float defaultValue) {
            this.settingName = settingName;
            this.defaultValue = defaultValue;
        }

        private void load(SharedPreferences storedSettings) {
            this.value = storedSettings.getFloat(this.settingName, this.defaultValue);
        }

        private void save(SharedPreferences.Editor editor) {
            editor.putFloat(this.settingName, value);
        }
    }
}
