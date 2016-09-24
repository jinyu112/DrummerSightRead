package com.tidisventures.drummersightread;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Settings extends ActionBarActivity {

    private String filename = "mySettings";
    private static CheckBox cb_met;
    private static CheckBox cb_sync;
    private static CheckBox cb_accnt;
    private static CheckBox cb_roll;
    private static CheckBox cb_flam;
    private static CheckBox cb_scroll;
    private static  Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cb_met = (CheckBox) findViewById(R.id.settings_cbmeton);
        cb_sync = (CheckBox) findViewById(R.id.settings_cbsync);
        cb_accnt = (CheckBox) findViewById(R.id.settings_cbaccents);
        cb_roll = (CheckBox) findViewById(R.id.settings_cbrolls);
        cb_flam = (CheckBox) findViewById(R.id.settings_cbflam);
        cb_scroll = (CheckBox) findViewById(R.id.settings_cbscroll);

        spinner = (Spinner) findViewById(R.id.settings_dpzoom);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.zoomSizes, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //default to normal zoom
        spinner.setSelection(1);

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
            if (settingsOut[5].equals("1")) {
                cb_scroll.setChecked(true);
            }

            if (settingsOut[6].equals("0")) {
                spinner.setSelection(0);
            }
            else if (settingsOut[6].equals("1")) {
                spinner.setSelection(1);
            }
            else if (settingsOut[6].equals("2")) {
                spinner.setSelection(2);
            }
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        boolean checked_met = cb_met.isChecked();
        boolean checked_sync = cb_sync.isChecked();
        boolean checked_accnt = cb_accnt.isChecked();
        boolean checked_roll = cb_roll.isChecked();
        boolean checked_flam = cb_flam.isChecked();
        boolean checked_scroll = cb_scroll.isChecked();

        String[] settingsInput = new String[] {"0", "0", "0", "0", "0", "0","0"};
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

        if (checked_scroll) {
            settingsInput[5] = "1"; //scrolling
        }
        else settingsInput[5] = "0";

        if (spinner.getSelectedItemPosition()==0) {
            settingsInput[6] = "0";
        }
        else if (spinner.getSelectedItemPosition()==1) {
            settingsInput[6] = "1";
        }
        else if (spinner.getSelectedItemPosition()==2) {
            settingsInput[6] = "2";
        }

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
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0","0","0"};
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

