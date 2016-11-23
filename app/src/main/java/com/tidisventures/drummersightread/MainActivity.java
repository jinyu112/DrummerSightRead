package com.tidisventures.drummersightread;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;


public class MainActivity extends ActionBarActivity {

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
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.butt);
        AppRater.app_launched(this);

        ImageView imgView = (ImageView) findViewById(R.id.imgview);
        imgView.setImageResource(R.drawable.icon);


    }

    public void goToSightReader(View view) {
        Intent intent = new Intent(this,SightReader.class);
        startActivity(intent);
    }

    public void goToSettings(View view) {
        Intent intent = new Intent(this,Settings.class);
        startActivity(intent);
    }

    public void goToAbout(View view) {
        Intent intent = new Intent(this,About.class);
        startActivity(intent);
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
            Intent intent = new Intent(this,Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
