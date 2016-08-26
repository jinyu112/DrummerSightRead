package com.tidisventures.drummersightread;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.zip.CRC32;


public class MainActivity extends ActionBarActivity {

    public static final String MidiDataID = "MidiDataID";
    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;

    private MidiPlayer player;   /* The play/stop/rewind toolbar */
    private Piano piano;         /* The piano at the top */
    private SheetMusic sheet;    /* The sheet music */
    private LinearLayout layout; /* THe layout */
    private MidiFile midifile;   /* The midi file to play */
    private MidiOptions options; /* The options for sheet music and sound */
    private long midiCRC;      /* CRC of the midi bytes */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


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
        options = new MidiOptions();
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

        sheet.init2(options);


        //sheet.setPlayer(player);
        layout.addView(sheet);
        //piano.SetMidiFile(midifile, options, player);
        //piano.SetShadeColors(options.shade1Color, options.shade2Color);
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
}
