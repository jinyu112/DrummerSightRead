package com.tidisventures.drummersightread;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Settings extends ActionBarActivity {

    private String filename = "mySettings";
    static private CheckBox cb_met;
    static private CheckBox cb_sync;
    static private CheckBox cb_accnt;
    static private CheckBox cb_roll;
    static private CheckBox cb_flam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cb_met = (CheckBox) findViewById(R.id.settings_cbmeton);
        cb_sync = (CheckBox) findViewById(R.id.settings_cbsync);
        cb_accnt = (CheckBox) findViewById(R.id.settings_cbaccents);
        cb_roll = (CheckBox) findViewById(R.id.settings_cbrolls);
        cb_flam = (CheckBox) findViewById(R.id.settings_cbflam);

        if (fileExistance(filename)) {
            String[] settingsOut = readSettingsDataInternal();
            if (settingsOut[0].equals("1")) {
                cb_met.setChecked(true);
            }
            if (settingsOut[1].equals("1")) {
                cb_sync.setChecked(true);
            }
            if (settingsOut[2].equals("1")) {
                cb_accnt.setChecked(true);
            }
            if (settingsOut[3].equals("1")) {
                cb_roll.setChecked(true);
            }
            if (settingsOut[4].equals("1")) {
                cb_flam.setChecked(true);
            }
        }
    }

    public void onStop() {
        super.onStop();
        boolean checked_met = cb_met.isChecked();
        boolean checked_sync = cb_sync.isChecked();
        boolean checked_accnt = cb_accnt.isChecked();
        boolean checked_roll = cb_roll.isChecked();
        boolean checked_flam = cb_flam.isChecked();

        String[] settingsInput = new String[] {"0", "0", "0", "0", "0"};
        if (checked_met) {
            settingsInput[0] = "1"; //metronome
        }
        else settingsInput[0] = "0";

        if (checked_sync) {
            settingsInput[1] = "1"; //syncopation
        }
        else settingsInput[1] = "0";

        if (checked_accnt) {
            settingsInput[2] = "1"; //accents
        }
        else settingsInput[2] = "0";

        if (checked_roll) {
            settingsInput[3] = "1"; //rolls
        }
        else settingsInput[3] = "0";

        if (checked_flam) {
            settingsInput[4] = "1"; //flams
        }
        else settingsInput[4] = "0";

        saveTimeDataInternal(settingsInput);
    }

    //use internal storage to save the training time data
    public void saveTimeDataInternal(String[] in) {
        FileOutputStream outputStream;

        if (fileExistance(filename)) {
            //String[] settingsOut = readSettingsDataInternal();

            try {
                outputStream = openFileOutput(filename, this.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(in);
                oos.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                outputStream = openFileOutput(filename, this.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(in);
                oos.close();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //this function returns data from the internal storage with information about the settings
    //this is also defined where the settings flags are needed
    public String[] readSettingsDataInternal() {
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0"};
        try {
            FileInputStream fin = openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(fin);
            settingsOut = (String[]) ois.readObject();
            ois.close();
            fin.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return settingsOut;
    }

    public boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }
}

