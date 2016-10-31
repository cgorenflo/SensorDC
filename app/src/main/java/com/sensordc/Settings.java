package com.sensordc;

import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.ArrayList;

public class Settings {
    private final SharedPreferences storedSettings;
    private Setting t1ambient = new Setting("t1ambient", 25.0f);
    private Setting t2ambient = new Setting("t2ambient", 45.0f);
    private Setting v1ambient = new Setting("v1ambient", 596.0f);
    private Setting v2ambient = new Setting("v2ambient", 636.0f);
    private Setting t1battery = new Setting("t1battery", 25.0f);
    private Setting t2battery = new Setting("t2battery", 45.0f);
    private Setting v1battery = new Setting("v1battery", 596.0f);
    private Setting v2battery = new Setting("v2battery", 636.0f);
    private ArrayList<Setting> allSettings = populateAllSettings();

    Settings(SharedPreferences storedSettings) {
        this.storedSettings = storedSettings;
        load();
    }

    // Needed for data binding to view
    public Setting getT1ambient() {
        return t1ambient;
    }

    // Needed for data binding to view
    public Setting getT2ambient() {
        return t2ambient;
    }

    // Needed for data binding to view
    public Setting getV1ambient() {
        return v1ambient;
    }

    // Needed for data binding to view
    public Setting getV2ambient() {
        return v2ambient;
    }

    // Needed for data binding to view
    public Setting getT1battery() {
        return t1battery;
    }

    // Needed for data binding to view
    public Setting getT2battery() {
        return t2battery;
    }

    // Needed for data binding to view
    public Setting getV1battery() {
        return v1battery;
    }

    // Needed for data binding to view
    public Setting getV2battery() {
        return v2battery;
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

    public class Setting extends BaseObservable {
        private final String settingName;
        private final float defaultValue;
        private float value;

        private Setting(String settingName, float defaultValue) {
            this.settingName = settingName;
            this.defaultValue = defaultValue;
        }

        private void load(SharedPreferences storedSettings) {
            this.setValue(storedSettings.getFloat(this.settingName, this.defaultValue));
        }

        private void save(SharedPreferences.Editor editor) {
            editor.putFloat(this.settingName, getValue());
        }

        float getValue() {
            return value;
        }

        private void setValue(float value) {
            this.value = value;
        }

        @Bindable
        // Needed for data binding to view
        public String getValueString() {
            return String.format("%.2f", value);
        }

        // Needed for data binding to view
        public void setValueString(String value) {
            this.value = Float.valueOf(value);
        }
    }
}
