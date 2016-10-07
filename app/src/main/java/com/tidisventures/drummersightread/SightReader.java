package com.tidisventures.drummersightread;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;


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

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private ArrayList<MidiNote> notes;
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;      /* CRC of the midi bytes */
    private int lastStartJin;

    //from settings
    private String filename = "mySettings";
    private static boolean scrollVert;
    private static int zoomSetting;
    private static boolean metronomeOn = true;
    private static int tempoInt = 60;
    private static int timeNum = 4;
    private static int timeDen = 4;
    private static int timeSig = 0; //0 is 4/4, 1 is 2/4, 2 is 3/4, and so on
    private static boolean playSoundFlag = false;
    private static boolean shadeNotes = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        //read in the settings from persistent memory
        if (fileExistance(filename)) {
            String[] settingsOut = readSettingsDataInternal();
            if (settingsOut[0].equals("1")) {
                metronomeOn = true;
            }
            else metronomeOn = false;
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
            if (settingsOut[5].equals("1")) {
                 scrollVert = true;//scrolling is vertical (defaults to horizontal)
            }
            else scrollVert = false;

            if (settingsOut[9].equals("1")) {
                playSoundFlag = true;//playsound
            }
            else playSoundFlag = false;

            if (settingsOut[10].equals("1")) {
                shadeNotes = true;//shadenotes
            }
            else shadeNotes = false;

            zoomSetting = 1;
            if (settingsOut[6].equals("0")) {
                zoomSetting = 0;
            }
            else if (settingsOut[6].equals("1")) {
                zoomSetting = 1;
            }
            else if (settingsOut[6].equals("2")) {
                zoomSetting = 2;
            }

            if (settingsOut[7] != null) {
                String temp = settingsOut[7];
                temp = temp.replaceAll("[$,.-]", "");
                try {
                    tempoInt = Integer.parseInt(temp);
                }catch(Exception e) {tempoInt = 60;}
            }


            if (settingsOut[8].equals("4/4")) {
                timeNum = 4;
                timeDen = 4;
                timeSig = 0;
            }
            else if (settingsOut[8].equals("2/4")) {
                timeNum = 2;
                timeDen = 4;
                timeSig = 1;
            }
            else if (settingsOut[8].equals("3/4")) {
                timeNum = 3;
                timeDen = 4;
                timeSig = 2;
            }
            else if (settingsOut[8].equals("3/8")) {
                timeNum = 3;
                timeDen = 8;
                timeSig = 3;
            }
            else if (settingsOut[8].equals("6/4")) {
                timeNum = 6;
                timeDen = 4;
                timeSig = 4;
            }
            else if (settingsOut[8].equals("12/8")) {
                timeNum = 12;
                timeDen = 8;
                timeSig = 5;
            }
            else if (settingsOut[8].equals("6/8")) {
                timeNum = 6;
                timeDen = 8;
                timeSig = 6;
            }
            else {
                timeNum = 4;
                timeDen = 4;
                timeSig = 0;
            }
        }

        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);


//        // Initialize the settings (MidiOptions).
//        // If previous settings have been saved, used those
        midifile = genMidiFile(genNotesMain());
        options = new MidiOptions(midifile);
        SharedPreferences settings = getPreferences(0);
        if (scrollVert) {
            options.setScrollVert(true);
        } else {
            options.setScrollVert(false);
        }

        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
        createView();
        createSheetMusic(options);

    }

    /* Create the MidiPlayer and Piano views */
    void createView() {
        layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        player = new MidiPlayer(this);
        layout.addView(player);
        setContentView(layout);
        layout.requestLayout();
    }

    /** Create the SheetMusic view with the given options */
    private void
    createSheetMusic(MidiOptions options) {
        if (sheet != null) {
            layout.removeView(sheet);
        }

        sheet = new SheetMusic(this);


        sheet.setNotes(notes);
        sheet.setLastStartJin(lastStartJin);
        if (zoomSetting==0) {
            sheet.setZoom(0.5f);
        }
        else if (zoomSetting==1) {
            sheet.setZoom(0.75f);
        } else if (zoomSetting==2) {
            sheet.setZoom(1.0f);
        }

        sheet.setTempoInt(tempoInt);

        sheet.setTimeNum(timeNum);
        sheet.setTimeDen(timeDen);
        sheet.setShadeNotes(shadeNotes);
        sheet.init2(options);


        //sheet.setPlayer(player);
        layout.addView(sheet);

        //player.SetMidiFile(midifile, options, sheet);
        player.setTempo((short) tempoInt);
        player.setTimeDen(timeDen);
        player.SetMidiFile(midifile, options, sheet);
        player.setMetronomeOn(metronomeOn);
        player.setPlaySoundFlag(playSoundFlag);

        Beats beat;
        if (timeSig==0) {
            beat = Beats.four;
            player.setBeats(beat);
        }
        else if (timeSig==1) {
            beat = Beats.two;
            player.setBeats(beat);
        }
        else if (timeSig==2) {
            beat = Beats.three;
            player.setBeats(beat);
        }
        else if (timeSig==3) {
            beat = Beats.one;
            player.setBeats(beat);
        }
        else if (timeSig==4) {
            beat = Beats.six;
            player.setBeats(beat);
        }
        else if (timeSig==5) {
            beat = Beats.four;
            player.setBeats(beat);
        }
        else if (timeSig==6) {
            beat = Beats.two;
            player.setBeats(beat);
        }
        sheet.setPlayer(player);



        layout.requestLayout();
        sheet.callOnDraw();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
//
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
        ts.setTimeSignature(timeNum, timeDen, 384, 128);

        Tempo tempo = new Tempo();
        float tempTempo = tempoInt;
        if (timeDen == 8) {
            tempTempo = tempTempo * 3 / 2;
        }
        tempo.setBpm(tempTempo);

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
        return null;
    }

    private ArrayList<MidiNote> genNotesMain() {
        ArrayList<MidiNote> tempnotes = new ArrayList<MidiNote>(12);

        int numNotes=8*4;

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
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0","0"};
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

    @Override
    public void onStop() {
        super.onStop();
        sheet.stopMusic();
    }

}
