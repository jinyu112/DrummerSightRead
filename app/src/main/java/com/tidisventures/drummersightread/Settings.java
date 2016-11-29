package com.tidisventures.drummersightread;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
    private static CheckBox cb_sound;
    private static CheckBox cb_shade;
    private static CheckBox cb_trip;
    private static  Spinner spinner;
    private static  Spinner spinnerTS;
    private static  Spinner spinnerDiff;
    private static  Spinner spinnerMeas;
    private static  Spinner spinnerPM; //practice mode spinner
    private static EditText evtempo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        cb_met = (CheckBox) findViewById(R.id.settings_cbmeton);
        cb_met.setChecked(true);
        cb_sync = (CheckBox) findViewById(R.id.settings_cbsync);
        cb_accnt = (CheckBox) findViewById(R.id.settings_cbaccents);
        cb_accnt.setChecked(true);
        cb_roll = (CheckBox) findViewById(R.id.settings_cbrolls);
        cb_roll.setChecked(true);
        cb_flam = (CheckBox) findViewById(R.id.settings_cbflam);
        cb_flam.setChecked(true);
        cb_scroll = (CheckBox) findViewById(R.id.settings_cbscroll);
        cb_sound = (CheckBox) findViewById(R.id.settings_cbsound);
        cb_sound.setChecked(true);
        cb_shade = (CheckBox) findViewById(R.id.settings_cbshade);
        cb_shade.setChecked(true);
        cb_trip = (CheckBox) findViewById(R.id.settings_cbtrip);
        evtempo = (EditText) findViewById(R.id.settings_evtempo);
        evtempo.setGravity(Gravity.CENTER);

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


        spinnerTS = (Spinner) findViewById(R.id.settings_dpts);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterTS = ArrayAdapter.createFromResource(this,
                R.array.timesig, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterTS.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerTS.setAdapter(adapterTS);

        //default to 4/4
        spinnerTS.setSelection(0);


        spinnerDiff = (Spinner) findViewById(R.id.settings_dpdiff);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterDiff = ArrayAdapter.createFromResource(this,
                R.array.difficulty, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterDiff.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerDiff.setAdapter(adapterDiff);

        //default to normal zoom
        spinnerDiff.setSelection(0);


        spinnerMeas = (Spinner) findViewById(R.id.settings_dpmeasures);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterMeas = ArrayAdapter.createFromResource(this,
                R.array.measures, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterMeas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerMeas.setAdapter(adapterMeas);

        //default to 8 measures
        spinnerMeas.setSelection(0);



        spinnerPM = (Spinner) findViewById(R.id.settings_pmspinner);

// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterPM = ArrayAdapter.createFromResource(this,
                R.array.pm, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapterPM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinnerPM.setAdapter(adapterPM);

        //default to normal practice mode
        spinnerPM.setSelection(0);


        spinnerPM.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    spinnerTS.setSelection(0);
                    cb_sync.setChecked(false);
                    cb_trip.setChecked(false);
                }
                if (position == 2) {
                    cb_trip.setChecked(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView){
                return;
            }

        });


        if (fileExistance(filename)) {
            String[] settingsOut = readSettingsDataInternal();
            if (settingsOut[0].equals("1")) {
                cb_met.setChecked(true);
            }
            else cb_met.setChecked(false);

            if (settingsOut[1].equals("1")) {
                cb_sync.setChecked(true);
            }
            else cb_sync.setChecked(false);

            if (settingsOut[2].equals("1")) {
                cb_accnt.setChecked(true);
            }
            else cb_accnt.setChecked(false);
            if (settingsOut[3].equals("1")) {
                cb_roll.setChecked(true);
            }
            else cb_roll.setChecked(false);
            if (settingsOut[4].equals("1")) {
                cb_flam.setChecked(true);
            }
            else cb_flam.setChecked(false);

            if (settingsOut[5].equals("1")) {
                cb_scroll.setChecked(true);
            }
            else cb_scroll.setChecked(false);

            if (settingsOut[9].equals("1")) {
                cb_sound.setChecked(true);
            }
            else cb_sound.setChecked(false);

            if (settingsOut[10].equals("1")) {
                cb_shade.setChecked(true);
            }
            else cb_shade.setChecked(false);

            if (settingsOut[13].equals("1")) {
                cb_trip.setChecked(true);
            }
            else cb_trip.setChecked(false);

            if (settingsOut[6].equals("0")) {
                spinner.setSelection(0);
            }
            else if (settingsOut[6].equals("1")) {
                spinner.setSelection(1);
            }
            else if (settingsOut[6].equals("2")) {
                spinner.setSelection(2);
            }

            if (settingsOut[11].equals("0")) {
                spinnerDiff.setSelection(0);
            }
            else if (settingsOut[11].equals("1")) {
                spinnerDiff.setSelection(1);
            }
            else if (settingsOut[11].equals("2")) {
                spinnerDiff.setSelection(2);
            }
            else if (settingsOut[11].equals("3")) {
                spinnerDiff.setSelection(3);
            }
            else if (settingsOut[11].equals("4")) {
                spinnerDiff.setSelection(4);
            }
            else if (settingsOut[11].equals("5")) {
                spinnerDiff.setSelection(5);
            }
            else spinnerDiff.setSelection(6);


            if (settingsOut[12].equals("4")) {
                spinnerMeas.setSelection(0);
            }
            else if (settingsOut[12].equals("8")) {
                spinnerMeas.setSelection(1);
            }
            else if (settingsOut[12].equals("12")) {
                spinnerMeas.setSelection(2);
            }
            else if (settingsOut[12].equals("16")) {
                spinnerMeas.setSelection(3);
            }
            else if (settingsOut[12].equals("20")) {
                spinnerMeas.setSelection(4);
            }
            else {
                spinnerMeas.setSelection(1);
            }


            if (settingsOut[8].equals("4/4")) {
                spinnerTS.setSelection(0);
            }
            else if (settingsOut[8].equals("2/4")) {
                spinnerTS.setSelection(1);
            }
            else if (settingsOut[8].equals("3/4")) {
                spinnerTS.setSelection(2);
            }
            else if (settingsOut[8].equals("6/4")) {
                spinnerTS.setSelection(3);
                if (cb_scroll.isChecked()) {
                    cb_scroll.setChecked(false);
                }
            }
            else if (settingsOut[8].equals("12/8")) {
                spinnerTS.setSelection(4);
                if (cb_scroll.isChecked()) {
                    cb_scroll.setChecked(false);
                }
            }
            else if (settingsOut[8].equals("6/8")) {
                spinnerTS.setSelection(5);
            }
            else if (settingsOut[8].equals("3/8")) {
                spinnerTS.setSelection(6);
            }
            else spinnerTS.setSelection(0);




            if (settingsOut[14].equals("Normal Mode")) {
                spinnerPM.setSelection(0);
            }
            else if (settingsOut[14].equals("8th Note Mode (4/4)")) {
                spinnerPM.setSelection(1);
            }
            else if (settingsOut[14].equals("Triplet Mode (4/4)")) {
                spinnerPM.setSelection(2);
            }
            else if (settingsOut[14].equals("16th Note Mode (4/4)")) {
                spinnerPM.setSelection(3);
            }
            else spinnerPM.setSelection(0);


            evtempo.setText(settingsOut[7]);
        }
        else {
            evtempo.setText("60");
        }


    }

    @Override
    public void onStop() {
        super.onStop();
        saveSettings();
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
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0","0","0","60","4/4","0","0","0","0","0","Normal Mode"};
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

    public void save_settings(View view){
        saveSettings();
        finish();
    }


    private void saveSettings() {
        boolean checked_met = cb_met.isChecked();
        boolean checked_sync = cb_sync.isChecked();
        boolean checked_accnt = cb_accnt.isChecked();
        boolean checked_roll = cb_roll.isChecked();
        boolean checked_flam = cb_flam.isChecked();
        boolean checked_scroll = cb_scroll.isChecked();
        boolean checked_sound = cb_sound.isChecked();
        boolean checked_shade = cb_shade.isChecked();
        boolean checked_trip = cb_trip.isChecked();

        String[] settingsInput = new String[] {"0", "0", "0", "0", "0", "0","0","60","4/4","0","0","0","0","0","Normal Mode"};
        if (checked_met) {
            settingsInput[0] = "1"; //metronome
        }
        else settingsInput[0] = "0";

        if (checked_trip) {
            settingsInput[13] = "1"; //triplets
        }
        else settingsInput[13] = "0";

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

        if (checked_sound) {
            settingsInput[9] = "1"; //sound
        }
        else settingsInput[9] = "0";

        if (checked_shade) {
            settingsInput[10] = "1"; //shade
        }
        else settingsInput[10] = "0";

        if (spinner.getSelectedItemPosition()==0) {
            settingsInput[6] = "0";
        }
        else if (spinner.getSelectedItemPosition()==1) {
            settingsInput[6] = "1";
        }
        else if (spinner.getSelectedItemPosition()==2) {
            settingsInput[6] = "2";
        }

        if (spinnerTS.getSelectedItemPosition()==0) {
            settingsInput[8] = "4/4";
        }
        else if (spinnerTS.getSelectedItemPosition()==1) {
            settingsInput[8] = "2/4";
        }
        else if (spinnerTS.getSelectedItemPosition()==2) {
            settingsInput[8] = "3/4";
        }
        else if (spinnerTS.getSelectedItemPosition()==3) {
            settingsInput[8] = "6/4";
            if (settingsInput[5].equals("1")) {
                settingsInput[5] = "0";
                Toast.makeText(this, "Vertical scrolling in 6/4 is not supported.",
                        Toast.LENGTH_SHORT).show();
                cb_scroll.setChecked(false);
            }
        }
        else if (spinnerTS.getSelectedItemPosition()==4) {
            settingsInput[8] = "12/8";
            if (settingsInput[5].equals("1")) {
                settingsInput[5] = "0";
                Toast.makeText(this, "Vertical scrolling in 12/8 is not supported.",
                        Toast.LENGTH_SHORT).show();
                cb_scroll.setChecked(false);
            }
        }
        else if (spinnerTS.getSelectedItemPosition()==5) {
            settingsInput[8] = "6/8";
        }
        else if (spinnerTS.getSelectedItemPosition()==6) {
            settingsInput[8] = "3/8";
        }
        else  {
            settingsInput[8] = "4/4";
        }

        if (spinnerDiff.getSelectedItemPosition()==0) {
            settingsInput[11] = "0";
        }
        else if (spinnerDiff.getSelectedItemPosition()==1) {
            settingsInput[11] = "1";
        }
        else if (spinnerDiff.getSelectedItemPosition()==2) {
            settingsInput[11] = "2";
        }
        else if (spinnerDiff.getSelectedItemPosition()==3) {
            settingsInput[11] = "3";
        }
        else if (spinnerDiff.getSelectedItemPosition()==4) {
            settingsInput[11] = "4";
        }
        else if (spinnerDiff.getSelectedItemPosition()==5) {
            settingsInput[11] = "5";
        }
        else {
            settingsInput[11] = "6";
        }

        if (spinnerMeas.getSelectedItemPosition()==0) {
            settingsInput[12] = "4";
        }
        else if (spinnerMeas.getSelectedItemPosition()==1) {
            settingsInput[12] = "8";
        }
        else if (spinnerMeas.getSelectedItemPosition()==2) {
            settingsInput[12] = "12";
        }
        else if (spinnerMeas.getSelectedItemPosition()==3) {
            settingsInput[12] = "16";
        }
        else if (spinnerMeas.getSelectedItemPosition()==4) {
            settingsInput[12] = "20";
        }
        else {
            settingsInput[12] = "8";
        }

        if (spinnerPM.getSelectedItemPosition()==0) {
            settingsInput[14] = "Normal Mode";
        }
        else if (spinnerPM.getSelectedItemPosition()==1) {
            settingsInput[14] = "8th Note Mode (4/4)";
            settingsInput[8] = "4/4";
            spinnerTS.setSelection(0);
            cb_sync.setChecked(false);
            cb_trip.setChecked(false);
        }
        else if (spinnerPM.getSelectedItemPosition()==2) {
            settingsInput[14] = "Triplet Mode (4/4)";
            settingsInput[8] = "4/4";
            spinnerTS.setSelection(0);
            cb_sync.setChecked(false);
            cb_trip.setChecked(true);
        }
        else if (spinnerPM.getSelectedItemPosition()==3) {
            settingsInput[14] = "16th Note Mode (4/4)";
            settingsInput[8] = "4/4";
            spinnerTS.setSelection(0);
            cb_sync.setChecked(false);
            cb_trip.setChecked(false);
        }
        else settingsInput[14] = "Normal Mode";


        String evtempo_str = "0";
        evtempo_str = evtempo.getText().toString();
        if (evtempo_str != null) {
            String temp = evtempo_str;
            int tempInt = 0;
            temp = temp.replaceAll("[$,.-]", "");
            try {
                tempInt = Integer.parseInt(temp);
            }catch(Exception e) {tempInt = 60;}

            if(tempInt > 200)  {
                tempInt = 200;
                Toast.makeText(this, "Max tempo is 200 beats per minute.",
                        Toast.LENGTH_SHORT).show();
            }
            else if (tempInt < 30) {
                tempInt = 30;
                Toast.makeText(this, "Min tempo is 30 beats per minute.",
                        Toast.LENGTH_SHORT).show();
            }
            evtempo_str = String.valueOf(tempInt);
        }
        settingsInput[7] = evtempo_str;

        saveTimeDataInternal(settingsInput);
    }
}

