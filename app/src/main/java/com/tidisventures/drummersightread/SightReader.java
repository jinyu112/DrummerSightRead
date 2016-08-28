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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.leff.midi.*;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class SightReader extends Activity {

    public static final String MidiDataID = "MidiDataID";
    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;

    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private long midiCRC;      /* CRC of the midi bytes */
    ArrayList<MusicSymbol> noteSequence = new ArrayList<MusicSymbol>(10);

    /** Create this SheetMusicActivity.  The Intent should have two parameters:
     * - MidiTitleID: The title of the song (String)
     * - MidiDataID: The raw byte[] data of the midi file.
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.sightreader);
        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);
        //MidiPlayer.LoadImages(this);

        // Parse the MidiFile from the raw bytes
        //byte[] data = this.getIntent().getByteArrayExtra(MidiDataID);
        //String title = this.getIntent().getStringExtra(MidiTitleID);
        //this.setTitle("MidiSheetMusic: " + title);
        //try {
        //    midifile = new MidiFile(data, title);
        //}
        //catch (MidiFileException e) {
        //    this.finish();
        //    return;
        //}

//         Initialize the settings (MidiOptions).
//         If previous settings have been saved, used those
        //options = new MidiOptions(midifile);
        //CRC32 crc = new CRC32(); //declare a checksum class
        //crc.update(data); //updates the checksum with an array of bytes
        //midiCRC = crc.getValue(); //long defining the checksum value
//        SharedPreferences settings = getPreferences(0);
//        options.scrollVert = settings.getBoolean("scrollVert", false);
//        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
//        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
//        String json = settings.getString("" + midiCRC, null);
//        MidiOptions savedOptions = MidiOptions.fromJson(json);
//        if (savedOptions != null) {
//            options.merge(savedOptions);
//        }
//        createView();
//        createSheetMusic(options);
    }

    /* Create the MidiPlayer and Piano views */
    void createView() {
//        layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        player = new MidiPlayer(this);
//        piano = new Piano(this);
//        layout.addView(player);
//        layout.addView(piano);
//        setContentView(layout);
//        player.SetPiano(piano);
//        layout.requestLayout();
    }

    /** Create the SheetMusic view with the given options */
    private void
    createSheetMusic(MidiOptions options) {
//        if (sheet != null) {
//            layout.removeView(sheet);
//        }
//        if (!options.showPiano) {
//            piano.setVisibility(View.GONE);
//        }
//        else {
//            piano.setVisibility(View.VISIBLE);
//        }
//        sheet = new SheetMusic(this);
//        sheet.init(midifile, options);
//        sheet.setPlayer(player);
//        layout.addView(sheet);
//        piano.SetMidiFile(midifile, options, player);
//        piano.SetShadeColors(options.shade1Color, options.shade2Color);
//        player.SetMidiFile(midifile, options, sheet);
//        layout.requestLayout();
//        sheet.callOnDraw();
    }


    /** Always display this activity in landscape mode. */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /** When the menu button is pressed, initialize the menus. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (player != null) {
//            player.Pause();
//        }
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.sheet_menu, menu);
        return true;
    }

    /** Callback when a menu item is selected.
     *  - Choose Song : Choose a new song
     *  - Song Settings : Adjust the sheet music and sound options
     *  - Save As Images: Save the sheet music as PNG images
     *  - Help : Display the HTML help screen
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.choose_song:
//                chooseSong();
//                return true;
//            case R.id.song_settings:
//                changeSettings();
//                return true;
//            case R.id.save_images:
//                showSaveImagesDialog();
//                return true;
//            case R.id.help:
//                showHelp();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
        return true;
    }

    /** To choose a new song, simply finish this activity.
     *  The previous activity is always the ChooseSongActivity.
     */
    private void chooseSong() {
        this.finish();
    }

    /** To change the sheet music options, start the SettingsActivity.
     *  Pass the current MidiOptions as a parameter to the Intent.
     *  Also pass the 'default' MidiOptions as a parameter to the Intent.
     *  When the SettingsActivity has finished, the onActivityResult()
     *  method will be called.
     */
    private void changeSettings() {
//        MidiOptions defaultOptions = new MidiOptions(midifile);
//        Intent intent = new Intent(this, SettingsActivity.class);
//        intent.putExtra(SettingsActivity.settingsID, options);
//        intent.putExtra(SettingsActivity.defaultSettingsID, defaultOptions);
//        startActivityForResult(intent, settingsRequestCode);
    }


    /* Show the "Save As Images" dialog */
    private void showSaveImagesDialog() {
//        LayoutInflater inflator = LayoutInflater.from(this);
//        final View dialogView= inflator.inflate(R.layout.save_images_dialog, null);
//        final EditText filenameView = (EditText)dialogView.findViewById(R.id.save_images_filename);
//        filenameView.setText(midifile.getFileName().replace("_", " ") );
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.save_images_str);
//        builder.setView(dialogView);
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface builder, int whichButton) {
//                saveAsImages(filenameView.getText().toString());
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface builder, int whichButton) {
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
    }


    /* Save the current sheet music as PNG images. */
    private void saveAsImages(String name) {
//        String filename = name;
//        try {
//            filename = URLEncoder.encode(name, "utf-8");
//        }
//        catch (UnsupportedEncodingException e) {
//        }
//        if (!options.scrollVert) {
//            options.scrollVert = true;
//            createSheetMusic(options);
//        }
//        try {
//            int numpages = sheet.GetTotalPages();
//            for (int page = 1; page <= numpages; page++) {
//                Bitmap image= Bitmap.createBitmap(SheetMusic.PageWidth + 40, SheetMusic.PageHeight + 40, Bitmap.Config.ARGB_8888);
//                Canvas imageCanvas = new Canvas(image);
//                sheet.DrawPage(imageCanvas, page);
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/MidiSheetMusic");
//                File file = new File(path, "" + filename + page + ".png");
//                path.mkdirs();
//                OutputStream stream = new FileOutputStream(file);
//                image.compress(Bitmap.CompressFormat.PNG, 0, stream);
//                image = null;
//                stream.close();
//
//                // Inform the media scanner about the file
//                MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null, null);
//            }
//        }
//        catch (IOException e) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("Error saving image to file " + Environment.DIRECTORY_PICTURES + "/MidiSheetMusic/" + filename  + ".png");
//            builder.setCancelable(false);
//            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                }
//            });
//            AlertDialog alert = builder.create();
//            alert.show();
//        }
    }


    /** Show the HTML help screen. */
    private void showHelp() {
//        Intent intent = new Intent(this, HelpActivity.class);
//        startActivity(intent);
    }

    /** This is the callback when the SettingsActivity is finished.
     *  Get the modified MidiOptions (passed as a parameter in the Intent).
     *  Save the MidiOptions.  The key is the CRC checksum of the midi data,
     *  and the value is a JSON dump of the MidiOptions.
     *  Finally, re-create the SheetMusic View with the new options.
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
//        if (requestCode != settingsRequestCode) {
//            return;
//        }
//        options = (MidiOptions)
//                intent.getSerializableExtra(SettingsActivity.settingsID);
//
//        // Check whether the default instruments have changed.
//        for (int i = 0; i < options.instruments.length; i++) {
//            if (options.instruments[i] !=
//                    midifile.getTracks().get(i).getInstrument()) {
//                options.useDefaultInstruments = false;
//            }
//        }
//        // Save the options.
//        SharedPreferences settings = getPreferences(0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean("scrollVert", options.scrollVert);
//        editor.putInt("shade1Color", options.shade1Color);
//        editor.putInt("shade2Color", options.shade2Color);
//        String json = options.toJson();
//        if (json != null) {
//            editor.putString("" + midiCRC, json);
//        }
//        editor.commit();
//
//        // Recreate the sheet music with the new options
//        createSheetMusic(options);
    }

    /** When this activity resumes, redraw all the views */
    @Override
    protected void onResume() {
        super.onResume();
//        layout.requestLayout();
//        player.invalidate();
//        piano.invalidate();
//        if (sheet != null) {
//            sheet.invalidate();
//        }
//        layout.requestLayout();
    }

    /** When this activity pauses, stop the music */
    @Override
    protected void onPause() {
//        if (player != null) {
//            player.Pause();
//        }
        super.onPause();
    }


    private File genTrack() {
        // 1. Create some MidiTracks
        com.leff.midi.MidiTrack tempoTrack = new com.leff.midi.MidiTrack();
        com.leff.midi.MidiTrack noteTrack = new com.leff.midi.MidiTrack();

        // 2. Add events to the tracks
        // Track 0 is the tempo map
        com.leff.midi.event.meta.TimeSignature ts = new com.leff.midi.event.meta.TimeSignature();
        ts.setTimeSignature(4, 4, com.leff.midi.event.meta.TimeSignature.DEFAULT_METER,
                com.leff.midi.event.meta.TimeSignature.DEFAULT_DIVISION);

        Tempo tempo = new Tempo();
        tempo.setBpm(228);

        tempoTrack.insertEvent(ts);
        tempoTrack.insertEvent(tempo);

        // Track 1 will have some notes in it
        final int NOTE_COUNT = 80;

        for(int i = 0; i < NOTE_COUNT; i++)
        {
            int channel = 0;
            int pitch = 1 + i;
            int velocity = 100;
            long tick = i * 480;
            long duration = 120;

            noteTrack.insertNote(channel, pitch, velocity, tick, duration);
        }

        // 3. Create a MidiFile with the tracks we created

        ArrayList<com.leff.midi.MidiTrack> tracks = new ArrayList<com.leff.midi.MidiTrack>();
        tracks.add(tempoTrack);
        tracks.add(noteTrack);

        com.leff.midi.MidiFile midi = new com.leff.midi.MidiFile(com.leff.midi.MidiFile.DEFAULT_RESOLUTION,tracks);

        // 4. Write the MIDI data to a file
        File output = new File("exampleout.mid");
//        try
//        {
//            midi.writeToFile();
//        }
//        catch(IOException e)
//        {
//            System.err.println(e);
//        }

        return output;
    }

    private ArrayList<MusicSymbol>
    CreateSymbols(ArrayList<ChordSymbol> chords, ClefMeasures clefs,
                  TimeSignature time, int lastStart) {

        ArrayList<MusicSymbol> symbols = new ArrayList<MusicSymbol>();
        symbols = AddBars(chords, time, lastStart);
        symbols = AddRests(symbols, time);

        return symbols;
    }



    /** Add in the vertical bars delimiting measures.
     *  Also, add the time signature symbols.
     */
    private ArrayList<MusicSymbol>
    AddBars(ArrayList<ChordSymbol> chords, TimeSignature time, int lastStart) {
        ArrayList<MusicSymbol> symbols = new ArrayList<MusicSymbol>();

        TimeSigSymbol timesig = new TimeSigSymbol(time.getNumerator(), time.getDenominator());
        symbols.add(timesig);

        /* The starttime of the beginning of the measure */
        int measuretime = 0;

        int i = 0;
        while (i < chords.size()) {
            if (measuretime <= chords.get(i).getStartTime()) {
                symbols.add(new BarSymbol(measuretime) );
                measuretime += time.getMeasure();
            }
            else {
                symbols.add(chords.get(i));
                i++;
            }
        }

        /* Keep adding bars until the last StartTime (the end of the song) */
        while (measuretime < lastStart) {
            symbols.add(new BarSymbol(measuretime) );
            measuretime += time.getMeasure();
        }

        /* Add the final vertical bar to the last measure */
        symbols.add(new BarSymbol(measuretime));
        return symbols;
    }

    /** Add rest symbols between notes.  All times below are
     * measured in pulses.
     */
    private
    ArrayList<MusicSymbol> AddRests(ArrayList<MusicSymbol> symbols, TimeSignature time) {
        int prevtime = 0;

        ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>( symbols.size() );

        for (MusicSymbol symbol : symbols) {
            int starttime = symbol.getStartTime();
            RestSymbol[] rests = GetRests(time, prevtime, starttime);
            if (rests != null) {
                for (RestSymbol r : rests) {
                    result.add(r);
                }
            }

            result.add(symbol);

            /* Set prevtime to the end time of the last note/symbol. */
            if (symbol instanceof ChordSymbol) {
                ChordSymbol chord = (ChordSymbol)symbol;
                prevtime = Math.max( chord.getEndTime(), prevtime );
            }
            else {
                prevtime = Math.max(starttime, prevtime);
            }
        }
        return result;
    }

    /** Return the rest symbols needed to fill the time interval between
     * start and end.  If no rests are needed, return nil.
     */
    private
    RestSymbol[] GetRests(TimeSignature time, int start, int end) {
        RestSymbol[] result;
        RestSymbol r1, r2;

        if (end - start < 0)
            return null;

        NoteDuration dur = time.GetNoteDuration(end - start);
        switch (dur) {
            case Whole:
            case Half:
            case Quarter:
            case Eighth:
                r1 = new RestSymbol(start, dur);
                result = new RestSymbol[]{ r1 };
                return result;

            case DottedHalf:
                r1 = new RestSymbol(start, NoteDuration.Half);
                r2 = new RestSymbol(start + time.getQuarter()*2,
                        NoteDuration.Quarter);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            case DottedQuarter:
                r1 = new RestSymbol(start, NoteDuration.Quarter);
                r2 = new RestSymbol(start + time.getQuarter(),
                        NoteDuration.Eighth);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            case DottedEighth:
                r1 = new RestSymbol(start, NoteDuration.Eighth);
                r2 = new RestSymbol(start + time.getQuarter()/2,
                        NoteDuration.Sixteenth);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            default:
                return null;
        }
    }


    /** Create the chord symbols for a single track.
     * @param midinotes  The Midinotes in the track.
     * @param key        The Key Signature, for determining sharps/flats.
     * @param time       The Time Signature, for determining the measures.
     * @param clefs      The clefs to use for each measure.
     * @ret An array of ChordSymbols
     */
    private
    ArrayList<ChordSymbol> CreateChords(ArrayList<MidiNote> midinotes,
                                        KeySignature key,
                                        TimeSignature time,
                                        ClefMeasures clefs) {

        int i = 0;
        ArrayList<ChordSymbol> chords = new ArrayList<ChordSymbol>();
        ArrayList<MidiNote> notegroup = new ArrayList<MidiNote>(12);
        int len = midinotes.size();

        while (i < len) {

            int starttime = midinotes.get(i).getStartTime();
            Clef clef = clefs.GetClef(starttime);

            /* Group all the midi notes with the same start time
             * into the notes list.
             */
            notegroup.clear();
            notegroup.add(midinotes.get(i));
            i++;
            while (i < len && midinotes.get(i).getStartTime() == starttime) {
                notegroup.add(midinotes.get(i));
                i++;
            }

            /* Create a single chord from the group of midi notes with
             * the same start time.
             */
            ChordSymbol chord = new ChordSymbol(notegroup, key, time, clef, sheet);
            chords.add(chord);
        }

        return chords;
    }


}

