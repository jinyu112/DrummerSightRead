package com.tidisventures.drummersightread;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AnimationUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/** @class BoxedInt **/
class BoxedInt {
    public int value;
}

/** @class SheetMusic
 *
 * The SheetMusic Control is the main class for displaying the sheet music.
 * The SheetMusic class has the following public methods:
 *
 * SheetMusic()
 *   Create a new SheetMusic control from the given midi file and options.
 *
 * onDraw()
 *   Method called to draw the SheetMuisc
 *
 * shadeNotes()
 *   Shade all the notes played at a given pulse time.
 */
public class SheetMusic extends SurfaceView implements SurfaceHolder.Callback {

    /* Measurements used when drawing.  All measurements are in pixels. */
    public static final int LineWidth  = 2;   /** The width of a line */ //chd from 1
    public static final int LeftMargin = 4;   /** The left margin */
    public static final int LineSpace  = 14;   /** The space between lines in the staff */ //chd from 7
    public static final int StaffHeight = LineSpace*4 + LineWidth*5;
    /** The height between the 5 horizontal lines of the staff */

    public static final int NoteHeight = LineSpace + LineWidth;
    /** The height of a whole note */
    public static final int NoteWidth = 3 * LineSpace/2;
    /** The width of a whole note */

    public static final int PageWidth = 800;    /** The width of each page */
    public static final int PageHeight = 1050;  /** The height of each page (when printing) */
    public static final int TitleHeight = 14;   /** Height of title on first page */

    private ArrayList<Staff> staffs;  /** The array of staffs to display (from top to bottom) */
    private KeySignature mainkey;     /** The main key signature */

    private String   filename;        /** The midi filename */
    private int      numtracks;       /** The number of tracks */
    private float    zoom;            /** The zoom level to draw at (1.0 == 100%) */
    private float zoomFact;
    private boolean  scrollVert;      /** Whether to scroll vertically or horizontally */
    private int      showNoteLetters; /** Display the note letters */
    private int[]    NoteColors;      /** The note colors to use */
    private int      shade1;          /** The color for shading */
    private int      shade2;          /** The color for shading left-hand piano */
    private Paint    paint;           /** The paint for drawing */
    private boolean  surfaceReady;    /** True if we can draw on the surface */
    private Bitmap   bufferBitmap;    /** The bitmap for drawing */
    private Canvas   bufferCanvas;    /** The canvas for drawing */
    private MidiPlayer player;        /** For pausing the music */
    private int      playerHeight;    /** Height of the midi player */
    private int      screenwidth;     /** The screen width */
    private int      screenheight;    /** The screen height */

    /* fields used for scrolling */

    private int      sheetwidth;      /** The sheet music width (excluding zoom) */
    private int      sheetheight;     /** The sheet music height (excluding zoom) */
    private int      viewwidth;       /** The width of this view. */
    private int      viewheight;      /** The height of this view. */
    private int      bufferX;         /** The (left,top) of the bufferCanvas */
    private int      bufferY;
    private int      scrollX;         /** The (left,top) of the scroll clip */
    private int      scrollY;
    private int      startMotionX;    /** The x pixel when a touch motion starts */
    private int      startMotionY;    /** The y pixel when a touch motion starts */
    private float    deltaX;          /** The change in x-pixel of the last motion */
    private float    deltaY;          /** The change in y-pixel of the last motion */
    private boolean  inMotion;        /** True if we're in a motion event */
    private long     lastMotionTime;  /** Time of the last motion event (millsec) */
    private Handler  scrollTimer;     /** Timer for doing 'fling' scrolling */


    private int lastStartJin = 0;
    private ArrayList<MidiNote> returnedNotes;
    private ArrayList<MidiNote> notes;
    private ArrayList<MidiTrack> returnedTracks;
    private TimeSignature returnedTime;

    private int tempoInt = 60;
    private int timeNum = 4;
    private int timeDen = 4;


    public SheetMusic(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        bufferX = bufferY = scrollX = scrollY = 0;

        Activity activity = (Activity)context;
        screenwidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        screenheight = activity.getWindowManager().getDefaultDisplay().getHeight();
        playerHeight = MidiPlayer.getPreferredSize(screenwidth, screenheight).y;
        scrollTimer = new Handler();
        zoomFact= 1.0f;
    }

    /** Create a new SheetMusic View.
     * MidiFile is the parsed midi file to display.
     * SheetMusic Options are the menu options that were selected.
     *
     * - Apply all the Menu Options to the MidiFile tracks.
     * - Calculate the key signature
     * - For each track, create a list of MusicSymbols (notes, rests, bars, etc)
     * - Vertically align the music symbols in all the tracks
     * - Partition the music notes into horizontal staffs
     */

    public void init2(MidiOptions options) {

        zoom = 1.0f;


        filename = "temp";
        SetColors(null, options.shade1Color, options.shade2Color);
        paint = new Paint();
        paint.setTextSize(10.0f);
        paint.setColor(Color.BLACK);

        ArrayList<MidiTrack> tracks = genTrack();

        scrollVert = options.scrollVert; //if this is true, the music scrolls vertically
        showNoteLetters = options.showNoteLetters;

        TimeSignature time = new TimeSignature(timeNum,timeDen,96,convertBPMtoMicrosec(tempoInt));
        returnedTime = time;

        mainkey = new KeySignature(0,0);
        numtracks = tracks.size();

        int lastStart = 0;

        lastStart=lastStartJin;

        /* Create all the music symbols (notes, rests, vertical bars, and
         * clef changes).  The symbols variable contains a list of music
         * symbols for each track.  The list does not include the left-side
         * Clef and key signature symbols.  Those can only be calculated
         * when we create the staffs.
         */
        ArrayList<ArrayList<MusicSymbol>> allsymbols =
                new ArrayList<ArrayList<MusicSymbol> >(numtracks);

        for (int tracknum = 0; tracknum < numtracks; tracknum++) {
            MidiTrack track = tracks.get(tracknum);
            ClefMeasures clefs = new ClefMeasures(track.getNotes(), time.getMeasure());
            ArrayList<ChordSymbol> chords = CreateChords(track.getNotes(), mainkey, time, clefs);
            allsymbols.add(CreateSymbols(chords, clefs, time, lastStart));
        }

        ArrayList<ArrayList<LyricSymbol>> lyrics = null;
        if (options.showLyrics) {
            lyrics = GetLyrics(tracks);
        }

        /* Vertically align the music symbols */
        SymbolWidths widths = new SymbolWidths(allsymbols, lyrics);
        AlignSymbols(allsymbols, widths);

        staffs = CreateStaffs(allsymbols, mainkey, options, time.getMeasure());
        CreateAllBeamedChords(allsymbols, time);
        if (lyrics != null) {
            AddLyricsToStaffs(staffs, lyrics);
        }

        /* After making chord pairs, the stem directions can change,
         * which affects the staff height.  Re-calculate the staff height.
         */
        for (Staff staff : staffs) {
            staff.CalculateHeight();
        }


        zoom = 1.0f;
    }




    /** Calculate the size of the sheet music width and height
     *  (without zoom scaling to fit the screen).  Store the result in
     *  sheetwidth and sheetheight.
     */
    private void calculateSize() {
        sheetwidth = 0;
        sheetheight = 0;
        for (Staff staff : staffs) {
            sheetwidth = Math.max(sheetwidth, staff.getWidth());
            sheetheight += (staff.getHeight());
        }
        sheetwidth += 2;
        sheetheight += LeftMargin;
    }

    /* Adjust the zoom level so that the sheet music page (PageWidth)
     * fits within the width. If the heightspec is 0, return the screenheight.
     * Else, use the given view width/height.
     */
    @Override
    protected void onMeasure(int widthspec, int heightspec) {
        // First, calculate the zoom level
        int specwidth = MeasureSpec.getSize(widthspec);
        int specheight = MeasureSpec.getSize(heightspec);

        if (specwidth == 0 && specheight == 0) {
            setMeasuredDimension(screenwidth, screenheight);
        }
        else if (specwidth == 0) {
            setMeasuredDimension(screenwidth, specheight);
        }
        else if (specheight == 0) {
            setMeasuredDimension(specwidth, screenheight);
        }
        else {
            setMeasuredDimension(specwidth, specheight);
        }
    }


    /** If this is the first size change, calculate the zoom level,
     *  and create the bufferCanvas.  Otherwise, do nothing.
     */
    @Override
    protected void
    onSizeChanged(int newwidth, int newheight, int oldwidth, int oldheight) {
        viewwidth = newwidth;
        viewheight = newheight;

        if (bufferCanvas != null) {
            callOnDraw();
            return;
        }

        calculateSize();
        if (scrollVert) {
            zoom = (float)((newwidth - 2) * 1.0 / PageWidth);
        }
        else {
            zoom = (float)( (newheight + playerHeight) * 1.0 / sheetheight);
            if (zoom < 0.75)
                zoom = 0.75f;
            if (zoom > 1.1)
                zoom = 1.1f;
        }

        zoom = zoom*zoomFact;

        if (bufferCanvas == null) {
            createBufferCanvas();
        }

        // following if statement is for issue 17 (center notes for horizontal scrolling and disable the vertical movement of notes on touch)
        if (!scrollVert) {
            scrollY = (int) (sheetheight*zoom)/2 - viewheight/2;
        }

        callOnDraw();
    }


    /** Get the best key signature given the midi notes in all the tracks. */
    private KeySignature GetKeySignature(ArrayList<MidiTrack> tracks) {
        ListInt notenums = new ListInt();
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                notenums.add(note.getNumber());
            }
        }
        return KeySignature.Guess(notenums);
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

        //debug loop
        //Log.d("Drum11", "len1: " + len);
        for (int jj = 0; jj < len; jj++) {
            int startt = midinotes.get(jj).getStartTime();
            //Log.d("Drum11", startt + "!");
        }

        i=0;

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
            ChordSymbol chord = new ChordSymbol(notegroup, key, time, clef, this);
            chords.add(chord);
        }

        return chords;
    }

    /** Given the chord symbols for a track, create a new symbol list
     * that contains the chord symbols, vertical bars, rests, and
     * clef changes.
     * Return a list of symbols (ChordSymbol, BarSymbol, RestSymbol, ClefSymbol)
     */
    private ArrayList<MusicSymbol>
    CreateSymbols(ArrayList<ChordSymbol> chords, ClefMeasures clefs,
                  TimeSignature time, int lastStart) {

        ArrayList<MusicSymbol> symbols = new ArrayList<MusicSymbol>();
        symbols = AddBars(chords, time, lastStart);
        symbols = AddRests(symbols, time);
        symbols = AddClefChanges(symbols, clefs, time);

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
        symbols.add(new BarSymbol(measuretime) );
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

    /** The current clef is always shown at the beginning of the staff, on
     * the left side.  However, the clef can also change from measure to
     * measure. When it does, a Clef symbol must be shown to indicate the
     * change in clef.  This function adds these Clef change symbols.
     * This function does not add the main Clef Symbol that begins each
     * staff.  That is done in the Staff() contructor.
     */
    private
    ArrayList<MusicSymbol> AddClefChanges(ArrayList<MusicSymbol> symbols,
                                          ClefMeasures clefs,
                                          TimeSignature time) {

        ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>( symbols.size() );
        Clef prevclef = clefs.GetClef(0);
        for (MusicSymbol symbol : symbols) {
            /* A BarSymbol indicates a new measure */
            if (symbol instanceof BarSymbol) {
                Clef clef = clefs.GetClef(symbol.getStartTime());
                if (clef != prevclef) {
                    result.add(new ClefSymbol(clef, symbol.getStartTime()-1, true));
                }
                prevclef = clef;
            }
            result.add(symbol);
        }
        return result;
    }


    /** Notes with the same start times in different staffs should be
     * vertically aligned.  The SymbolWidths class is used to help
     * vertically align symbols.
     *
     * First, each track should have a symbol for every starttime that
     * appears in the Midi File.  If a track doesn't have a symbol for a
     * particular starttime, then add a "blank" symbol for that time.
     *
     * Next, make sure the symbols for each start time all have the same
     * width, across all tracks.  The SymbolWidths class stores
     * - The symbol width for each starttime, for each track
     * - The maximum symbol width for a given starttime, across all tracks.
     *
     * The method SymbolWidths.GetExtraWidth() returns the extra width
     * needed for a track to match the maximum symbol width for a given
     * starttime.
     */
    private
    void AlignSymbols(ArrayList<ArrayList<MusicSymbol>> allsymbols, SymbolWidths widths) {

        for (int track = 0; track < allsymbols.size(); track++) {
            ArrayList<MusicSymbol> symbols = allsymbols.get(track);
            ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>();

            int i = 0;

            /* If a track doesn't have a symbol for a starttime,
             * add a blank symbol.
             */
            for (int start : widths.getStartTimes()) {

                /* BarSymbols are not included in the SymbolWidths calculations */
                while (i < symbols.size() && (symbols.get(i) instanceof BarSymbol) &&
                        symbols.get(i).getStartTime() <= start) {
                    result.add(symbols.get(i));
                    i++;
                }

                if (i < symbols.size() && symbols.get(i).getStartTime() == start) {

                    while (i < symbols.size() &&
                            symbols.get(i).getStartTime() == start) {

                        result.add(symbols.get(i));
                        i++;
                    }
                }
                else {
                    result.add(new BlankSymbol(start, 0));
                }
            }

            /* For each starttime, increase the symbol width by
             * SymbolWidths.GetExtraWidth().
             */
            i = 0;
            while (i < result.size()) {
                if (result.get(i) instanceof BarSymbol) {
                    i++;
                    continue;
                }
                int start = result.get(i).getStartTime();
                int extra = widths.GetExtraWidth(track, start);
                int newwidth = result.get(i).getWidth() + extra;
                result.get(i).setWidth(newwidth);

                /* Skip all remaining symbols with the same starttime. */
                while (i < result.size() && result.get(i).getStartTime() == start) {
                    i++;
                }
            }
            allsymbols.set(track, result);
        }
    }


    /** Find 2, 3, 4, or 6 chord symbols that occur consecutively (without any
     *  rests or bars in between).  There can be BlankSymbols in between.
     *
     *  The startIndex is the index in the symbols to start looking from.
     *
     *  Store the indexes of the consecutive chords in chordIndexes.
     *  Store the horizontal distance (pixels) between the first and last chord.
     *  If we failed to find consecutive chords, return false.
     */
    private static boolean
    FindConsecutiveChords(ArrayList<MusicSymbol> symbols, TimeSignature time,
                          int startIndex, int[] chordIndexes,
                          BoxedInt horizDistance) {

        int i = startIndex;
        int numChords = chordIndexes.length;

        while (true) {
            horizDistance.value = 0;

            /* Find the starting chord */
            while (i < symbols.size() - numChords) {
                if (symbols.get(i) instanceof ChordSymbol) {
                    ChordSymbol c = (ChordSymbol) symbols.get(i);
                    if (c.getStem() != null) {
                        break;
                    }
                }
                i++;
            }
            if (i >= symbols.size() - numChords) {
                chordIndexes[0] = -1;
                return false;
            }
            chordIndexes[0] = i;
            boolean foundChords = true;
            for (int chordIndex = 1; chordIndex < numChords; chordIndex++) {
                i++;
                int remaining = numChords - 1 - chordIndex;
                while ((i < symbols.size() - remaining) &&
                        (symbols.get(i) instanceof BlankSymbol)) {

                    horizDistance.value += symbols.get(i).getWidth();
                    i++;
                }
                if (i >= symbols.size() - remaining) {
                    return false;
                }
                if (!(symbols.get(i) instanceof ChordSymbol)) {
                    foundChords = false;
                    break;
                }
                chordIndexes[chordIndex] = i;
                horizDistance.value += symbols.get(i).getWidth();
            }
            if (foundChords) {
                return true;
            }

            /* Else, start searching again from index i */
        }
    }


    /** Connect chords of the same duration with a horizontal beam.
     *  numChords is the number of chords per beam (2, 3, 4, or 6).
     *  if startBeat is true, the first chord must start on a quarter note beat.
     */
    private static void
    CreateBeamedChords(ArrayList<ArrayList<MusicSymbol>> allsymbols, TimeSignature time,
                       int numChords, boolean startBeat) {
        int[] chordIndexes = new int[numChords];
        ChordSymbol[] chords = new ChordSymbol[numChords];

        for (ArrayList<MusicSymbol> symbols : allsymbols) {
            int startIndex = 0;
            while (true) {
                BoxedInt horizDistance = new BoxedInt();
                horizDistance.value = 0;
                boolean found = FindConsecutiveChords(symbols, time,
                        startIndex,
                        chordIndexes,
                        horizDistance);
                if (!found) {
                    break;
                }
                for (int i = 0; i < numChords; i++) {
                    chords[i] = (ChordSymbol)symbols.get( chordIndexes[i] );
                }

                if (ChordSymbol.CanCreateBeam(chords, time, startBeat)) {
                    ChordSymbol.CreateBeam(chords, horizDistance.value);
                    startIndex = chordIndexes[numChords-1] + 1;
                }
                else {
                    startIndex = chordIndexes[0] + 1;
                }

                /* What is the value of startIndex here?
                 * If we created a beam, we start after the last chord.
                 * If we failed to create a beam, we start after the first chord.
                 */
            }
        }
    }


    /** Connect chords of the same duration with a horizontal beam.
     *
     *  We create beams in the following order:
     *  - 6 connected 8th note chords, in 3/4, 6/8, or 6/4 time
     *  - Triplets that start on quarter note beats
     *  - 3 connected chords that start on quarter note beats (12/8 time only)
     *  - 4 connected chords that start on quarter note beats (4/4 or 2/4 time only)
     *  - 2 connected chords that start on quarter note beats
     *  - 2 connected chords that start on any beat
     */
    private static void
    CreateAllBeamedChords(ArrayList<ArrayList<MusicSymbol>> allsymbols, TimeSignature time) {
        if ((time.getNumerator() == 3 && time.getDenominator() == 4) ||
                (time.getNumerator() == 6 && time.getDenominator() == 8) ||
                (time.getNumerator() == 6 && time.getDenominator() == 4) ) {

            CreateBeamedChords(allsymbols, time, 6, true);
        }
        CreateBeamedChords(allsymbols, time, 3, true);
        CreateBeamedChords(allsymbols, time, 4, true);
        CreateBeamedChords(allsymbols, time, 2, true);
        CreateBeamedChords(allsymbols, time, 2, false);
    }


    /** Get the width (in pixels) needed to display the key signature */
    public static int
    KeySignatureWidth(KeySignature key) {
        ClefSymbol clefsym = new ClefSymbol(Clef.Treble, 0, false);
        int result = clefsym.getMinWidth();
        AccidSymbol[] keys = key.GetSymbols(Clef.Treble);
        for (AccidSymbol symbol : keys) {
            result += symbol.getMinWidth();
        }
        return result + SheetMusic.LeftMargin + 5;
    }


    /** Given MusicSymbols for a track, create the staffs for that track.
     *  Each Staff has a maxmimum width of PageWidth (800 pixels).
     *  Also, measures should not span multiple Staffs.
     */
    private ArrayList<Staff>
    CreateStaffsForTrack(ArrayList<MusicSymbol> symbols, int measurelen,
                         KeySignature key, MidiOptions options,
                         int track, int totaltracks) {
        int keysigWidth = KeySignatureWidth(key);
        int startindex = 0;
        ArrayList<Staff> thestaffs = new ArrayList<Staff>(symbols.size() / 50);

        while (startindex < symbols.size()) {
            /* startindex is the index of the first symbol in the staff.
             * endindex is the index of the last symbol in the staff.
             */
            int endindex = startindex;
            int width = keysigWidth;
            int maxwidth;

            /* If we're scrolling vertically, the maximum width is PageWidth. */
            if (scrollVert) {
                maxwidth = SheetMusic.PageWidth;
            }
            else {
                maxwidth = 2000000;
            }

            while (endindex < symbols.size() &&
                    width + symbols.get(endindex).getWidth() < maxwidth) {

                width += symbols.get(endindex).getWidth();
                endindex++;
            }
            endindex--;

            /* There's 3 possibilities at this point:
             * 1. We have all the symbols in the track.
             *    The endindex stays the same.
             *
             * 2. We have symbols for less than one measure.
             *    The endindex stays the same.
             *
             * 3. We have symbols for 1 or more measures.
             *    Since measures cannot span multiple staffs, we must
             *    make sure endindex does not occur in the middle of a
             *    measure.  We count backwards until we come to the end
             *    of a measure.
             */

            if (endindex == symbols.size() - 1) {
                /* endindex stays the same */
            }
            else if (symbols.get(startindex).getStartTime() / measurelen ==
                    symbols.get(endindex).getStartTime() / measurelen) {
                /* endindex stays the same */
            }
            else {
                int endmeasure = symbols.get(endindex+1).getStartTime()/measurelen;
                while (symbols.get(endindex).getStartTime() / measurelen ==
                        endmeasure) {
                    endindex--;
                }
            }

            if (scrollVert) {
                width = SheetMusic.PageWidth;
            }
            // int range = endindex + 1 - startindex;
            ArrayList<MusicSymbol> staffSymbols = new ArrayList<MusicSymbol>();
            for (int i = startindex; i <= endindex; i++) {
                staffSymbols.add(symbols.get(i));
            }
            Staff staff = new Staff(staffSymbols, key, options, track, totaltracks);
            thestaffs.add(staff);
            startindex = endindex + 1;
        }
        return thestaffs;
    }


    /** Given all the MusicSymbols for every track, create the staffs
     * for the sheet music.  There are two parts to this:
     *
     * - Get the list of staffs for each track.
     *   The staffs will be stored in trackstaffs as:
     *
     *   trackstaffs[0] = { Staff0, Staff1, Staff2, ... } for track 0
     *   trackstaffs[1] = { Staff0, Staff1, Staff2, ... } for track 1
     *   trackstaffs[2] = { Staff0, Staff1, Staff2, ... } for track 2
     *
     * - Store the Staffs in the staffs list, but interleave the
     *   tracks as follows:
     *
     *   staffs = { Staff0 for track 0, Staff0 for track1, Staff0 for track2,
     *              Staff1 for track 0, Staff1 for track1, Staff1 for track2,
     *              Staff2 for track 0, Staff2 for track1, Staff2 for track2,
     *              ... }
     */
    private ArrayList<Staff>
    CreateStaffs(ArrayList<ArrayList<MusicSymbol>> allsymbols, KeySignature key,
                 MidiOptions options, int measurelen) {

        ArrayList<ArrayList<Staff>> trackstaffs =
                new ArrayList<ArrayList<Staff>>( allsymbols.size() );
        int totaltracks = allsymbols.size();

        for (int track = 0; track < totaltracks; track++) {
            ArrayList<MusicSymbol> symbols = allsymbols.get( track );
            trackstaffs.add(CreateStaffsForTrack(symbols, measurelen, key,
                    options, track, totaltracks));
        }

        /* Update the EndTime of each Staff. EndTime is used for playback */
        for (ArrayList<Staff> list : trackstaffs) {
            for (int i = 0; i < list.size()-1; i++) {
                list.get(i).setEndTime( list.get(i+1).getStartTime() );
            }
        }

        /* Interleave the staffs of each track into the result array. */
        int maxstaffs = 0;
        for (int i = 0; i < trackstaffs.size(); i++) {
            if (maxstaffs < trackstaffs.get(i).size()) {
                maxstaffs = trackstaffs.get(i).size();
            }
        }
        ArrayList<Staff> result = new ArrayList<Staff>(maxstaffs * trackstaffs.size());
        for (int i = 0; i < maxstaffs; i++) {
            for (ArrayList<Staff> list : trackstaffs) {
                if (i < list.size()) {
                    result.add(list.get(i));
                }
            }
        }
        return result;
    }


    /** Change the note colors for the sheet music, and redraw.
     *  This is not currently used.
     */
    public void SetColors(int[] newcolors, int newshade1, int newshade2) {
        if (NoteColors == null) {
            NoteColors = new int[12];
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = Color.BLACK;
            }
        }
        if (newcolors != null) {
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = newcolors[i];
            }
        }
        shade1 = newshade1;
        shade2 = newshade2;
    }

    /** Get the color for a given note number. Not currently used. */
    public int NoteColor(int number) {
        return NoteColors[ NoteScale.FromNumber(number) ];
    }

    /** Get the shade color */
    public int getShade1() { return shade1; }

    /** Get the shade2 color */
    public int getShade2() { return shade2; }

    /** Get whether to show note letters or not */
    public int getShowNoteLetters() { return showNoteLetters; }

    /** Get the main key signature */
    public KeySignature getMainKey() { return mainkey; }

    /** Get the lyrics for each track */
    private static ArrayList<ArrayList<LyricSymbol>>
    GetLyrics(ArrayList<MidiTrack> tracks) {
        boolean hasLyrics = false;
        ArrayList<ArrayList<LyricSymbol>> result = new ArrayList<ArrayList<LyricSymbol>>();
        for (int tracknum = 0; tracknum < tracks.size(); tracknum++) {
            ArrayList<LyricSymbol> lyrics = new ArrayList<LyricSymbol>();
            result.add(lyrics);
            MidiTrack track = tracks.get(tracknum);
            if (track.getLyrics() == null) {
                continue;
            }
            hasLyrics = true;
            for (MidiEvent ev : track.getLyrics()) {
                try {
                    String text = new String(ev.Value, 0, ev.Value.length, "UTF-8");
                    LyricSymbol sym = new LyricSymbol(ev.StartTime, text);
                    lyrics.add(sym);
                }
                catch (UnsupportedEncodingException e) {}
            }
        }
        if (!hasLyrics) {
            return null;
        }
        else {
            return result;
        }
    }

    /** Add the lyric symbols to the corresponding staffs */
    static void
    AddLyricsToStaffs(ArrayList<Staff> staffs, ArrayList<ArrayList<LyricSymbol>> tracklyrics) {
        for (Staff staff : staffs) {
            ArrayList<LyricSymbol> lyrics = tracklyrics.get(staff.getTrack());
            staff.AddLyrics(lyrics);
        }
    }



    /** Create a bitmap/canvas to use for double-buffered drawing.
     *  This is needed for shading the notes quickly.
     *  Instead of redrawing the entire sheet music on every shade call,
     *  we draw the sheet music to this bitmap canvas.  On subsequent
     *  calls to ShadeNotes(), we only need to draw the delta (the
     *  new notes to shade/unshade) onto the bitmap, and then draw the bitmap.
     *
     *  We include the MidiPlayer height (since we hide the MidiPlayer
     *  once the music starts playing). Also, we make the bitmap twice as
     *  large as the scroll viewable area, so that we don't need to
     *  refresh the bufferCanvas on every scroll change.
     */
    void createBufferCanvas() {
        if (bufferBitmap != null) {
            bufferCanvas = null;
            bufferBitmap.recycle();
            bufferBitmap = null;
        }
        if (scrollVert) {
            bufferBitmap = Bitmap.createBitmap(viewwidth,
                    (viewheight + playerHeight) * 2,
                    Bitmap.Config.ARGB_8888); //actual thing being drawn on
        }
        else {
            bufferBitmap = Bitmap.createBitmap(viewwidth * 2,
                    (viewheight + playerHeight) * 2,
                    Bitmap.Config.ARGB_8888); //actual thing being drawn on
        }

        bufferCanvas = new Canvas(bufferBitmap);
        drawToBuffer(scrollX, scrollY);
    }


    /** Obtain the drawing canvas and call onDraw() */
    public void callOnDraw() {
        if (!surfaceReady) {
            return;
        }
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        onDraw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    /** Draw the SheetMusic. */
    @Override
    protected void onDraw(Canvas canvas) {
        if (bufferBitmap == null) {
            createBufferCanvas();
        }
        if (!isScrollPositionInBuffer()) {
            drawToBuffer(scrollX, scrollY);
        }

        // We want (scrollX - bufferX, scrollY - bufferY)
        // to be (0,0) on the canvas
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
    }

    /** Return true if the scrollX/scrollY is in the bufferBitmap */
    private boolean isScrollPositionInBuffer() {
        if ((scrollY < bufferY) ||
                (scrollX < bufferX) ||
                (scrollY > bufferY + bufferBitmap.getHeight()/3) ||
                (scrollX > bufferX + bufferBitmap.getWidth()/3) ) {

            return false;
        }
        else {
            return true;
        }
    }

    /** Draw the SheetMusic to the bufferCanvas, with the
     * given (left,top) corner.
     *
     * Scale the graphics by the current zoom factor.
     * Only draw Staffs which lie inside the buffer area.
     */
    private void drawToBuffer(int left, int top) {
        if (staffs == null) {
            return;
        }

        bufferX =left;
        bufferY = top;

        bufferCanvas.translate(-bufferX, -bufferY);
        Rect clip = new Rect(bufferX, bufferY,
                bufferX + bufferBitmap.getWidth(),
                bufferY + bufferBitmap.getHeight());

        // Scale both the canvas and the clip by the zoom factor
        clip.left   = (int)(clip.left   / zoom);
        clip.top    = (int)(clip.top    / zoom);
        clip.right  = (int)(clip.right  / zoom);
        clip.bottom = (int)(clip.bottom / zoom);
        bufferCanvas.scale(zoom, zoom);

        // Draw a white background
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        bufferCanvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        // Draw the staffs in the clip area
        int ypos = 0;
        for (Staff staff : staffs) {
            if ((ypos + staff.getHeight() < clip.top) || (ypos > clip.bottom))  {
                /* Staff is not in the clip, don't need to draw it */
            }
            else {
                bufferCanvas.translate(0, ypos);
                staff.Draw(bufferCanvas, clip, paint);
                bufferCanvas.translate(0, -ypos);
            }

            ypos += staff.getHeight();
        }
        bufferCanvas.scale(1.0f/zoom, 1.0f/zoom);
        bufferCanvas.translate(bufferX, bufferY);
    }


    /** Write the MIDI filename at the top of the page */
    private void DrawTitle(Canvas canvas) {
        int leftmargin = 20;
        int topmargin = 20;
        String title = filename;
        title = title.replace(".mid", "").replace("_", " ");
        paint.setTextSize(12.0f);
        canvas.translate(leftmargin, topmargin);
        canvas.drawText(title, 0, 0, paint);
        canvas.translate(-leftmargin, -topmargin);
        paint.setTextSize(10.0f);
    }

    /**
     * Return the number of pages needed to print this sheet music.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public int GetTotalPages() {
        int num = 1;
        int currheight = TitleHeight;

        if (numtracks == 2 && (staffs.size() % 2) == 0) {
            for (int i = 0; i < staffs.size(); i += 2) {
                int heights = staffs.get(i).getHeight() + staffs.get(i+1).getHeight();
                if (currheight + heights > PageHeight) {
                    num++;
                    currheight = heights;
                }
                else {
                    currheight += heights;
                }
            }
        }
        else {
            for (Staff staff : staffs) {
                if (currheight + staff.getHeight() > PageHeight) {
                    num++;
                    currheight = staff.getHeight();
                }
                else {
                    currheight += staff.getHeight();
                }
            }
        }
        return num;
    }

    /** Draw the given page of the sheet music.
     * Page numbers start from 1.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public void DrawPage(Canvas canvas, int pagenumber)
    {
        int leftmargin = 20;
        int topmargin = 20;
        int rightmargin = 20;
        int bottommargin = 20;

        float scale = 1.0f;
        Rect clip = new Rect(0, 0, PageWidth + 40, PageHeight + 40);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        int ypos = TitleHeight;
        int pagenum = 1;
        int staffnum = 0;

        if (numtracks == 2 && (staffs.size() % 2) == 0) {
            /* Skip the staffs until we reach the given page number */
            while (staffnum + 1 < staffs.size() && pagenum < pagenumber) {
                int heights = staffs.get(staffnum).getHeight() +
                        staffs.get(staffnum+1).getHeight();
                if (ypos + heights >= PageHeight) {
                    pagenum++;
                    ypos = 0;
                }
                else {
                    ypos += heights;
                    staffnum += 2;
                }
            }
            /* Print the staffs until the height reaches PageHeight */
            if (pagenum == 1) {
                DrawTitle(canvas);
                ypos = TitleHeight;
            }
            else {
                ypos = 0;
            }
            for (; staffnum + 1 < staffs.size(); staffnum += 2) {
                int heights = staffs.get(staffnum).getHeight() +
                        staffs.get(staffnum+1).getHeight();

                if (ypos + heights >= PageHeight)
                    break;

                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum).getHeight();
                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum + 1).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum + 1).getHeight();
            }
        }

        else {
            /* Skip the staffs until we reach the given page number */
            while (staffnum < staffs.size() && pagenum < pagenumber) {
                if (ypos + staffs.get(staffnum).getHeight() >= PageHeight) {
                    pagenum++;
                    ypos = 0;
                }
                else {
                    ypos += staffs.get(staffnum).getHeight();
                    staffnum++;
                }
            }

            /* Print the staffs until the height reaches viewPageHeight */
            if (pagenum == 1) {
                DrawTitle(canvas);
                ypos = TitleHeight;
            }
            else {
                ypos = 0;
            }
            for (; staffnum < staffs.size(); staffnum++) {
                if (ypos + staffs.get(staffnum).getHeight() >= PageHeight)
                    break;

                canvas.translate(leftmargin, topmargin + ypos);
                staffs.get(staffnum).Draw(canvas, clip, paint);
                canvas.translate(-leftmargin, -(topmargin + ypos));
                ypos += staffs.get(staffnum).getHeight();
            }
        }

        /* Draw the page number */
        canvas.drawText("" + pagenumber,
                PageWidth - leftmargin,
                topmargin + PageHeight - 12,
                paint);

    }


    /** Shade all the chords played at the given pulse time.
     *  First, make sure the current scroll position is in the bufferBitmap.
     *  Loop through all the staffs and call staff.Shade().
     *  If scrollGradually is true, scroll gradually (smooth scrolling)
     *  to the shaded notes.
     */
    public void ShadeNotes(int currentPulseTime, int prevPulseTime,
                           boolean scrollGradually)  {
        if (!surfaceReady || staffs == null) {
            return;
        }
        if (bufferCanvas == null) {
            createBufferCanvas();
        }

        /* If the scroll position is not in the bufferCanvas,
         * we need to redraw the sheet music into the bufferCanvas
         */
        if (!isScrollPositionInBuffer()) {
            drawToBuffer(scrollX, scrollY);
        }

        /* We're going to draw the shaded notes into the bufferCanvas.
         * Translate, so that (bufferX, bufferY) maps to (0,0) on the canvas
         */
        bufferCanvas.translate(-bufferX, -bufferY);

        /* Loop through each staff.  Each staff will shade any notes that
         * start at currentPulseTime, and unshade notes at prevPulseTime.
         */
        int x_shade = 0;
        int y_shade = 0;
        paint.setAntiAlias(true);
        bufferCanvas.scale(zoom, zoom);
        int ypos = 0;
        for (Staff staff : staffs) {
            bufferCanvas.translate(0, ypos);
            x_shade = staff.ShadeNotes(bufferCanvas, paint, shade1,
                    currentPulseTime, prevPulseTime, x_shade);
            bufferCanvas.translate(0, -ypos);
            ypos += staff.getHeight();
            if (currentPulseTime >= staff.getEndTime()) {
                y_shade += staff.getHeight();
            }
        }
        bufferCanvas.scale(1.0f/zoom, 1.0f/zoom);
        bufferCanvas.translate(bufferX, bufferY);

        /* We have the (x,y) position of the shaded notes.
         * Calculate the new scroll position.
         */
        if (currentPulseTime >= 0) {
            x_shade = (int)(x_shade * zoom);
            y_shade -= NoteHeight;
            y_shade = (int)(y_shade * zoom);
            ScrollToShadedNotes(x_shade, y_shade, scrollGradually);
        }

        /* If the new scrollX, scrollY is not in the buffer,
         * we have to call this method again.
         */
        if (scrollX < bufferX || scrollY < bufferY) {
            ShadeNotes(currentPulseTime, prevPulseTime, scrollGradually);
            return;
        }

        /* Draw the buffer canvas to the real canvas.
         * Translate canvas such that (scrollX,scrollY) within the
         * bufferCanvas maps to (0,0) on the real canvas.
         */
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
        holder.unlockCanvasAndPost(canvas);
    }

    /** Scroll the sheet music so that the shaded notes are visible.
     * If scrollGradually is true, scroll gradually (smooth scrolling)
     * to the shaded notes. Update the scrollX/scrollY fields.
     */
    void ScrollToShadedNotes(int x_shade, int y_shade, boolean scrollGradually) {

        if (scrollVert) {
            int scrollDist = (int)(y_shade - scrollY);

            if (scrollGradually) {
                if (scrollDist > (zoom * StaffHeight * 8))
                    scrollDist = scrollDist/2;
                else if (scrollDist > (NoteHeight * 4 * zoom))
                    scrollDist = (int)(NoteHeight * 4 * zoom);
            }
            scrollY += scrollDist;
        }
        else {

            int x_view = scrollX + viewwidth * 40/100;
            int xmax   = scrollX + viewwidth * 65/100;
            int scrollDist = x_shade - x_view;

            if (scrollGradually) {
                if (x_shade > xmax)
                    scrollDist = (x_shade - x_view)/3;
                else if (x_shade > x_view)
                    scrollDist = (x_shade - x_view)/6;
            }

            scrollX += scrollDist;
        }
        checkScrollBounds();
    }

    /** Check that the scrollX/scrollY position does not exceed
     *  the bounds of the sheet music.
     */
    private void
    checkScrollBounds() {
        // Get the width/height of the scrollable area
        int scrollwidth = (int)(sheetwidth * zoom);
        int scrollheight = (int)(sheetheight * zoom);

        // this following if statement prevents the horizontal shift of notes on touch
        if (!scrollVert) {
            if (scrollX < 0) {
                scrollX = 0;
            }

            if (scrollX > scrollwidth - viewwidth / 2) {
                scrollX = scrollwidth - viewwidth / 2;
            }
        }

        // following if statement is for issue 17 (center notes for horizontal scrolling and disable the vertical shift of notes on touch)
        if (scrollVert) {
            if (scrollY < 0) {
                scrollY = 0;
            }

            if (scrollY > scrollheight - viewheight / 2) {
                scrollY = scrollheight - viewheight / 2; // this line causes the notes to jump to the center if user touches screen
                // and zoom is 0.5f
            }
        }

    }


    /** Handle touch/motion events to implement scrolling the sheet music.
     *  - On down touch, store the (x,y) of the touch
     *  - On a motion event, calculate the delta (change) in x, y.
     *    Update the scrolX, scrollY and redraw the sheet music.
     *  - On a up touch, implement a 'fling'.  Call flingScroll
     *    every 50 msec for the next 2 seconds.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                deltaX = deltaY = 0;
                scrollTimer.removeCallbacks(flingScroll);
                if (player != null && player.getVisibility() == View.GONE) {
                    player.Pause();
                    inMotion = false;
                    return true;
                }
                inMotion = true;
                startMotionX = (int)event.getX();
                startMotionY = (int)event.getY();

                return true;
            case MotionEvent.ACTION_MOVE:
                if (!inMotion)
                    return false;

                deltaX = startMotionX - event.getX();
                deltaY = startMotionY - event.getY();
                startMotionX = (int)event.getX();
                startMotionY = (int)event.getY();
                if (scrollVert) {
                    scrollY += (int)deltaY;
                }
                else {
                    scrollX += (int)deltaX;
                    if ((Math.abs(deltaY) > Math.abs(deltaX)) ||
                            (Math.abs(deltaY) > 4)) {
                        // following commented statement is for issue 17 (center notes for horizontal scrolling and disable the vertical shift of notes on touch)
                        //scrollY += (int)deltaY; //commenting this out disables the vertical scrolling by the user
                    }

                }
                checkScrollBounds();
                lastMotionTime = AnimationUtils.currentAnimationTimeMillis();
                callOnDraw();

                return true;

            case MotionEvent.ACTION_UP:
                inMotion = false;
                long deltaTime = AnimationUtils.currentAnimationTimeMillis() - lastMotionTime;
                if (deltaTime >= 100) {
                    return true;
                }
                if (scrollVert && (Math.abs(deltaY) <= 5)) {
                    return true;
                }
                if (!scrollVert && (Math.abs(deltaX) <= 5)) {
                    return true;
                }

                /* Keep scrolling for 2 more seconds.
                 * Scale the delta to 20 msec.
                 * Make sure delta doesn't exceed the maximum scroll delta.
                 */
                int msecInterval = 20;
                deltaX = deltaX * msecInterval/deltaTime;
                deltaY = deltaY * msecInterval/deltaTime;
                int maxscroll = StaffHeight * 4;
                if (Math.abs(deltaX) > maxscroll) {
                    deltaX = deltaX/Math.abs(deltaX) * StaffHeight;
                }
                if (Math.abs(deltaY) > maxscroll) {
                    deltaY = deltaY/Math.abs(deltaY) * StaffHeight;
                }
                int duration = 2000 / msecInterval;
                for (int i = 1; i <= duration; i++) {
                    scrollTimer.postDelayed(flingScroll, i * msecInterval);
                }
                return true;
            default:
                return false;
        }
    }

    /** The timer callback for doing 'fling' scrolling.
     *  Adjust the scrollX/scrollY using the last delta.
     *  Redraw the sheet music.
     *  Then, schedule this timer again, after 30 msec.
     */
    Runnable flingScroll = new Runnable() {
        public void run() {
            if (scrollVert && (Math.abs(deltaY) >= 5)) {
                scrollY += (int)deltaY;
                checkScrollBounds();
                callOnDraw();
                deltaY = deltaY * 9.2f/10.0f;
            }
            else if (!scrollVert && (Math.abs(deltaX) >= 5)) {
                scrollX += (int)deltaX;
                checkScrollBounds();
                callOnDraw();
                deltaX = deltaX * 9.2f/10.0f;
            }
        }
    };

    public void setPlayer(MidiPlayer p) {
        player = p;
    }

    public void
    surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        callOnDraw();
    }

    /** Surface is ready for shading the notes */
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
    }

    /** Surface has been destroyed */
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    @Override
    public String toString() {
        String result = "SheetMusic staffs=" + staffs.size() + "\n";
        for (Staff staff : staffs) {
            result += staff.toString();
        }
        result += "End SheetMusic\n";
        return result;
    }










//this functions were defined by jin
    private ArrayList<MidiTrack> genTrack() {
        ArrayList<MidiTrack> generated_tracks = new ArrayList<MidiTrack>(1);
        //ArrayList<MidiNote> notes = genNotes();
        MidiTrack track = new MidiTrack(0);
        track.setNotes(notes);
        generated_tracks.add(track);
        returnedTracks = generated_tracks;
        return generated_tracks;
    }


    private ArrayList<MidiNote> genNotes() { //this was moved to genNotesMain in main activity. genNOtes is no longer used
        ArrayList<MidiNote> notes = new ArrayList<MidiNote>(12);

        int numNotes=8*2;

        int runningStartTime = 0;
        for (int i = 0; i < numNotes; i++) {
            int randomNum = 1 + (int)(Math.random() * 2);
            MidiNote note = new MidiNote(0,0,0,0);
            note.setChannel(0);
            note.setDuration(48);
            note.setNumber(60);
            note.setStartTime(runningStartTime);

            if (randomNum==1) {
                runningStartTime += 48;
            }
            else {
                runningStartTime += 96;
            }
            notes.add(note);
        }
        lastStartJin = runningStartTime+1; //plus one because of end of sheet music measure display issue
        returnedNotes=notes;
        return notes;
    }

public ArrayList<MidiNote> getNotes() {return returnedNotes;}

public ArrayList<MidiTrack> getTracks() {return returnedTracks;}

public TimeSignature getTime() {return returnedTime;}


public void setNotes(ArrayList<MidiNote> notes_in) {this.notes=notes_in;}

public void setLastStartJin(int lastStart_in) {this.lastStartJin=lastStart_in;}

public void setZoom(float in) {
    this.zoomFact = in;
}

public void stopMusic() {
    //if (player==null) return;
    player.setStopSound();
}

    private int convertBPMtoMicrosec(int tempo_in) {
        int microseconds = 1000000;
        if (tempo_in != 0) {
            microseconds = (int) Math.round(60000000 / tempo_in);
        }
        return microseconds;
    }

    public void setTempoInt(int in) {
        this.tempoInt = in;
    }

    public void setTimeNum(int in) {
        this.timeNum = in;
    }

    public void setTimeDen(int in) {
        this.timeDen = in;
    }
}

