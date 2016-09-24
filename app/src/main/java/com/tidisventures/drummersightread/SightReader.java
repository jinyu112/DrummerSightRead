package com.tidisventures.drummersightread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leff.midi.*;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/** @class SheetMusicActivity
 *
 * The SheetMusicActivity is the main activity. The main components are:
 * - MidiPlayer : The buttons and speed bar at the top.
 * - Piano : For highlighting the piano notes during playback.
 * - SheetMusic : For highlighting the sheet music notes during playback.
 *
 */
public class SightReader extends ActionBarActivity {

    public static final String MidiDataID = "MidiDataID";
    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;
    private String filename = "mySettings";

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private ArrayList<MidiNote> notes;
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;      /* CRC of the midi bytes */
    private int lastStartJin;


    //added by jin 8/26/16 from midifile
    /* The list of Midi Events */
    public static final byte EventNoteOff         = (byte)0x80;
    public static final byte EventNoteOn          = (byte)0x90;
    public static final byte EventKeyPressure     = (byte)0xA0;
    public static final byte EventControlChange   = (byte)0xB0;
    public static final byte EventProgramChange   = (byte)0xC0;
    public static final byte EventChannelPressure = (byte)0xD0;
    public static final byte EventPitchBend       = (byte)0xE0;
    public static final byte SysexEvent1          = (byte)0xF0;
    public static final byte SysexEvent2          = (byte)0xF7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        //read in the settings from persistent memory
        if (fileExistance(filename)) {
            String[] settingsOut = readSettingsDataInternal();
            if (settingsOut[0].equals("1")) {
                Log.d("Drumm16","Metrnome on"); //metronome
            }
            if (settingsOut[1].equals("1")) {
                Log.d("Drumm16", "syncop on"); //syncopation
            }
            if (settingsOut[2].equals("1")) {
                Log.d("Drumm16", "accents on"); //accents
            }
            if (settingsOut[3].equals("1")) {
                Log.d("Drumm16","rolls on"); //rolls
            }
            if (settingsOut[4].equals("1")) {
                Log.d("Drumm16", "flams on"); //flams
            }
        }

        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

//        // Parse the MidiFile from the raw bytes
//        byte[] data = this.getIntent().getByteArrayExtra(MidiDataID);
//        String title = this.getIntent().getStringExtra(MidiTitleID);
//        this.setTitle("MidiSheetMusic: " + title);
//        try {
//            midifile = new MidiFile(data, title);
//        }
//        catch (MidiFileException e) {
//            this.finish();
//            return;
//        }
//
//        // Initialize the settings (MidiOptions).
//        // If previous settings have been saved, used those
        midifile = genMidiFile(genNotesMain());
        options = new MidiOptions(midifile);
//        CRC32 crc = new CRC32();
//        crc.update(data);
//        midiCRC = crc.getValue();
        SharedPreferences settings = getPreferences(0);
        options.scrollVert = settings.getBoolean("scrollVert", false);
        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
//        String json = settings.getString("" + midiCRC, null);
//        MidiOptions savedOptions = MidiOptions.fromJson(json);
//        if (savedOptions != null) {
//            options.merge(savedOptions);
//        }
        createView();
        createSheetMusic(options);

//        Button butt = (Button) findViewById(R.id.butt);
//
//        butt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(), SightReader.class);
//                startActivity(i);
//                finish();
//            }
//
//        });
    }

    /* Create the MidiPlayer and Piano views */
    void createView() {
        layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        player = new MidiPlayer(this);
        //piano = new Piano(this);
        layout.addView(player);
        //layout.addView(piano);
        setContentView(layout);
        //player.SetPiano(piano);
        layout.requestLayout();
    }

    /** Create the SheetMusic view with the given options */
    private void
    createSheetMusic(MidiOptions options) {
        if (sheet != null) {
            layout.removeView(sheet);
        }
//        if (!options.showPiano) {
//            piano.setVisibility(View.GONE);
//        }
//        else {
//            piano.setVisibility(View.VISIBLE);
//        }
        sheet = new SheetMusic(this);
        //sheet.init(midifile, options);

        sheet.setNotes(notes);
        sheet.setLastStartJin(lastStartJin);
        sheet.init2(options);


        //sheet.setPlayer(player);
        layout.addView(sheet);
        //piano.SetMidiFile(midifile, options, player);
        //piano.SetShadeColors(options.shade1Color, options.shade2Color);

//        midifile = new MidiFile("adsf",genEvents(sheet.getNotes()),sheet.getTracks(),(short) 0,sheet.getTime(),96,96*4*12,false);

        //player.SetMidiFile(midifile, options, sheet);
        player.SetMidiFile(midifile, options, sheet);

        layout.requestLayout();
        sheet.callOnDraw();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public MidiFile genMidiFile(ArrayList<MidiNote> tempnotes) {
        com.leff.midi.MidiTrack tempoTrack = new com.leff.midi.MidiTrack();
        com.leff.midi.MidiTrack noteTrack = new com.leff.midi.MidiTrack();

// 2. Add events to the tracks
// Track 0 is the tempo map
        com.leff.midi.event.meta.TimeSignature ts = new com.leff.midi.event.meta.TimeSignature();
        ts.setTimeSignature(4, 4, 384,
                128);

        Tempo tempo = new Tempo();
        tempo.setBpm(60);

        //com.leff.midi.event.Controller controllerEvent = new com.leff.midi.event.Controller(0,0,64,127);
        //tempoTrack.insertEvent(controllerEvent);
        tempoTrack.insertEvent(tempo);
        tempoTrack.insertEvent(ts);

// Track 1 will have some notes in it
        int NOTE_COUNT = tempnotes.size();

        for(int i = 0; i < NOTE_COUNT; i++)
        {
            int channel = 0, velocity = 100;
            int pitch = tempnotes.get(i).getNumber();
            int noteStart = tempnotes.get(i).getStartTime();
            int noteDuration = tempnotes.get(i).getDuration();
            NoteOn on = new NoteOn(noteStart, channel, pitch , velocity);
            NoteOff off = new NoteOff(noteStart + noteDuration, channel, pitch, 0);

            noteTrack.insertEvent(on);
            noteTrack.insertEvent(off);

            // There is also a utility function for notes that you should use
            // instead of the above.
            noteTrack.insertNote(channel, pitch , velocity, noteStart, noteDuration);
        }

// 3. Create a MidiFile with the tracks we created
        ArrayList<com.leff.midi.MidiTrack> tracks = new ArrayList<com.leff.midi.MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        com.leff.midi.MidiFile midi = new com.leff.midi.MidiFile(96, tracks);

// 4. Write the MIDI data to a file
        try
        {
            FileOutputStream fos = new FileOutputStream(new File(getExternalFilesDir(null), "exampleout.mid"));
            midi.writeToFile(fos);

        }
        catch(IOException e)
        {
            System.err.println(e);
        }


        //convert exampleout.mid to regular midi class obj
        byte[] data = returnData("exampleout.mid");
        String title = "asdf";
        MidiFile tempmidifile = new MidiFile(data,title);

        return tempmidifile;
    }


    private byte[] returnData(String name) {
        try {

            //FileInputStream in = this.openFileInput(name);
            FileInputStream in = new FileInputStream(new File(getExternalFilesDir(null), "exampleout.mid"));


            byte[] data = new byte[4096];
            int total = 0, len = 0;
            while (true) {
                len = in.read(data, 0, 4096);
                if (len > 0)
                    total += len;
                else
                    break;
            }
            in.close();
            data = new byte[total];
            FileInputStream in1 = new FileInputStream(new File(getExternalFilesDir(null), "exampleout.mid"));
            int offset = 0;
            while (offset < total) {
                len = in1.read(data, offset, total - offset);
                if (len > 0)
                    offset += len;
            }
            in1.close();

            return data;
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(this, "CheckFile: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
        catch (MidiFileException e) {
            Toast toast = Toast.makeText(this, "CheckFile midi: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
        Log.d("Drum13", "here bad data");
        return null;
    }

    private ArrayList<MidiNote> genNotesMain() {
        ArrayList<MidiNote> tempnotes = new ArrayList<MidiNote>(12);

        int numNotes=8*3;

        int runningStartTime = 0;
        for (int i = 0; i < numNotes; i++) {

            MidiNote note = new MidiNote(0,0,0,0);
            note.setChannel(0);
            note.setDuration(48);
            note.setNumber(60);
            note.setStartTime(runningStartTime);

            //accents
            int randomNumAccent = 1 + (int)(Math.random() * 8);
            if (randomNumAccent==1) {
                note.setAccentNum(1);
            }
            else if (randomNumAccent==2) {
                note.setAccentNum(2);
            }
            //rolls
            int randomNumRoll = 1 + (int)(Math.random() * 50);
            if (randomNumRoll<6) { //10%
                note.setRollNum(1);
            }
            else if (randomNumRoll<9) { //6%
                note.setRollNum(2);
            }
            else if (randomNumRoll==10) { //2%
                note.setRollNum(3);
            }

            //flams
            int randomNumFlam = 1 + (int)(Math.random() * 5);
            if (randomNumFlam==1) {
                note.setFlamNum(1);
            }

            //notes
            int randomNum = 1 + (int)(Math.random() * 4);
            if (randomNum < 4) {
                runningStartTime += 48;
            }
            else {
                runningStartTime += 96;
            }
            tempnotes.add(note);
        }

        notes=tempnotes;
        return tempnotes;
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
