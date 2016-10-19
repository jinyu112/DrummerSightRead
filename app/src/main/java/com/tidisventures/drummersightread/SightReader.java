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
import java.util.Random;


/** @class SheetMusicActivity
 *
 * The SheetMusicActivity is the main activity. The main components are:
 * - MidiPlayer : The buttons and speed bar at the top.
 * - Piano : For highlighting the piano notes during playback.
 * - SheetMusic : For highlighting the sheet music notes during playback.
 *
 */
public class SightReader extends ActionBarActivity {

    public static final int settingsRequestCode = 1;

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private ArrayList<MidiNote> notes;
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private int lastStartJin;

    //note durations in pulses
    private static final int quarterNote = 96;
    private static final int halfNote = quarterNote * 2;
    private static final int wholeNote = quarterNote * 4;
    private static final int dottedQuarterNote = 96 * 3 / 2;
    private static final int dottedHalfNote = 96 * 3;
    private static final int eighthNote = 96 / 2;
    private static final int dottedEighthNote = eighthNote * 3 / 2;
    private static final int tripletNote = 96 / 3 ;
    private static final int sixteenthNote= 96 / 4;
    private static final int thirtysecondNote= 96 / 8;



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
    private static boolean accentsFlag = false;
    private static boolean flamsFlag = false;
    private static boolean rollsFlag = false;
    private static int numMeasures = 8;
    private static int difficulty = 0; //0 is easiest

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
                accentsFlag = true;
            }
            else accentsFlag = false;
            if (settingsOut[3].equals("1")) {
                rollsFlag = true;
            }
            else rollsFlag = false;
            if (settingsOut[4].equals("1")) {
                flamsFlag = true;
            }
            else flamsFlag = false;
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


            if (settingsOut[11].equals("0")) {
                difficulty = 0;
            }
            else if (settingsOut[11].equals("1")) {
                difficulty = 1;
            }
            else if (settingsOut[11].equals("2")) {
                difficulty = 2;
            }
            else if (settingsOut[11].equals("3")) {
                difficulty = 3;
            }
            else if (settingsOut[11].equals("4")) {
                difficulty = 4;
            }
            else if (settingsOut[11].equals("5")) {
                difficulty = 5;
            }
            else {
                difficulty = 6;
            }


            if (settingsOut[12].equals("4")) {
                numMeasures = 4;
            }
            else if (settingsOut[12].equals("8")) {
                numMeasures = 8;
            }
            else if (settingsOut[12].equals("12")) {
                numMeasures = 12;
            }
            else if (settingsOut[12].equals("16")) {
                numMeasures = 16;
            }
            else if (settingsOut[12].equals("20")) {
                numMeasures = 20;
            }
            else {
                numMeasures = 8;
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
            else if (settingsOut[8].equals("3/8")) {
                timeNum = 3;
                timeDen = 8;
                timeSig = 7;
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
        midifile = genMidiFile(genNotes());
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
        player.setTimeNum(timeNum);
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
        else if (timeSig==7) {
            beat = Beats.one;
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
            tempTempo = tempTempo * 3 / 2; //real tempo (not metronome tempo)
        }
        tempo.setBpm(tempTempo);

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

        com.leff.midi.MidiFile midi = new com.leff.midi.MidiFile(quarterNote, tracks);

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


    private ArrayList<MidiNote> genNotes() {
        ArrayList<MidiNote> tempnotes = new ArrayList<MidiNote>(12);

        //calculate number of pulses in numMeasures
        int totalPulses = numMeasures * timeNum * quarterNote;
        int pulsesPerMeasure = timeNum * quarterNote;
        if (timeDen == 8) {
            totalPulses = totalPulses / 2;
            pulsesPerMeasure = pulsesPerMeasure / 2;
        }

        int remainingPulsesInMeasure = pulsesPerMeasure;
        int runningPulseTime = 0;

        //note probabilities
        int specialNoteSeqProb1 = 0; //0 for selectedNote
        int specialNoteSeqProb2 = 0; //1 for selectedNote
        int sixteenthNoteProb = 0; //2 for selectedNote
        int tripletNoteProb = 0; //3 for selectedNote
        int eighthNoteProb = 0; //20 4 for selectedNote
        int dottedEighthNoteProb = 0; //5 for selectedNote
        int quarterNoteProb = 45; //6 for selectedNote
        int dottedQuarterNoteProb = 0; //5 7 for selectedNote
        int halfNoteProb =  20; //8 for selectedNote
        int dottedhalfNoteProb =  0; //9 for selectedNote
        int wholeNoteProb = 10; //10 for selectedNote

        //accent probabilities
        int accentDraw = 50; // there is a one out of this number chance that an accent is produced in the note sequence
        int[] accentProbs = new int[] {0,1}; //marcato, normal

        //rolls probabilities
        int rollDraw = 50; // there is a one out of this number chance that a roll is produced in the note sequence
        int[] rollProbs = new int[] {0,0,0};

        //flam probabtilties
        int flamDraw = 50; // there is a one out of this number chance that a flam is produced in the note sequence
                          // the smaller this is, the more likely a flam will occur

        //rest probabilities
        int restDraw = 18; //10 the smaller this is, the more likely a rest will occur
        int[] restProbs = new int[] {0,0,0,1,0}; //sixteenth, triplet, eighth, quarter, half


        if (difficulty == 1) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            sixteenthNoteProb = 0; //2 for selectedNote
            tripletNoteProb = 0; //3 for selectedNote
            eighthNoteProb = 20; //20 4 for selectedNote
            dottedEighthNoteProb = 0; //5 for selectedNote
            quarterNoteProb = 45; //6 for selectedNote
            dottedQuarterNoteProb = 5; //5 7 for selectedNote
            halfNoteProb =  20; //8 for selectedNote
            dottedhalfNoteProb =  0; //9 for selectedNote
            wholeNoteProb = 10; //10 for selectedNote

            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 45;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 45;

            restDraw = 15;
            restProbs[0] = 0;
            restProbs[1] = 0;
            restProbs[2] = 2;
            restProbs[3] = 1;
            restProbs[4] = 0;
        }
        else if (difficulty == 2) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            sixteenthNoteProb = 25+15; //2 for selectedNote
            tripletNoteProb = 0; //3 for selectedNote
            eighthNoteProb = 40; //20 4 for selectedNote
            dottedEighthNoteProb = 1*0; //5 for selectedNote
            quarterNoteProb = 30*0; //6 for selectedNote
            dottedQuarterNoteProb = 5*0; //5 7 for selectedNote
            halfNoteProb =  0; //8 for selectedNote
            dottedhalfNoteProb =  0; //9 for selectedNote
            wholeNoteProb = 0; //10 for selectedNote

            accentDraw = 40;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 40;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 35;

            restDraw = 15;
            restProbs[0] = 2;
            restProbs[1] = 0;
            restProbs[2] = 10;
            restProbs[3] = 2;
            restProbs[4] = 0;

        }



        int[] restArray = {sixteenthNote, tripletNote, eighthNote, quarterNote, halfNote};

        int[] noteProbabilities = {specialNoteSeqProb1, specialNoteSeqProb2, sixteenthNoteProb,
                tripletNoteProb, eighthNoteProb, dottedEighthNoteProb, quarterNoteProb, dottedQuarterNoteProb,
                halfNoteProb, dottedhalfNoteProb, wholeNoteProb}; //specialnoteseq1 are note sequences with a duration of one quarter note
                                                                  //specialnoteseq2 are note sequences with a duration of two quarter notes
        int[] noteArray = {quarterNote, halfNote, sixteenthNote,
                tripletNote, eighthNote, dottedEighthNote, quarterNote, dottedQuarterNote,
                halfNote, dottedHalfNote, wholeNote}; //must have the same order as noteProbabilities !!!!!!!!!!!!

        while (runningPulseTime < totalPulses) {


            // selecting a rest
            int probRest = 1 + (int)(Math.random() * restDraw);
            int rest;
            boolean downBeatCheck  = remainingPulsesInMeasure % quarterNote == 0; //quarternote downbeat
            boolean upbeatCheck = false;
            boolean sixteenthUpBeatCheck = false;
            if (!downBeatCheck) {
                upbeatCheck = remainingPulsesInMeasure % eighthNote== 0; //eighth note upbeat
                if (!upbeatCheck) {
                    sixteenthUpBeatCheck = remainingPulsesInMeasure % sixteenthNote == 0;
                }
            }

            if (probRest == 1) { //either note or rest
                rest = 0;
                int[] tempRestProbs = new int[restProbs.length];
                System.arraycopy( restProbs, 0, tempRestProbs, 0, restProbs.length );
                if (downBeatCheck) { // don't allow eighth or sixteenth rests on the downbeat, decrease syncopated note sequence
                    tempRestProbs[2] = 0;
                    tempRestProbs[0] = 0;
                }
                else if (upbeatCheck) {
                    tempRestProbs[2] = 100; // on a upbeat, make sure the rest is an eighth rest, decrease syncopated note sequence
                 }
                else if (sixteenthUpBeatCheck) {
                    tempRestProbs[0] = 100; // on a upbeat, make sure the rest is a 16th rest, decrease syncopated note sequence
                }

                int i_rest = rouletteSelect(returnRestProbArray(remainingPulsesInMeasure,tempRestProbs,restArray));
                if (i_rest == 0) {
                    rest = sixteenthNote;
                }
                else if (i_rest == 1) {
                    rest = tripletNote;
                }
                else if (i_rest == 2) {
                    rest = eighthNote;
                }
                else if (i_rest == 3) {
                    rest = quarterNote;
                }
                else if (i_rest == 4){
                    rest = halfNote;
                }
                else { //something wrong
                    rest = 0;
                }
                runningPulseTime += rest;
                remainingPulsesInMeasure = remainingPulsesInMeasure - rest;
            } //a rest was selected
            else { //a note was selected
                MidiNote note = new MidiNote(0,0,0,0);
                note.setChannel(0);
                note.setNumber(60);
                note.setStartTime(runningPulseTime);

                int[] tempNoteProbs = new int[noteProbabilities.length];
                System.arraycopy( noteProbabilities, 0, tempNoteProbs, 0, noteProbabilities.length );
                if (sixteenthUpBeatCheck) {
                    tempNoteProbs[2] = noteProbabilities[2]*20/20; //if on a sixteenth note upbeat, tend to select another sixteenth note
                                                                 // to decrease chances of syncopation
                    tempNoteProbs[6] = 0; //don't select a quarter note on a sixteenthupbeat
                }
                else if (upbeatCheck) { //by definition, upbeatcheck and sixteenthupbeatcheck cannot be both true at same time
                    tempNoteProbs[4] = noteProbabilities[4]*100/50; //if on a eighth not upbeat, tend to select another eighth note
                                                                 // to decrease chances of syncopation
                }

                // selecting a note
                int selectedNote = rouletteSelect(returnNoteProbArray(remainingPulsesInMeasure, tempNoteProbs, noteArray));
                if (selectedNote == 0) { // specialNoteSeq1
                    //note.setDuration(48);
                } else if (selectedNote == 1) { // specialNoteSeq2
                    //note.setDuration(48);
                } else if (selectedNote == 2) { //sixteenthNote
                    note.setDuration(sixteenthNote);
                    runningPulseTime += sixteenthNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - sixteenthNote;
                } else if (selectedNote == 3) { //tripletNote
                    note.setDuration(tripletNote);
                } else if (selectedNote == 4) { //eighthNote
                    note.setDuration(eighthNote);
                    runningPulseTime += eighthNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - eighthNote;
                } else if (selectedNote == 5) {// dottedEighthNote
                    note.setDuration(dottedEighthNote);
                    runningPulseTime += dottedEighthNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - dottedEighthNote;
                } else if (selectedNote == 6) { //quarterNote
                    note.setDuration(quarterNote);
                    runningPulseTime += quarterNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - quarterNote;
                } else if (selectedNote == 7) {// dottedQuarterNote
                    note.setDuration(dottedQuarterNote);
                    runningPulseTime += dottedQuarterNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - dottedQuarterNote;
                } else if (selectedNote == 8) { //halfNote
                    note.setDuration(halfNote);
                    runningPulseTime += halfNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - halfNote;
                } else if (selectedNote == 9) {//dottedhalfNote
                    note.setDuration(dottedHalfNote);
                } else if (selectedNote == 10){ //wholeNote
                    note.setDuration(wholeNote);
                    runningPulseTime += wholeNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - wholeNote;
                }
                else { //shouldn't even get here
                    note.setDuration(thirtysecondNote); //temp command
                }

                //selecting a flam
                if (flamsFlag) {
                    int probFlam = 1 + (int) (Math.random() * flamDraw);
                    if (probFlam == 1) {
                        note.setFlamNum(1);
                    }
                }

                //selecting an accent
                int probAccent = 0;
                if (accentsFlag) {
                    probAccent = 1 + (int) (Math.random() * accentDraw);
                    if (probAccent == 1) {
                        int i_accent = rouletteSelect(accentProbs);
                        if (i_accent != -1) {
                            if (i_accent == 0) {
                                note.setAccentNum(1);
                            } else {
                                note.setAccentNum(2);
                            }
                        }
                    }
                }

                //selecting a roll
                if (rollsFlag) {
                    int probRoll = 1 + (int) (Math.random() * rollDraw);
                    if (probRoll == 1 && probAccent != 1) {
                        int i_roll = rouletteSelect(rollProbs);
                        if (i_roll != -1) {
                            if (i_roll == 0) {
                                note.setRollNum(1);
                            } else if (i_roll == 1) {
                                note.setRollNum(2);
                            } else {
                                note.setRollNum(3);
                            }
                        }
                    }
                }

                tempnotes.add(note);
            } //else, a note was selected

            if (remainingPulsesInMeasure == 0) {
                remainingPulsesInMeasure = pulsesPerMeasure;
            }
            else if (remainingPulsesInMeasure < 0) {
                remainingPulsesInMeasure = pulsesPerMeasure + remainingPulsesInMeasure;
            }


        } //while loop

        notes = tempnotes;
        return tempnotes;
    }


    //this function returns data from the internal storage with information about the settings
    //this is also defined where the settings flags are needed
    public String[] readSettingsDataInternal() {
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0", "0","0","60","4/4","0","0","0","0"};
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



    // Returns the selected index based on the weights(probabilities)
    private int rouletteSelect(int[] weight) {
        int weight_sum = 0;
        for(int i=0; i<weight.length; i++) {
            weight_sum += weight[i];
        }
        if (weight_sum == 0) return -1;

        // get a random value
        double value = randUniformPositive() * weight_sum;
        // locate the random value based on the weights
        for(int i = 0; i < weight.length; i++) {
            value -= weight[i];
            if(value <= 0) return i;
        }
        // only when rounding errors occur
        return weight.length - 1;
    }

    private int[] returnNoteProbArray(int remainPulsesInMeasure, int[] fullNoteProbArray, int[] fullNoteDurationArray) {
        int[] noteProbArray = new int[fullNoteDurationArray.length];
        for (int i = 0; i < fullNoteProbArray.length; i++) {
            if (fullNoteDurationArray[i] <= remainPulsesInMeasure) {
                noteProbArray[i] = fullNoteProbArray[i];
            }
            else noteProbArray[i] = 0;
        }
        return noteProbArray;
    }

    private int[] returnRestProbArray(int remainPulsesInMeasure, int[] fullRestProbArray, int[] fullRestDurationArray) {
        int[] restProbArray = new int[fullRestDurationArray.length];
        for (int i = 0; i < fullRestProbArray.length; i++) {
            if (fullRestDurationArray[i] <= remainPulsesInMeasure) {
                restProbArray[i] = fullRestProbArray[i];
            }
            else restProbArray[i] = 0;
        }
        return restProbArray;
    }


    // Returns a uniformly distributed double value between 0.0 and 1.0
    private double randUniformPositive() {
        // easiest implementation
        return new Random().nextDouble();
    }

}
