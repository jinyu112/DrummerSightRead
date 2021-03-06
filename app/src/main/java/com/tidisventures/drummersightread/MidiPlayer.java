package com.tidisventures.drummersightread;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/** @class MidiPlayer
 *
 * The MidiPlayer is the panel at the top used to play the sound
 * of the midi file.  It consists of:
 *
 * - The Rewind button
 * - The Play/Pause button
 * - The Stop button
 * - The Fast Forward button
 * - The Playback speed bar
 *
 * The sound of the midi file depends on
 * - The MidiOptions (taken from the menus)
 *   Which tracks are selected
 *   How much to transpose the keys by
 *   What instruments to use per track
 * - The tempo (from the Speed bar)
 * - The volume
 *
 * The MidiFile.ChangeSound() method is used to create a new midi file
 * with these options.  The mciSendString() function is used for
 * playing, pausing, and stopping the sound.
 *
 * For shading the notes during playback, the method
 * SheetMusic.ShadeNotes() is used.  It takes the current 'pulse time',
 * and determines which notes to shade.
 */
//
public class MidiPlayer extends LinearLayout {
    static Bitmap rewindImage;   /** The rewind image */
    static Bitmap playImage;     /** The play image */
    static Bitmap pauseImage;    /** The pause image */
    static Bitmap stopImage;      /** The stop image */
    static Bitmap fastFwdImage;  /** The fast forward image */

    private ImageButton rewindButton; /** The rewind button */
    private ImageButton playButton;   /** The play/pause button */
    private ImageButton stopButton;   /** The stop button */
    private ImageButton fastFwdButton;/** The fast forward button */

    private TextView tempoTV;
    private TextView tempoTVLabel;
    private ImageButton plusButton;
    private ImageButton minusButton;

    int playstate;               /** The playing state of the Midi Player */
    final int stopped   = 1;     /** Currently stopped */
    final int playing   = 2;     /** Currently playing music */
    final int paused    = 3;     /** Currently paused */
    final int initStop  = 4;     /** Transitioning from playing to stop */
    final int initPause = 5;     /** Transitioning from playing to pause */

    final String tempSoundFile = "exampleout.mid"; /** The filename to play sound from */

    MediaPlayer player;         /** For playing the audio */
    MidiFile midifile;          /** The midi file to play */
    MidiOptions options;        /** The sound options for playing the midi file */
    double pulsesPerMsec;       /** The number of pulses per millisec */
    SheetMusic sheet;           /** The sheet music to shade while playing */
    Handler timer;              /** Timer used to update the sheet music while playing */
    long startTime;             /** Absolute time when music started playing (msec) */
    double startPulseTime;      /** Time (in pulses) when music started playing */
    double currentPulseTime;    /** Time (in pulses) music is currently at */
    double prevPulseTime;       /** Time (in pulses) music was last at */
    Context context;            /** The context, for writing midi files */
    private int scrWidth;

    //metronome
    private short bpm = 60;
    private short noteValue = 4;
    private short beats = 4;
    private MetronomeAsyncTask metroTask;
    private double beatSound = 2440;
    private double sound = 6440;
    private boolean metronomeOn = true;
    private Beats beat;

    private int timeDen = 4;
    private int timeNum = 4;

    //play sound?
    private boolean playSoundFlag = false;


    /** Load the play/pause/stop button images */
    public static void LoadImages(Context context) {
        if (rewindImage != null) {
            return;
        }
        Resources res = context.getResources();
        rewindImage = BitmapFactory.decodeResource(res, R.drawable.ic_fast_rewind_black_24dp);
        playImage = BitmapFactory.decodeResource(res, R.drawable.ic_play_arrow_black_24dp);
        pauseImage = BitmapFactory.decodeResource(res, R.drawable.pause);
        stopImage = BitmapFactory.decodeResource(res, R.drawable.ic_stop_black_24dp);
        fastFwdImage = BitmapFactory.decodeResource(res, R.drawable.ic_fast_forward_black_24dp);
    }


    /** Create a new MidiPlayer, displaying the play/stop buttons, and the
     *  speed bar.  The midifile and sheetmusic are initially null.
     */
    public MidiPlayer(Context context) {
        super(context);
        LoadImages(context);
        this.context = context;
        this.midifile = null;
        this.options = null;
        this.sheet = null;
        playstate = stopped;
        startTime = SystemClock.uptimeMillis();
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = -10;
        this.setPadding(0, 0, 0, 0);
        CreateButtons();

        Activity activity = (Activity)context;
        int screenwidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        scrWidth = screenwidth;
        int screenheight = activity.getWindowManager().getDefaultDisplay().getHeight();
        Point newsize = MidiPlayer.getPreferredSize(screenwidth, screenheight);
        resizeButtons(newsize.x, newsize.y);
        player = new MediaPlayer();
//


        setBackgroundColor(getResources().getColor(R.color.colorAccent));

        metroTask = new MetronomeAsyncTask();
        beat = Beats.four;
        metroTask.setBpm(bpm);
        metroTask.setBeat(beat.getNum());

    }



    /** Get the preferred width/height given the screen width/height */
    public static Point getPreferredSize(int screenwidth, int screenheight) {
        int height = (int) (5.0 * screenwidth / ( 2 + Piano.KeysPerOctave * Piano.MaxOctave));
        height = height * 2/3 ;
        Point result = new Point(screenwidth, height);
        return result;
    }

    /** Determine the measured width and height.
     *  Resize the individual buttons according to the new width/height.
     */
    @Override
    protected void onMeasure(int widthspec, int heightspec) {
        super.onMeasure(widthspec, heightspec);
        int screenwidth = MeasureSpec.getSize(widthspec);
        int screenheight = MeasureSpec.getSize(heightspec);

        /* Make the button height 2/3 the piano WhiteKeyHeight */
        int width = screenwidth;
        int height = (int) (5.0 * screenwidth / ( 2 + Piano.KeysPerOctave * Piano.MaxOctave));
        height = height * 2/3;
        setMeasuredDimension(width, height);
    }

    /** When this view is resized, adjust the button sizes */
    @Override
    protected void
    onSizeChanged(int newwidth, int newheight, int oldwidth, int oldheight) {
        resizeButtons(newwidth, newheight);
        super.onSizeChanged(newwidth, newheight, oldwidth, oldheight);
    }


    /** Create the rewind, play, stop, and fast forward buttons */
    void CreateButtons() {
        this.setOrientation(LinearLayout.HORIZONTAL);

        /* Create the rewind button */
        rewindButton = new ImageButton(context);
        rewindButton.setImageBitmap(rewindImage);
        rewindButton.setScaleType(ImageView.ScaleType.FIT_XY);
        rewindButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Rewind();
            }
        });
        this.addView(rewindButton);

        /* Create the stop button */
        stopButton = new ImageButton(context);
        stopButton.setImageBitmap(stopImage);
        stopButton.setScaleType(ImageView.ScaleType.FIT_XY);
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Stop();
            }
        });
        this.addView(stopButton);


        /* Create the play button */
        playButton = new ImageButton(context);
        playButton.setImageBitmap(playImage);
        playButton.setScaleType(ImageView.ScaleType.FIT_XY);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Play();
            }
        });
        this.addView(playButton);

        /* Create the fastFwd button */
        fastFwdButton = new ImageButton(context);
        fastFwdButton.setImageBitmap(fastFwdImage);
        fastFwdButton.setScaleType(ImageView.ScaleType.FIT_XY);
        fastFwdButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FastForward();
            }
        });
        this.addView(fastFwdButton);


        //Tempo control
        tempoTVLabel = new TextView(context);
        tempoTVLabel.setText("TEMPO (BPM):");
        tempoTVLabel.setTextColor(Color.WHITE);
        tempoTVLabel.setGravity(Gravity.CENTER);
        tempoTVLabel.setSingleLine(true);
        this.addView(tempoTVLabel);

        tempoTV = new TextView(context);
        tempoTV.setText(Integer.toString(bpm));
        tempoTV.setTextColor(Color.WHITE);
        tempoTV.setGravity(Gravity.CENTER);
        tempoTV.setSingleLine(true);
        this.addView(tempoTV);


        plusButton = new ImageButton(context);
        plusButton.setImageResource(R.drawable.ic_add_black_24dp);

        plusButton.setScaleType(ImageView.ScaleType.FIT_XY);
        PlusOnClickListener ptsScoredPlusListener = new PlusOnClickListener(tempoTV);
        plusButton.setOnClickListener(ptsScoredPlusListener);
        this.addView(plusButton);

        minusButton = new ImageButton(context);
        minusButton.setImageResource(R.drawable.ic_remove_black_24dp);
        minusButton.setScaleType(ImageView.ScaleType.FIT_XY);
        MinusOnClickListener ptsScoredMinusListener = new MinusOnClickListener(tempoTV);
        minusButton.setOnClickListener(ptsScoredMinusListener);
        this.addView(minusButton);



        /* Initialize the timer used for playback, but don't startroundedDelayForCountOff
         * the timer yet (enabled = false).
         */
        timer = new Handler();
    }

    void resizeButtons(int newwidth, int newheight) {
        int buttonheight = newheight;
        int pad = buttonheight/6;
        rewindButton.setPadding(pad, pad, pad, pad);
        stopButton.setPadding(pad, pad, pad, pad);
        playButton.setPadding(pad, pad, pad, pad);
        fastFwdButton.setPadding(pad, pad, pad, pad);
        plusButton.setPadding(pad,pad,pad,pad);
        minusButton.setPadding(pad,pad,pad,pad);

        LinearLayout.LayoutParams params;

        params = new LinearLayout.LayoutParams(buttonheight, buttonheight);
        params.width = buttonheight;
        params.height = buttonheight;
        params.bottomMargin = 0;
        params.topMargin = 0;
        params.rightMargin = 0;
        params.leftMargin = buttonheight/6;

        rewindButton.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(buttonheight, buttonheight);
        params.bottomMargin = 0;
        params.topMargin = 0;
        params.rightMargin = 0;
        params.leftMargin = 0;

        playButton.setLayoutParams(params);
        stopButton.setLayoutParams(params);
        fastFwdButton.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(buttonheight*2, buttonheight);
        params.bottomMargin = 0;
        params.topMargin = 0;
        params.rightMargin = 0;
        params.leftMargin =  scrWidth - buttonheight * 19 / 2;
        tempoTVLabel.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(buttonheight, buttonheight);
        params.bottomMargin = 0;
        params.topMargin = 0;
        params.rightMargin = 0;
        params.leftMargin =  0;
        tempoTV.setLayoutParams(params);
        plusButton.setLayoutParams(params);
        minusButton.setLayoutParams(params);



    }

    /** The MidiFile and/or SheetMusic has changed. Stop any playback sound,
     *  and store the current midifile and sheet music.
     */
    public void SetMidiFile(MidiFile file, MidiOptions opt, SheetMusic s) {
        //Displaying the tempo in beats per minute dynamically
        tempoTV.setText(Integer.toString(bpm));

        /* If we're paused, and using the same midi file, redraw the
         * highlighted notes.
         */
        if ((file == midifile && midifile != null && playstate == paused)) {
            options = opt;
            sheet = s;
            sheet.ShadeNotes((int)currentPulseTime, (int)-1, false);

            /* We have to wait some time (200 msec) for the sheet music
             * to scroll and redraw, before we can re-shade.
             */
            timer.removeCallbacks(TimerCallback);
            timer.postDelayed(ReShade, 500);
        }
        else {
            Stop();
            midifile = file;
            options = opt;
            sheet = s;
        }


        if ((playstate == paused)) {
            options = opt;
            sheet = s;
            sheet.ShadeNotes((int)currentPulseTime, (int)-1, false);

            /* We have to wait some time (200 msec) for the sheet music
             * to scroll and redraw, before we can re-shade.
             */
            timer.removeCallbacks(TimerCallback);
            timer.postDelayed(ReShade, 500);
        }
        else {
            Stop();
            midifile = file;
            options = opt;
            sheet = s;
        }
    }

    /** If we're paused, reshade the sheet music and piano. */
    Runnable ReShade = new Runnable() {
        public void run() {
            if (playstate == paused || playstate == stopped) {
                sheet.ShadeNotes((int)currentPulseTime, (int)-10, false);
            }
        }
    };


    /** Return the number of tracks selected in the MidiOptions.
     *  If the number of tracks is 0, there is no sound to play.
     */
    private int numberTracks() {
        int count = 0;
        for (int i = 0; i < options.tracks.length; i++) {
            if (options.tracks[i] && !options.mute[i]) {
                count += 1;
            }
        }
        return count;
    }

    /** Create a new midi file with all the MidiOptions incorporated.
     *  Save the new file to playing.mid, and store
     *  this temporary filename in tempSoundFile.
     */
    private void CreateMidiFile() {

        double inverse_tempo = 1.0 / midifile.getTime().getTempo();
        try {
            inverse_tempo = returnInverseTempo();
        } catch (NumberFormatException ex) {
            System.err.println("Caught error in midiplayer: "
                    + ex.getMessage());
        }
        double inverse_tempo_scaled = inverse_tempo;

        options.tempo = (int)(1.0 / inverse_tempo_scaled);
        pulsesPerMsec = midifile.getTime().getQuarter() * (1000.0 / options.tempo);

        try {
            FileOutputStream dest = new FileOutputStream(new File(context.getExternalFilesDir(null), tempSoundFile));
            midifile.ChangeSound(dest, options);
            dest.close();
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(context, "Error: Unable to create MIDI file for playing.", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void checkFile(String name) {
        try {
            FileInputStream in = context.openFileInput(name);
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
            in = context.openFileInput(name);
            int offset = 0;
            while (offset < total) {
                len = in.read(data, offset, total - offset);
                if (len > 0)
                    offset += len;
            }
            in.close();
            MidiFile testmidi = new MidiFile(data, name);
        }
        catch (IOException e) {
            Toast toast = Toast.makeText(context, "CheckFile: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
        catch (MidiFileException e) {
            Toast toast = Toast.makeText(context, "CheckFile midi: " + e.toString(), Toast.LENGTH_LONG);
            toast.show();
        }
    }


    /** Play the sound for the given MIDI file */
    private void PlaySound(String filename) {
        if (player == null)
            return;
        try {
            FileInputStream input = new FileInputStream(new File(context.getExternalFilesDir(null), filename));
            player.reset();
            player.setDataSource(input.getFD());
            input.close();
            player.prepare();

            // My AsyncTask is currently not doing work in doInBackground()
            if(metroTask.getStatus() != AsyncTask.Status.RUNNING && metronomeOn) { //this if statement was put in because on rapid pressing of the stop/start buttons while playing, the async task was executed while it was already running
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    metroTask.setBeat(beat.getNum());
                    beats = beat.getNum();
                    metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                }
                else {
                    metroTask.setBeat(beat.getNum());
                    beats = beat.getNum();
                    metroTask.execute();
                }
            }
            if (playSoundFlag) {
                player.start();
            }

        }
        catch (IOException e) {
            Toast toast = Toast.makeText(context, "Error: Unable to play MIDI sound", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    /** Stop playing the MIDI music */
    private void StopSound() {
        if (player == null)
            return;
        player.stop();
        player.reset();
    }

    /** Stop playing the MIDI music when the stop button is pressed */
    public void setStopSound() {
        if (player == null)
            return;
        player.stop();
        player.reset();
        metroTask.stop();
        Runtime.getRuntime().gc();
    }


    /** The callback for the play button.
     *  If we're stopped or pause, then play the midi file.
     */
    private void Play() {
        if (midifile == null || sheet == null || numberTracks() == 0) {
            return;
        }
        else if (playstate == initStop || playstate == initPause || playstate == playing) {
            return;
        }
        if (sheet == null || numberTracks() == 0) {
            return;
        }
        else if (playstate == initStop || playstate == initPause || playstate == playing) {
            return;
        }
        // playstate is stopped or paused



        // Hide the midi player, wait a little for the view
        // to refresh, and then start playing
        timer.removeCallbacks(TimerCallback);

        int roundedDelayForCountOff = 100; // millisecs
        timer.postDelayed(DoPlay, roundedDelayForCountOff);
    }

    Runnable DoPlay = new Runnable() {
        public void run() {
            Activity activity = (Activity)context;
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /* The startPulseTime is the pulse time of the midi file when
         * we first start playing the music.  It's used during shading.
         */
            if (playstate == paused) {
                 
                startPulseTime = currentPulseTime;
                options.pauseTime = (int)(currentPulseTime - options.shifttime);
            }
            else {
                options.pauseTime = 0;
                startPulseTime = options.shifttime;
                currentPulseTime = options.shifttime;
                prevPulseTime = options.shifttime - midifile.getTime().getQuarter();

            }

            CreateMidiFile();
            playstate = playing;
            //if (playSoundFlag) {
                PlaySound(tempSoundFile);
            //}
            startTime = SystemClock.uptimeMillis();

            timer.removeCallbacks(TimerCallback);
            timer.removeCallbacks(ReShade);
            timer.postDelayed(TimerCallback, 100);
            sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, true);

            return;
        }
    };


    /** The callback for pausing playback.
     *  If we're currently playing, pause the music.
     *  The actual pause is done when the timer is invoked.
     */
    public void Pause() {
        this.setVisibility(View.VISIBLE);
        LinearLayout layout = (LinearLayout)this.getParent();
        layout.requestLayout();
        this.requestLayout();
        this.invalidate();

        Activity activity = (Activity)context;
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (sheet == null || numberTracks() == 0) {
            return;
        }
        else if (playstate == playing) {
             
            playstate = initPause;
            return;
        }


    }


    /** The callback for the Stop button.
     *  If playing, initiate a stop and wait for the timer to finish.
     *  Then do the actual stop.
     */
    void Stop() {
        this.setVisibility(View.VISIBLE);
        if ( sheet == null || playstate == stopped) {
             
            return;
        }

        if (playstate == initPause || playstate == initStop || playstate == playing) {
            /* Wait for timer to finish */
            playstate = initStop;
             
            DoStop();
        }
        else if (playstate == paused) {
             
            DoStop();
        }
    }

    /** Perform the actual stop, by stopping the sound,
     *  removing any shading, and clearing the state.
     */
    void DoStop() {
        playstate = stopped;
        timer.removeCallbacks(TimerCallback);
        sheet.ShadeNotes(-10, (int)prevPulseTime, false);
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);
        startPulseTime = 0;
        currentPulseTime = 0;
        prevPulseTime = 0;
        setVisibility(View.VISIBLE);
        stopMetronome();
        StopSound();
    }

    /** Rewind the midi music back one measure.
     *  The music must be in the paused state.
     *  When we resume in playPause, we start at the currentPulseTime.
     *  So to rewind, just decrease the currentPulseTime,
     *  and re-shade the sheet music.
     */
    void Rewind() {
        if ( sheet == null || playstate != paused) {
            return;
        }

        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int)currentPulseTime, false);

        prevPulseTime = currentPulseTime;
        currentPulseTime -= midifile.getTime().getMeasure();
        if (currentPulseTime < options.shifttime) {
            currentPulseTime = options.shifttime;
        }
        sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, false);
    }

    /** Fast forward the midi music by one measure.
     *  The music must be in the paused/stopped state.
     *  When we resume in playPause, we start at the currentPulseTime.
     *  So to fast forward, just increase the currentPulseTime,
     *  and re-shade the sheet music.
     */
    void FastForward() {
        if (sheet == null) {
            return;
        }
        if (playstate != paused && playstate != stopped) {
            return;
        }
        playstate = paused;

        /* Remove any highlighted notes */
        sheet.ShadeNotes(-10, (int) currentPulseTime, false);

        prevPulseTime = currentPulseTime;
        currentPulseTime += midifile.getTime().getMeasure();
        if (currentPulseTime > midifile.getTotalPulses()) {
            currentPulseTime -= midifile.getTime().getMeasure();

        }
        sheet.ShadeNotes((int) currentPulseTime, (int) prevPulseTime, false);
    }


    /** The callback for the timer. If the midi is still playing,
     *  update the currentPulseTime and shade the sheet music.
     *  If a stop or pause has been initiated (by someone clicking
     *  the stop or pause button), then stop the timer.
     */
    Runnable TimerCallback = new Runnable() {
        public void run() {

            if (sheet == null) {
                 
                playstate = stopped;
                return;
            }
            else if (playstate == stopped || playstate == paused) {
                 
            /* This case should never happen */
                return;
            }
            else if (playstate == initStop) {
                 
                return;
            }
            else if (playstate == playing) {

                long msec = SystemClock.uptimeMillis() - startTime;
                prevPulseTime = currentPulseTime;
                currentPulseTime = startPulseTime + msec * pulsesPerMsec;


            /* If we're playing in a loop, stop and restart */
                if (options.playMeasuresInLoop) {
                    double nearEndTime = currentPulseTime + pulsesPerMsec*10;
                    int measure = (int)(nearEndTime /midifile.getTime().getMeasure()); //hardcoded jin 8/25/16
                    if (measure > options.playMeasuresInLoopEnd) {
                        RestartPlayMeasuresInLoop();
                        return;
                    }
                }

            /* Stop if we've reached the end of the song */
                if (currentPulseTime > midifile.getTotalPulses()) { //random hardcoded jin 8/25/16
                    DoStop();
                    return;
                }
                sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, true);
                timer.postDelayed(TimerCallback, 100);




                return;
            }
            else if (playstate == initPause) {
                long msec = SystemClock.uptimeMillis() - startTime;
                StopSound();
                 
                prevPulseTime = currentPulseTime;
                currentPulseTime = startPulseTime + msec * pulsesPerMsec;
                sheet.ShadeNotes((int)currentPulseTime, (int)prevPulseTime, false);
                playstate = paused;
                timer.postDelayed(ReShade, 1000);
                return;
            }
        }
    };


    /** The "Play Measures in a Loop" feature is enabled, and we've reached
     *  the last measure. Stop the sound, unshade the music, and then
     *  start playing again.
     */
    private void RestartPlayMeasuresInLoop() {
        playstate = stopped;
        sheet.ShadeNotes(-10, (int)prevPulseTime, false);
        currentPulseTime = 0;
        prevPulseTime = -1;
        StopSound();
        timer.postDelayed(DoPlay, 300);
    }


    private class MetronomeAsyncTask extends AsyncTask<Void,Void,String> {
        Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
        }

        protected String doInBackground(Void... params) {
            if (metronome!=null) {
                metronome.setBeat(beats);
                metronome.setNoteValue(noteValue);

                short tempTempo = (short) getTempoFromTV();

//            //limit tempo going into metronome
                if (tempTempo > 200) tempTempo = 200;
                if (tempTempo < 30) tempTempo = 30;

                metronome.setBpm(tempTempo);
                metronome.setBeatSound(beatSound);
                metronome.setSound(sound);
                metronome.play();
            }
            return null;
        }

        public void stop() {
            if (metronome!=null) {
                metronome.stop();
                metronome = null;
            }
        }

        public void setBpm(short bpm_in) {
            if (metronome!=null) {
                metronome.setBpm(bpm_in);
                metronome.calcSilence();
            }
        }

        public void setBeat(short beat) {
            if(metronome != null)
                metronome.setBeat(beat);
        }
    }

    public void stopMetronome() {
        metroTask.stop();
        metroTask = new MetronomeAsyncTask();
        Runtime.getRuntime().gc();
    }


    public void setMetronomeOn(boolean in) {
        this.metronomeOn = in;
    }

    public void setTempo(short in) {
        this.bpm = in;
    }

    public void setBeats(Beats in) {
        this.beat = in;
    }

public void setPlaySoundFlag(boolean in) {
    this.playSoundFlag = in;
}

    private int convertBPMtoMicrosec(double tempo_in) {
        int microseconds = 1000000;
        if (tempo_in != 0) {
            microseconds = (int) Math.round(60000000 / tempo_in);
        }
        return microseconds;
    }


    private int getTempoFromTV() {
        String tempStr="0";
        int tempo_out = 60;
        try {
            tempStr = tempoTV.getText().toString();
            if (tempStr.equals("") || tempStr.equals(".") || tempStr.equals(",")) {
                tempStr = "60";
            }
            double tempStrDbl = Double.parseDouble(tempStr);
            tempo_out = (int) Math.round(tempStrDbl);
        } catch (NumberFormatException ex) {
            System.err.println("Caught NumberFormatException in midiplayer: "
                    + ex.getMessage());
        }
        return tempo_out;
    }

private double returnInverseTempo() {
    String tempStr="0";
    double inverse_tempo_temp = 1.0 / 1000000;
    try {
        tempStr = tempoTV.getText().toString();
        if (tempStr.equals("") || tempStr.equals(".") || tempStr.equals(",")) {
            tempStr = "60";
        }
        double tempStrDbl = Double.parseDouble(tempStr);
        if (timeDen == 8) {
            tempStrDbl = tempStrDbl* 3 / 2;
        }
        inverse_tempo_temp = 1.0 / convertBPMtoMicrosec(tempStrDbl);
    } catch (NumberFormatException ex) {
        System.err.println("Caught NumberFormatException in midiplayer: "
                + ex.getMessage());
    }
    return inverse_tempo_temp;
}

    public void setTimeDen(int in) {
        this.timeDen = in;
    }
    public void setTimeNum(int in) {
        this.timeNum = in;
    }
}




