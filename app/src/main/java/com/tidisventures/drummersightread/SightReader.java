package com.tidisventures.drummersightread;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private static final int dottedQuarterNote = quarterNote * 3 / 2;
    private static final int dottedHalfNote = quarterNote * 3;
    private static final int eighthNote = quarterNote / 2;
    private static final int dottedEighthNote = eighthNote * 3 / 2;
    private static final int tripletNote = quarterNote / 3 ;
    private static final int sixteenthNote= quarterNote / 4;
    private static final int sixteenthTripletNote = quarterNote / 6;
    private static final int thirtysecondNote= quarterNote / 8;



    //from settings
    private String filename = "mySettings";
    private static boolean scrollVert;
    private static int zoomSetting;
    private static boolean metronomeOn = true;
    private static int tempoInt = 60;
    private static int timeNum = 4;
    private static int timeDen = 4;
    private static int timeSig = 0; //0 is 4/4, 1 is 2/4, 2 is 3/4, and so on
    private static boolean playSoundFlag = true;
    private static boolean shadeNotes = true;
    private static boolean accentsFlag = true;
    private static boolean flamsFlag = true;
    private static boolean rollsFlag = true;
    private static boolean tripletFlag = false;
    private static int numMeasures = 4;
    private static int difficulty = 0; //0 is easiest
    private static boolean syncoFlag = false;
    private static int practiceMode = 0; //0 = normal, 1 = 8th note, 2 = triplet, 3 = 16th note mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read in the settings from persistent memory
        zoomSetting = 1;
        if (fileExistance(filename)) {
            String[] settingsOut = readSettingsDataInternal();
            if (settingsOut[0].equals("1")) {
                metronomeOn = true;
            }
            else metronomeOn = false;

            if (settingsOut[13].equals("1")) {
                tripletFlag = true;
            }
            else {
                tripletFlag = false;
            }

            if (settingsOut[1].equals("1")) {
                syncoFlag = true; //syncopation
            }
            else syncoFlag = false;

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


            if (settingsOut[14].equals("Normal Mode")) {
                practiceMode = 0;
            }
            else if (settingsOut[14].equals("8th Note Mode (4/4)")) {
                practiceMode = 1;
            }
            else if (settingsOut[14].equals("Triplet Mode (4/4)")) {
                practiceMode = 2;
            }
            else if (settingsOut[14].equals("16th Note Mode (4/4)")) {
                practiceMode = 3;
            }
            else practiceMode = 0;


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
                scrollVert = false;
            }
            else if (settingsOut[8].equals("12/8")) {
                timeNum = 12;
                timeDen = 8;
                timeSig = 5;
                scrollVert = false;
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

//        options.showMeasures = true;
        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
        createView();
        createSheetMusic(options);

        if (practiceMode != 0) {
            sheet.setTimeNum(timeNum);
            sheet.setTimeDen(timeDen);
            Beats beat = Beats.four;
            player.setBeats(beat);
        }

        if (practiceMode == 3) sheet.setSixteenthNoteMode(true);

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
        getMenuInflater().inflate(R.menu.menu_sightreader, menu);
        return true;
    }
//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_saveimage) {
            showSaveImagesDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /* Show the "Save As Images" dialog */
    private void showSaveImagesDialog() {
        LayoutInflater inflator = LayoutInflater.from(this);
        final View dialogView= inflator.inflate(R.layout.save_images_dialog, null);
        final EditText filenameView = (EditText)dialogView.findViewById(R.id.save_images_filename);
        filenameView.setText(midifile.getFileName().replace("_", " ") );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.saveimage);
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface builder, int whichButton) {

                saveAsImages(filenameView.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface builder, int whichButton) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    /* Save the current sheet music as PNG images. */
    private void saveAsImages(String name) {
        String filename = name;
        try {
            filename = URLEncoder.encode(name, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
        }
        if (!options.scrollVert) {
            options.scrollVert = true;
            if ((timeNum == 6 && timeDen == 4) || (timeNum == 12)) {
                options.scrollVert=false; //this is too prevent the
                Toast.makeText(this, "Saving sheet music not supported in 6/4 or 12/8 time signatures.",
                            Toast.LENGTH_LONG).show();
                return;
            }
                                                                                             // difficulty of plotting vertical
                                                                                             // sheet music in 12/8 and 6/4
            createSheetMusic(options);
        }
        try {
            int numpages = sheet.GetTotalPages();
            for (int page = 1; page <= numpages; page++) {
                Bitmap image= Bitmap.createBitmap(SheetMusic.PageWidth + 40, SheetMusic.PageHeight + 40, Bitmap.Config.ARGB_8888);
                Canvas imageCanvas = new Canvas(image);
                sheet.DrawPage(imageCanvas, page);
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/SheetMusic");
                File file = new File(path, "" + filename + page + ".png");
                path.mkdirs();
                OutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 0, stream);
                image = null;
                stream.close();

                // Inform the media scanner about the file
                MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
            }
        }
        catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error saving image to file " + Environment.DIRECTORY_PICTURES + "/SheetMusic/" + filename  + ".png");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
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
        String title = "Filename";
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


    private ArrayList<MidiNote> genNotes() {
        ArrayList<MidiNote> tempnotes = new ArrayList<MidiNote>(12);

        //calculate number of pulses in numMeasures
        int totalPulses = numMeasures * timeNum * quarterNote;
        int pulsesPerMeasure = timeNum * quarterNote;
        if (timeDen == 8 && practiceMode == 0) {
            totalPulses = totalPulses / 2;
            pulsesPerMeasure = pulsesPerMeasure / 2;
        }

        if (practiceMode != 0) {
            timeNum = 4;
            timeDen = 4;
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
        if (timeDen == 8) {
            halfNoteProb = 0;
            wholeNoteProb = 0;
        }

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

        if (timeNum == 3 && timeDen == 8 && difficulty == 0) difficulty = 1;

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
            dottedhalfNoteProb =  3; //9 for selectedNote
            wholeNoteProb = 10; //10 for selectedNote

            if (timeDen == 8) {
                dottedQuarterNoteProb = 15;
                halfNoteProb = 1;
                wholeNoteProb = 0;
                quarterNoteProb = 25;
            }

            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 45;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 45;

            restDraw = 15;
            restProbs[0] = 0; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0;
            restProbs[2] = 8;
            restProbs[3] = 4;
            restProbs[4] = 0;
        }
        else if (difficulty == 2) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            sixteenthNoteProb = 0; //2 for selectedNote
            tripletNoteProb = 0; //3 for selectedNote
            eighthNoteProb = 50; //20 4 for selectedNote
            dottedEighthNoteProb = 0; //5 for selectedNote
            quarterNoteProb = 20; //6 for selectedNote
            dottedQuarterNoteProb = 8; //5 7 for selectedNote
            halfNoteProb =  10; //8 for selectedNote
            dottedhalfNoteProb =  3; //9 for selectedNote
            wholeNoteProb = 5; //10 for selectedNote

            if (timeDen == 8) {
                dottedQuarterNoteProb = 5;
                halfNoteProb = 2;
                wholeNoteProb = 0;
                quarterNoteProb = 25;
            }

            accentDraw = 30;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 45;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 40;

            restDraw = 15;
            restProbs[0] = 0; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0;
            restProbs[2] = 8;
            restProbs[3] = 4;
            restProbs[4] = 0;
        }
        else if (difficulty == 3) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            sixteenthNoteProb = 0; //2 for selectedNote
            tripletNoteProb = 10; //3 for selectedNote
            eighthNoteProb = 50; //20 4 for selectedNote
            dottedEighthNoteProb = 0; //5 for selectedNote
            quarterNoteProb = 20; //6 for selectedNote
            dottedQuarterNoteProb = 8; //5 7 for selectedNote
            halfNoteProb =  10; //8 for selectedNote
            dottedhalfNoteProb =  3; //9 for selectedNote
            wholeNoteProb = 5; //10 for selectedNote
            if (timeDen == 8) {
                dottedQuarterNoteProb = 1;
                halfNoteProb = 1;
                wholeNoteProb = 0;
                tripletNoteProb = 0;
                quarterNoteProb = 5;
            }

            accentDraw = 10;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 3;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 8;

            restDraw = 15;
            restProbs[0] = 0; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0;
            restProbs[2] = 8;
            restProbs[3] = 4;
            restProbs[4] = 0;
        }
        else if (difficulty == 4) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            sixteenthNoteProb = 30; //2 for selectedNote
            tripletNoteProb = 10; //3 for selectedNote
            eighthNoteProb = 50; //20 4 for selectedNote
            dottedEighthNoteProb = 1; //5 for selectedNote
            quarterNoteProb = 10; //6 for selectedNote
            dottedQuarterNoteProb = 5; //5 7 for selectedNote
            halfNoteProb =  5; //8 for selectedNote
            dottedhalfNoteProb =  1; //9 for selectedNote
            wholeNoteProb = 0; //10 for selectedNote
            if (timeDen == 8) {
                dottedQuarterNoteProb = 1;
                halfNoteProb = 1;
                wholeNoteProb = 0;
                tripletNoteProb = 0;
                quarterNoteProb = 0;
            }


            accentDraw = 10;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 10;
            rollProbs[0] = 1;
            rollProbs[1] = 0;
            rollProbs[2] = 0;

            flamDraw = 10;

            restDraw = 8;
            if (!syncoFlag && timeDen == 8) {
                restDraw = 16;
            }

            restProbs[0] = 4; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0; //should always be 0
            restProbs[2] = 8;
            restProbs[3] = 0;
            restProbs[4] = 0;
        }
        else if (difficulty == 5) {
            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 5; //1 for selectedNote
            sixteenthNoteProb = 50; //2 for selectedNote
            tripletNoteProb = 10; //3 for selectedNote
            eighthNoteProb = 30; //20 4 for selectedNote
            dottedEighthNoteProb = 1; //5 for selectedNote
            quarterNoteProb = 5; //6 for selectedNote
            dottedQuarterNoteProb = 5; //5 7 for selectedNote
            halfNoteProb =  1; //8 for selectedNote
            dottedhalfNoteProb =  1; //9 for selectedNote
            wholeNoteProb = 0; //10 for selectedNote
            if (timeDen == 8) {
                dottedQuarterNoteProb = 1;
                halfNoteProb = 1;
                wholeNoteProb = 0;
                tripletNoteProb = 0;
                quarterNoteProb = 0;
            }

            accentDraw = 8;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 8;
            rollProbs[0] = 10;
            rollProbs[1] = 1;
            rollProbs[2] = 0;

            flamDraw = 8;

            restDraw = 8;

            if (!syncoFlag && timeDen == 8) {
                restDraw = 16;
            }
            restProbs[0] = 4; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0; //should always be 0
            restProbs[2] = 8;
            restProbs[3] = 0;
            restProbs[4] = 0;
        }
        else if (difficulty == 6) {
            specialNoteSeqProb1 = 20; //0 for selectedNote
            specialNoteSeqProb2 = 8; //1 for selectedNote
            sixteenthNoteProb = 50; //2 for selectedNote
            tripletNoteProb = 3; //3 for selectedNote
            eighthNoteProb = 36; //20 4 for selectedNote
            dottedEighthNoteProb = 1; //5 for selectedNote
            quarterNoteProb = 1; //6 for selectedNote
            dottedQuarterNoteProb = 5; //5 7 for selectedNote
            halfNoteProb =  0; //8 for selectedNote
            dottedhalfNoteProb =  0; //9 for selectedNote
            wholeNoteProb = 0; //10 for selectedNote
            if (timeDen == 8) {
                dottedQuarterNoteProb = 1;
                halfNoteProb = 1;
                wholeNoteProb = 0;
                tripletNoteProb = 0;
                quarterNoteProb = 0;
            }

            accentDraw = 7;
            accentProbs[0] = 1;
            accentProbs[1] = 5;

            rollDraw = 3;
            rollProbs[0] = 15;
            rollProbs[1] = 1;
            rollProbs[2] = 0;

            flamDraw = 3;

            restDraw = 8;

            if (!syncoFlag && timeDen == 8) {
                restDraw = 16;
            }
            restProbs[0] = 8; //sixteenth, triplet, eighth, quarter, half
            restProbs[1] = 0; //should always be 0
            restProbs[2] = 12;
            restProbs[3] = 0;
            restProbs[4] = 0;
        }


        if (!accentsFlag || !flamsFlag || !rollsFlag) { //don't draw special note group2 if accents and flams and rolls are not set
            specialNoteSeqProb2 = 0;
        }

        if (timeDen == 8) { //don't allow the specialnote groups to be selected whe in X/8 time (may change in the future)
            specialNoteSeqProb1 = 0;
            specialNoteSeqProb2 = 0;
            tripletNoteProb = 0;
            tripletFlag = false;
        }

        if (!tripletFlag) tripletNoteProb = 0;

        if (syncoFlag && difficulty < 1) syncoFlag = false;

        // set practice mode probabilities
        if (practiceMode != 0) { //no rests for 8th note -> 16th note practice modes
            restProbs[0] = 0;
            restProbs[1] = 0;
            restProbs[2] = 0;
            restProbs[3] = 0;
            restProbs[4] = 0;

            specialNoteSeqProb1 = 0; //0 for selectedNote
            specialNoteSeqProb2 = 0; //1 for selectedNote
            dottedEighthNoteProb = 0; //5 for selectedNote
            quarterNoteProb = 0; //6 for selectedNote
            dottedQuarterNoteProb = 0; //5 7 for selectedNote
            halfNoteProb =  0; //8 for selectedNote
            dottedhalfNoteProb =  0; //9 for selectedNote
            wholeNoteProb = 0; //10 for selectedNote
            sixteenthNoteProb = 0; //2 for selectedNote
            tripletNoteProb = 0; //3 for selectedNote
            eighthNoteProb = 0; //20 4 for selectedNote

            if (difficulty == 0) {
                accentDraw = 15;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 15;
                rollProbs[0] = 1;
                rollProbs[1] = 0;
                rollProbs[2] = 0;

                flamDraw = 15;
            }
            else if (difficulty == 1) {
                accentDraw = 10;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 12;
                rollProbs[0] = 1;
                rollProbs[1] = 0;
                rollProbs[2] = 0;

                flamDraw = 15;
            }
            else if (difficulty == 2) {
                accentDraw = 9;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 10;
                rollProbs[0] = 1;
                rollProbs[1] = 0;
                rollProbs[2] = 0;

                flamDraw = 15;
            }
            else if (difficulty == 3) {
                accentDraw = 8;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 8;
                rollProbs[0] = 1;
                rollProbs[1] = 0;
                rollProbs[2] = 0;

                flamDraw = 10;
            }
            else if (difficulty == 4) {
                accentDraw = 6;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 5;
                rollProbs[0] = 1;
                rollProbs[1] = 0;
                rollProbs[2] = 0;

                flamDraw = 8;
            }
            else if (difficulty == 5) {
                accentDraw = 5;
                accentProbs[0] = 1;
                accentProbs[1] = 25;

                rollDraw = 2;
                rollProbs[0] = 25;
                rollProbs[1] = 1;
                rollProbs[2] = 0;

                flamDraw = 4;
            }
            else if (difficulty == 6) {
                accentDraw = 3;
                accentProbs[0] = 1;
                accentProbs[1] = 15;

                rollDraw = 1;
                rollProbs[0] = 25;
                rollProbs[1] = 1;
                rollProbs[2] = 0;

                flamDraw = 4;
            }
        }

        if (practiceMode == 1) {
            eighthNoteProb = 10;
        }
        else if (practiceMode == 2) { //triplets
            tripletFlag = true;
            tripletNoteProb = 10;
        }
        else if (practiceMode == 3) { //triplets
            sixteenthNoteProb = 10;
        }


        int[] restArray = {sixteenthNote, tripletNote, eighthNote, quarterNote, halfNote};

        int[] noteProbabilities = {specialNoteSeqProb1, specialNoteSeqProb2, sixteenthNoteProb,
                tripletNoteProb, eighthNoteProb, dottedEighthNoteProb, quarterNoteProb, dottedQuarterNoteProb,
                halfNoteProb, dottedhalfNoteProb, wholeNoteProb}; //specialnoteseq1 are note sequences with a duration of one quarter note
                                                                  //specialnoteseq2 are note sequences with a duration of two quarter notes
        int[] noteArray = {quarterNote, halfNote, sixteenthNote,
                tripletNote, eighthNote, dottedEighthNote, quarterNote, dottedQuarterNote,
                halfNote, dottedHalfNote, wholeNote}; //must have the same order as noteProbabilities !!!!!!!!!!!!

        //this shifting of the runningPulseTime is for the countoff measures (artificial rests)
       runningPulseTime = pulsesPerMeasure; //start one measure off for count off
        if ( (timeNum == 3) && timeDen == 8) {
            runningPulseTime = 4 * pulsesPerMeasure;
        }
        else if (timeNum == 2 && timeDen == 4) {
            runningPulseTime = 2 * pulsesPerMeasure;
        }
        else if (timeNum == 6 && timeDen == 8) {
            runningPulseTime = 2 * pulsesPerMeasure;
        }
        totalPulses += runningPulseTime;

        while (runningPulseTime < totalPulses) {

            // selecting a rest
            int probRest = 1 + (int)(Math.random() * restDraw);
            if (practiceMode != 0) {
                probRest = -1;
            }
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

            if (timeDen == 8) downBeatCheck = false;// to prevent weird hang up problem
            if (timeDen == 8) upbeatCheck = false; // to prevent weird hang up problem

            if (probRest == 1) { //either note or rest
                rest = 0;
                int[] tempRestProbs = new int[restProbs.length];
                System.arraycopy( restProbs, 0, tempRestProbs, 0, restProbs.length );
                if (downBeatCheck) { // don't allow eighth or sixteenth rests on the downbeat, decrease syncopated note sequence
                    if (syncoFlag && timeDen != 8) {
                        tempRestProbs[2] = 6;
                        tempRestProbs[0] = 4;
                    }
                    else {
                        tempRestProbs[2] = 1;
                        tempRestProbs[0] = 0;
                    }

                    if (difficulty <= 3) {
                        tempRestProbs[0] = 0;
                        tempRestProbs[2] = 0;
                    }
                }
                else if (upbeatCheck) {
                    if (syncoFlag  && timeDen != 8) {
                        //tempRestProbs[2] = 50;
                        tempRestProbs[2] = 8 + (int) (Math.random() * 42);
                    }
                    else tempRestProbs[2] = 200; // on a upbeat, make sure the rest is an eighth rest, decrease syncopated note sequence
                 }
                else if (sixteenthUpBeatCheck) {
                    if (syncoFlag  && timeDen != 8) {
                        //tempRestProbs[0] = 75;
                        tempRestProbs[0] = 15 + (int) (Math.random() * 60);
                    }
                    else tempRestProbs[0] = 200; // on a upbeat, make sure the rest is a 16th rest, decrease syncopated note sequence
                }

                if (difficulty < 4 && timeDen == 8) {
                    tempRestProbs[0] = 0; //don't allow 16th rests for X/8 mode unless high enough difficulty
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
                    if (syncoFlag) {
                        tempNoteProbs[2] = noteProbabilities[2] / 2;
                    }
                    else tempNoteProbs[2] = noteProbabilities[2] * 5; //if on a sixteenth note upbeat, tend to select another sixteenth note
                                                                            // to decrease chances of syncopation
                    tempNoteProbs[6] = 0; //don't select a quarter note on a sixteenthupbeat
                }
                else if (upbeatCheck) { //by definition, upbeatcheck and sixteenthupbeatcheck cannot be both true at same time
                    if (syncoFlag) {
                        tempNoteProbs[4] = noteProbabilities[4] / 2 ;
                    }
                    else tempNoteProbs[4] = noteProbabilities[4] * 5;      //if on a sixteenth note upbeat, tend to select another sixteenth note
                                                                            // to decrease chances of syncopation
                }

                if (difficulty < 4 && timeDen == 8) {
                    tempNoteProbs[0] = 0; //don't allow 16th notes for X/8 mode unless high enough difficulty
                }
                if (difficulty < 1 && timeDen == 8) {
                    tempNoteProbs[2] = 0; //don't allow 8th notes for X/8 mode unless high enough difficulty
                }

                if (difficulty <= 3 && practiceMode == 0 ) {
                    tempNoteProbs[0] = 0;
                    tempNoteProbs[2] = 0;
                }
                // selecting a note
                int selectedNote = rouletteSelect(returnNoteProbArray(remainingPulsesInMeasure, tempNoteProbs, noteArray, downBeatCheck));

                ArrayList<MidiNote> specialNotes = new ArrayList<MidiNote>(1);
                if (selectedNote == 0) { // specialNoteSeq1
                    specialNotes = genSpecialNotes1(runningPulseTime);
                    runningPulseTime += quarterNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - quarterNote;
                } else if (selectedNote == 1) { // specialNoteSeq2
                    specialNotes = genSpecialNotes2(runningPulseTime);
                    runningPulseTime += halfNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - halfNote;
                } else if (selectedNote == 2) { //sixteenthNote
                    note.setDuration(sixteenthNote);
                    runningPulseTime += sixteenthNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - sixteenthNote;
                } else if (selectedNote == 3) { //tripletNote
                    if (practiceMode == 0) {
                        specialNotes = genLowDifficultyTriplets(runningPulseTime);
                        runningPulseTime += quarterNote;
                        remainingPulsesInMeasure = remainingPulsesInMeasure - quarterNote;
                    }
                    else {
                        note.setDuration(tripletNote);
                        runningPulseTime += tripletNote;
                        remainingPulsesInMeasure = remainingPulsesInMeasure - tripletNote;
                    }
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
                    runningPulseTime += dottedHalfNote;
                    remainingPulsesInMeasure = remainingPulsesInMeasure - dottedHalfNote;
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
                int accentAndRoll = 1 + (int) (Math.random() * 10); //likely to not select a roll with an accent
                if (accentAndRoll == 1 && probAccent == 1) probAccent = 2;
                if (rollsFlag) {
                    int probRoll = 1 + (int) (Math.random() * rollDraw);
                    if (probRoll == 1 && probAccent != 1) {
                        int i_roll = rouletteSelect(rollProbs);
                        if (i_roll != -1) {
                            if (i_roll == 0 && note.getDuration() < quarterNote) {
                                note.setRollNum(1);
                            } else if (i_roll == 1 && (note.getDuration() != sixteenthNote && note.getDuration() != tripletNote)) {
                                note.setRollNum(2);
                            } else {
                                if (note.getDuration() >= halfNote) note.setRollNum(3);
                                else if (note.getDuration() <= eighthNote) note.setRollNum(1);
                            }
                        }
                    }
                }

                //whole/dottedhalf note rolls
                if ((note.getDuration() == wholeNote || note.getDuration() == dottedHalfNote || note.getDuration() == halfNote) && rollsFlag) {
                    int wholeNoteRollProb = 1 + (int) (Math.random() * 2);
                    if (wholeNoteRollProb == 1) {
                        note.setRollNum(3);
                    }
                }



                //error catches
                if (note.getDuration() == dottedEighthNote) note.setRollNum(0);

                //adding the special note groups
                if ((selectedNote ==0 || selectedNote == 1 || selectedNote == 3) && practiceMode == 0) {
                    if (specialNotes != null) {
                        if (specialNotes.size() > 0) {
                            for (int ii = 0; ii < specialNotes.size(); ii++) {
                                tempnotes.add(specialNotes.get(ii));
                            }
                        }
                    }
                }
                else {
                    tempnotes.add(note);
                }
            } //else, a note was selected

            if (remainingPulsesInMeasure == 0) {
                remainingPulsesInMeasure = pulsesPerMeasure;
            }
            else if (remainingPulsesInMeasure < 0) {
                remainingPulsesInMeasure = pulsesPerMeasure + remainingPulsesInMeasure;
            }

//            Log.d("Drumm17","runningPulseTime: " + runningPulseTime);
//            Log.d("Drumm17","totalPulses: " + totalPulses);
        } //while loop

        //make the last note an eighth note to avoid that cutoff issue
        MidiNote note = new MidiNote(0,0,0,0);
        note.setChannel(1);
        note.setNumber(60);
        note.setStartTime(runningPulseTime);
        note.setDuration(quarterNote);
        tempnotes.add(note);

        notes = tempnotes;
        return tempnotes;
    }


    //this function returns data from the internal storage with information about the settings
    //this is also defined where the settings flags are needed
    public String[] readSettingsDataInternal() {
        String settingsOut[] = new String[]{"0", "0", "0", "0", "0", "0","0","60","4/4","0","0","0","0","0","Normal Mode"};
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

    private int[] returnNoteProbArray(int remainPulsesInMeasure, int[] fullNoteProbArray, int[] fullNoteDurationArray, boolean downBeatCheck) {
        int[] noteProbArray = new int[fullNoteDurationArray.length];
        for (int i = 0; i < fullNoteProbArray.length; i++) {
            if (fullNoteDurationArray[i] <= remainPulsesInMeasure) {
                noteProbArray[i] = fullNoteProbArray[i];
            }
            else noteProbArray[i] = 0;

            if (!downBeatCheck) {
                noteProbArray[0] = 0; //don't allow specialnote groups to be returned unless on a downbeat
                noteProbArray[1] = 0; //don't allow specialnote groups to be returned unless on a downbeat
                if (practiceMode != 2) noteProbArray[3] = 0; //don't allow triplet note groups to be returned unless on a downbeat
            }
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



    //special notes generation method 1
    private ArrayList<MidiNote> genSpecialNotes1(int startPulseTime) {
        // this function is accessed if a special note group is selected
        // all these note groups total to one quarter note count
        // must return something
        ArrayList<MidiNote> specialNotes = new ArrayList<MidiNote>();
        int numSpecialNoteGroups = 10;
        if (!accentsFlag && !flamsFlag && rollsFlag ) numSpecialNoteGroups = 5;
        else if (!accentsFlag && !flamsFlag && !rollsFlag) numSpecialNoteGroups = 4;

        int whichNoteGroup = 1 + (int) (Math.random() * numSpecialNoteGroups);

        if (!tripletFlag) {
            int[] tempArray = new int[]{2,3,5,7};
            if (!accentsFlag && !flamsFlag && rollsFlag ){
                tempArray[3] = 2;
            }
            else if (!accentsFlag && !flamsFlag && !rollsFlag) {
                tempArray[3] = 2;
                tempArray[2] = 3;
            }
            whichNoteGroup = getRandom(tempArray);
        }

        int accentsForNoteGroups = 4;
        int accentNotes = 1 + (int) (Math.random() * accentsForNoteGroups);

        int flamsForNoteGroups = 5;
        int flamNotes = 1 + (int) (Math.random() * flamsForNoteGroups);

        int rollForNoteGroups = 5;
        int rollNotes = 1 + (int) (Math.random() * rollForNoteGroups);

        int runningPulseTime = startPulseTime;
        if (timeDen == 4) {
            if (whichNoteGroup == 1) { //triplets
                for (int i = 0; i < 3; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(tripletNote);

                    if (accentsFlag) {
                        if (accentNotes == 1) {
                            int num = (int) (Math.random() * 2);
                            if (i == num) {
                                note.setAccentNum(2);
                            }
                        }
                        else if (accentNotes == 2) {
                            if (i == 0 || i == 2) {
                                note.setAccentNum(2);
                            }
                        }
                    }

                    if (flamsFlag) {
                        if (flamNotes == 1) {
                            if (i == 0) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 2) {
                            if (i == 1) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 3) {
                            if (i == 2) {
                                note.setFlamNum(1);
                            }
                        }
                    }

                    if (rollsFlag) {
                        if (rollNotes == 1) {
                            if (i == 1) {
                                note.setRollNum(1);
                            }
                        }
                        else if (rollNotes == 2) {
                            if (i == 0 || i == 1 || i == 2) {
                                note.setRollNum(1);
                            }
                        }
                        else if (rollNotes == 3) {
                            if (i == 2 || i == 1 ) {
                                note.setRollNum(1);
                            }
                        }
                    }

                    runningPulseTime += tripletNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 2) { //32
                for (int i = 0; i < 8; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(thirtysecondNote);

                    if (accentsFlag) {
                        if (accentNotes == 1 || accentNotes >= 4) {
                            int num = (int) (Math.random() * 7);
                            if (i == num) {
                                note.setAccentNum(2);
                            }
                        }
                        else if (accentNotes == 2) {
                            if (i == 0 || i == 4) {
                                note.setAccentNum(2);
                            }
                        }
                        else if (accentNotes == 3) {
                            if (i == 3 || i == 4) {
                                note.setAccentNum(2);
                            }
                        }
                    }

                    if (flamsFlag) {
                        if (flamNotes == 1 || flamNotes >=4) {
                            int num = (int) (Math.random() * 7);
                            if (i == num) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 2) {
                            if (i == 4) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 3) {
                            if (i == 0 || i == 4) {
                                note.setFlamNum(1);
                            }
                        }
                    }

                    runningPulseTime += thirtysecondNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 3) { //4 32nd notes
                int num = (int) (Math.random() * 3);
                for (int i = 0; i < 4; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(thirtysecondNote);
                    accentNotes = 1 + (int) (Math.random() * 3);
                    if (accentsFlag) {
                        if (accentNotes == 1) {
                            if (i == num) {
                                note.setAccentNum(2);
                            }
                        }
                        else if (accentNotes == 2) {
                            if (i == 0 || i == 3) {
                                note.setAccentNum(2);
                            }
                        }
                    }

                    flamNotes = 1 + (int) (Math.random() * 3);
                    if (flamsFlag) {
                        if (flamNotes == 1) {
                            if (i == num) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 2) {
                            if (i == 2) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 3) {
                            if (i == 2 || i == 0) {
                                note.setFlamNum(1);
                            }
                        }
                    }

                    runningPulseTime += thirtysecondNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 4 || whichNoteGroup == 8 || whichNoteGroup == 9) { // 16th triplets
                for (int i = 0; i < 6; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthTripletNote);
                    runningPulseTime += sixteenthTripletNote;

                    if (accentsFlag) {
                        int num = (int) (Math.random() * 5);
                        if (accentNotes == 1) {
                            if (i == num) {
                                note.setAccentNum(2);
                            }
                        } else if (accentNotes == 2) {
                            if (i == num || i == 3) {
                                note.setAccentNum(2);
                            }
                        } else if (accentNotes == 3) {
                            if (i == 0 || i == num || i == 3) {
                                note.setAccentNum(2);
                            }
                        }
                    }

                    if (flamsFlag) {
                        if (flamNotes == 1) {
                            int num = (int) (Math.random() * 5);
                            if (i == num) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 2) {
                            if (i == 1) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 3) {
                            if (i == 2) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 4) {
                            if (i == 0 || i == 3) {
                                note.setFlamNum(1);
                            }
                        }
                        else if (flamNotes == 5) {
                            if (i == 5) {
                                note.setFlamNum(1);
                            }
                        }
                    }

                    if (rollsFlag) {
                        if (rollNotes == 1) {
                            int num = 1 + (int) (Math.random() * 5);
                            if (i == num) {
                                note.setRollNum(1);
                            }
                        }
                        else if (rollNotes == 2) {
                            int num = 1 + (int) (Math.random() * 3);
                            num = num - 1;
                            if (i == num || i == 1 + num || i == 2 + num) {
                                note.setRollNum(1);
                            }
                        }
                        else if (rollNotes == 3) {
                            if (i == 1 || i == 2 ) {
                                note.setRollNum(1);
                            }
                        }
                        else if (rollNotes == 4) {
                            int num =  (int) (Math.random() * 5);
                            if (i == num) {
                                note.setRollNum(1);
                            }
                        }
                    }

                    specialNotes.add(note);

                }
            }
            else if (whichNoteGroup == 5) { //5 stroke roll
                int downBeat5StrokeRoll = (int) (Math.random() * 1);
                if (downBeat5StrokeRoll == 1) {
                    for (int i = 0; i < 2; i++) {
                        MidiNote note = new MidiNote(0, 0, 0, 0);
                        note.setChannel(0);
                        note.setNumber(60);
                        note.setStartTime(runningPulseTime);
                        note.setDuration(eighthNote);
                        if (i == 0) {
                            if (rollsFlag) note.setRollNum(2);
                        } else if (i == 1 && accentsFlag) {
                            note.setAccentNum(2);
                        }
                        runningPulseTime += eighthNote;
                        specialNotes.add(note);
                    }
                }
                else {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime + eighthNote);
                    note.setDuration(eighthNote);
                    if (rollsFlag) note.setRollNum(2);
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 6) { //flam accent
                for (int i = 0; i < 3; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(tripletNote);
                    if (i == 0) {
                        if (accentsFlag) note.setAccentNum(2);
                        if (flamsFlag) note.setFlamNum(1);
                    }
                    runningPulseTime += tripletNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 7) { //flam tap
                for (int i = 0; i < 4; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 2) {
                        if (accentsFlag) note.setAccentNum(2);
                        if (flamsFlag) note.setFlamNum(1);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 10) { // 3 16th notes and a random 16th rest

                int which16thNoteRest = (int) (Math.random() * 2);
                for (int i = 0; i < 3; i++) {
                    if (i==which16thNoteRest) {
                        runningPulseTime += sixteenthNote;
                    }
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 2) {
                        if (accentsFlag) note.setAccentNum(2);
                        if (flamsFlag) note.setFlamNum(1);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);
                }
            }
        }
        else if (timeDen == 8) {

        }
        return specialNotes;
    }


    //special notes generation method 2
    private ArrayList<MidiNote> genSpecialNotes2(int startPulseTime) {
        // this function is accessed if a special note group is selected
        // this function only called if accents and flams and rolls are set to true
        // all these note groups total to one half note count
        // must return something
        ArrayList<MidiNote> specialNotes = new ArrayList<MidiNote>();
        int numSpecialNoteGroups = 8;

        int whichNoteGroup = 1 + (int) (Math.random() * numSpecialNoteGroups);
        if (!tripletFlag) {
            int[] tempArray = new int[]{1,2,3,4,5,8};
            whichNoteGroup = getRandom(tempArray);
        }

        int runningPulseTime = startPulseTime;
        if (timeDen == 4) {
            if (whichNoteGroup == 1) { //flamacue
                for (int i = 0; i < 5; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    if (i != 4) {
                        note.setDuration(sixteenthNote);
                        runningPulseTime += sixteenthNote;
                    }
                    else {
                        note.setDuration(quarterNote);
                        runningPulseTime += quarterNote;
                    }

                    if (i==0) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    else if (i == 4) {
                        note.setFlamNum(1);
                    }
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 2) { // single paradiddle
                for (int i = 0; i < 8; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 4) {
                        note.setAccentNum(2);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);

                }
            }
            else if (whichNoteGroup == 3) { //flam paradiddle
                for (int i = 0; i < 8; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 4) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);

                }
            }
            else if (whichNoteGroup == 4) { //pata fla fla
                for (int i = 0; i < 8; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 3 || i == 4 || i == 7) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);

                }
            }
            else if (whichNoteGroup == 5) { //inverted flam tap
                for (int i = 0; i < 8; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(sixteenthNote);
                    if (i == 0 || i == 2 || i == 4 || i == 6) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    runningPulseTime += sixteenthNote;
                    specialNotes.add(note);

                }
            }
            else if (whichNoteGroup == 6) { // triplet notes 1
                for (int i = 0; i < 6; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(tripletNote);
                    if (i == 0 || i == 3) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    else if (i == 1 || i == 4) {
                        note.setRollNum(1);
                    }
                    runningPulseTime += tripletNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 7) { // triplet notes 2
                for (int i = 0; i < 6; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(tripletNote);
                    if (i == 0 || i == 3) {
                        note.setAccentNum(2);
                        note.setFlamNum(1);
                    }
                    else if (i == 1 || i == 4 || i == 0 || i == 3) {
                        note.setRollNum(1);
                    }
                    runningPulseTime += tripletNote;
                    specialNotes.add(note);
                }
            }
            else if (whichNoteGroup == 8) { //2 5 stroke rolls
                for (int i = 0; i < 4; i++) {
                    MidiNote note = new MidiNote(0, 0, 0, 0);
                    note.setChannel(0);
                    note.setNumber(60);
                    note.setStartTime(runningPulseTime);
                    note.setDuration(eighthNote);
                    if (i == 0 || i == 2) {
                        note.setRollNum(2);
                    }
                    else if ( (i == 1 || i == 3)&& accentsFlag) {
                        note.setAccentNum(2);
                    }
                    runningPulseTime += eighthNote;
                    specialNotes.add(note);
                }
            }
        }
        else if (timeDen == 8) {

        }
        return specialNotes;
    }

    //triplet group for low difficulty
    private ArrayList<MidiNote> genLowDifficultyTriplets(int startPulseTime) {

        ArrayList<MidiNote> specialNotes = new ArrayList<MidiNote>();
        int runningPulseTime = startPulseTime;

        int accentsForNoteGroups = 8;
        int accentNotes = 1 + (int) (Math.random() * accentsForNoteGroups);

        int flamsForNoteGroups = 10;
        int flamNotes = 1 + (int) (Math.random() * flamsForNoteGroups);

        int rollForNoteGroups = 12;
        int rollNotes = 1 + (int) (Math.random() * rollForNoteGroups);


        if (timeDen == 4) {
            for (int i = 0; i < 3; i++) {
                MidiNote note = new MidiNote(0, 0, 0, 0);
                note.setChannel(0);
                note.setNumber(60);
                note.setStartTime(runningPulseTime);
                note.setDuration(tripletNote);

                if (accentsFlag) {
                    int whichNote = (int) (Math.random() * 2);
                    if (whichNote > 2) whichNote = 2;
                    if (whichNote < 0 ) whichNote = 0;
                    if (accentNotes == 1) {
                        if (i == whichNote) {
                            note.setAccentNum(2);
                        }
                    } else if (accentNotes == 2) {
                        if (i == 0 || i == 2) {
                            note.setAccentNum(2);
                        }
                    }
                }

                if (rollsFlag) {
                    if (rollNotes == 1) {
                        if (i == 0) {
                            note.setRollNum(1);
                        }
                    } else if (rollNotes == 2) {
                        if (i == 0 || i == 1 || i == 2) {
                            note.setRollNum(1);
                        }
                    } else if (rollNotes == 3) {
                        if (i == 0 || i == 1) {
                            note.setRollNum(1);
                        }
                    } else if (rollNotes == 4) {
                        if (i == 1 || i == 2) {
                            note.setRollNum(1);
                        }
                    }
                }

                if (flamsFlag) {
                    if (rollNotes == 1) { //using rollnotes to not put flam and roll on the same note (makes it easier)
                        if (i == 1) {
                            note.setFlamNum(1);
                        }
                    }
                    else if (rollNotes == 3) {
                        if (i == 2) {
                            note.setFlamNum(1);
                        }
                    }
                }

                runningPulseTime += tripletNote;
                specialNotes.add(note);
            }

        }
        return specialNotes;
    }


    public static int getRandom(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

}
