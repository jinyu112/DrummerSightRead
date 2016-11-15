package com.tidisventures.drummersightread;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/** @class ChordSymbol
 * A chord symbol represents a group of notes that are played at the same
 * time.  A chord includes the notes, the accidental symbols for each
 * note, and the stem (or stems) to use.  A single chord may have two
 * stems if the notes have different durations (e.g. if one note is a
 * quarter note, and another is an eighth note).
 */
public class ChordSymbol implements MusicSymbol {
    private Clef clef;             /** Which clef the chord is being drawn in */
    private int starttime;         /** The time (in pulses) the notes occurs at */
    private int endtime;           /** The starttime plus the longest note duration */
    private NoteData[] notedata;   /** The notes to draw */
    private AccidSymbol[] accidsymbols;   /** The accidental symbols to draw */
    private int width;             /** The width of the chord */
    private Stem stem1;            /** The stem of the chord. Can be null. */
    private Stem stem2;            /** The second stem of the chord. Can be null */
    private boolean hastwostems;   /** True if this chord has two stems */
    private SheetMusic sheetmusic; /** Used to get colors and other options */
    private AccentSymbol[] accentSymbols;
    private RollSymbol[] rollSymbols;
    private FlamSymbol[] flamSymbols;


    /** Create a new Chord Symbol from the given list of midi notes.
     * All the midi notes will have the same start time.  Use the
     * key signature to get the white key and accidental symbol for
     * each note.  Use the time signature to calculate the duration
     * of the notes. Use the clef when drawing the chord.
     */
    public ChordSymbol(ArrayList<MidiNote> midinotes, KeySignature key,
                       TimeSignature time, Clef c, SheetMusic sheet) {

        int len = midinotes.size();
        int i;

        hastwostems = false;
        clef = c;
        sheetmusic = sheet;

        starttime = midinotes.get(0).getStartTime();
        endtime = midinotes.get(0).getEndTime();

        for (i = 0; i < len; i++) {
            if (i > 1) {
                if (!(midinotes.get(i).getNumber() >= midinotes.get(i-1).getNumber()) ) {
                    throw new IllegalArgumentException();
                }
            }
            endtime = Math.max(endtime, midinotes.get(i).getEndTime());
        }

        notedata = CreateNoteData(midinotes, key, time);
        accidsymbols = CreateAccidSymbols(notedata, clef);
        accentSymbols = CreateAccentSymbols(notedata, clef);
        rollSymbols = CreateRollSymbols(notedata, clef);
        flamSymbols = CreateFlamSymbols(notedata, clef);


        /* Find out how many stems we need (1 or 2) */
        NoteDuration dur1 = notedata[0].duration;
        NoteDuration dur2 = dur1;
        int change = -1;
        for (i = 0; i < notedata.length; i++) {
            dur2 = notedata[i].duration;
            if (dur1 != dur2) {
                change = i;
                break;
            }
        }

        if (dur1 != dur2) {
            /* We have notes with different durations.  So we will need
             * two stems.  The first stem points down, and contains the
             * bottom note up to the note with the different duration.
             *
             * The second stem points up, and contains the note with the
             * different duration up to the top note.
             */
            hastwostems = true;
            stem1 = new Stem(notedata[0].whitenote,
                    notedata[change-1].whitenote,
                    dur1,
                    Stem.Down,
                    NotesOverlap(notedata, 0, change)
            );

            stem2 = new Stem(notedata[change].whitenote,
                    notedata[notedata.length-1].whitenote,
                    dur2,
                    Stem.Up,
                    NotesOverlap(notedata, change, notedata.length)
            );
        }
        else {
            /* All notes have the same duration, so we only need one stem. */
            int direction = StemDirection(notedata[0].whitenote,
                    notedata[notedata.length-1].whitenote,
                    clef);

            stem1 = new Stem(notedata[0].whitenote,
                    notedata[notedata.length-1].whitenote,
                    dur1,
                    direction,
                    NotesOverlap(notedata, 0, notedata.length)
            );
            stem2 = null;
        }

        /* For whole notes, no stem is drawn. */
        if (dur1 == NoteDuration.Whole)
            stem1 = null;
        if (dur2 == NoteDuration.Whole)
            stem2 = null;

        width = getMinWidth();
    }


    /** Given the raw midi notes (the note number and duration in pulses),
     * calculate the following note data:
     * - The white key
     * - The accidental (if any)
     * - The note duration (half, quarter, eighth, etc)
     * - The side it should be drawn (left or side)
     * By default, notes are drawn on the left side.  However, if two notes
     * overlap (like A and B) you cannot draw the next note directly above it.
     * Instead you must shift one of the notes to the right.
     *
     * The KeySignature is used to determine the white key and accidental.
     * The TimeSignature is used to determine the duration.
     */

    private static NoteData[]
    CreateNoteData(ArrayList<MidiNote> midinotes, KeySignature key,
                   TimeSignature time) {

        int len = midinotes.size();
        NoteData[] notedata = new NoteData[len];

        for (int i = 0; i < len; i++) {
            MidiNote midi = midinotes.get(i);
            notedata[i] = new NoteData();
            notedata[i].number = midi.getNumber();
            notedata[i].leftside = true;
            notedata[i].whitenote = key.GetWhiteNote(midi.getNumber());
            notedata[i].duration = time.GetNoteDuration(midi.getEndTime() - midi.getStartTime());
            notedata[i].accid = key.GetAccidental(midi.getNumber(), midi.getStartTime() / time.getMeasure());

            //Accents
            if (midi.getAccentNum()==1) {
                notedata[i].accent = Accent.Marcato;
            }
            else if (midi.getAccentNum()==2) {
                notedata[i].accent = Accent.Regular;
            }
            else {
                notedata[i].accent = Accent.None;
            }

            //Rolls
            if (midi.getRollNum()==1) {
                notedata[i].roll = Roll.Single;
            }
            else if (midi.getRollNum()==2) {
                notedata[i].roll = Roll.Double;
            }
            else if (midi.getRollNum()==3) {
                notedata[i].roll = Roll.Triple;
            }
            else {
                notedata[i].roll = Roll.None;
            }

            //Flams
            if (midi.getFlamNum()==1) {
                notedata[i].flam = Flam.Flam;
            }
            else {
                notedata[i].flam = Flam.None;
            }



            if (i > 0 && (notedata[i].whitenote.Dist(notedata[i-1].whitenote) == 1)) {
                /* This note (notedata[i]) overlaps with the previous note.
                 * Change the side of this note.
                 */

                if (notedata[i-1].leftside) {
                    notedata[i].leftside = false;
                } else {
                    notedata[i].leftside = true;
                }
            } else {
                notedata[i].leftside = true;
            }
        }
        return notedata;
    }


    /** Given the note data (the white keys and accidentals), create
     * the Accidental Symbols and return them.
     */
    private static AccidSymbol[]
    CreateAccidSymbols(NoteData[] notedata, Clef clef) {
        int count = 0;
        for (NoteData n : notedata) {
            if (n.accid != Accid.None) {
                count++;
            }
        }
        AccidSymbol[] accidsymbols = new AccidSymbol[count];
        int i = 0;
        for (NoteData n : notedata) {
            if (n.accid != Accid.None) {
                accidsymbols[i] = new AccidSymbol(n.accid, n.whitenote, clef);
                i++;
            }
        }
        return accidsymbols;
    }


    /** Given the note data (the white keys and accents), create
     * the accent Symbols and return them.
     */
    private static AccentSymbol[]
    CreateAccentSymbols(NoteData[] notedata, Clef clef) {
        int count = 0;
        for (NoteData n : notedata) {
            if (n.accent != Accent.None) {
                count++;
            }
        }
        AccentSymbol[] accentsymbols = new AccentSymbol[count];
        int i = 0;
        for (NoteData n : notedata) {
            if (n.accent != Accent.None) {
                accentsymbols[i] = new AccentSymbol(n.accent, n.whitenote, clef, n.duration);
                i++;
            }
        }
        return accentsymbols;
    }


    private static RollSymbol[]
    CreateRollSymbols(NoteData[] notedata, Clef clef) {
        int count = 0;
        for (NoteData n : notedata) {
            if (n.roll != Roll.None) {
                count++;
            }
        }
        RollSymbol[] rollsymbols = new RollSymbol[count];
        int i = 0;
        for (NoteData n : notedata) {
            if (n.roll != Roll.None) {
                rollsymbols[i] = new RollSymbol(n.roll, n.whitenote, clef, n.duration);
                i++;
            }
        }
        return rollsymbols;
    }


    private static FlamSymbol[]
    CreateFlamSymbols(NoteData[] notedata, Clef clef) {
        int count = 0;
        for (NoteData n : notedata) {
            if (n.flam != Flam.None) {
                count++;
            }
        }
        FlamSymbol[] flamsymbols = new FlamSymbol[count];
        int i = 0;
        for (NoteData n : notedata) {
            if (n.flam != Flam.None) {
                flamsymbols[i] = new FlamSymbol(n.flam, n.whitenote, clef);
                i++;
            }
        }
        return flamsymbols;
    }


    /** Calculate the stem direction (Up or down) based on the top and
     * bottom note in the chord.  If the average of the notes is above
     * the middle of the staff, the direction is down.  Else, the
     * direction is up.
     */
    private static int
    StemDirection(WhiteNote bottom, WhiteNote top, Clef clef) {
        WhiteNote middle;
        if (clef == Clef.Treble)
            middle = new WhiteNote(WhiteNote.B, 5);
        else
            middle = new WhiteNote(WhiteNote.D, 3);

        int dist = middle.Dist(bottom) + middle.Dist(top);
        if (dist >= 0)
            return Stem.Up;
        else
            return Stem.Down;
    }

    /** Return whether any of the notes in notedata (between start and
     * end indexes) overlap.  This is needed by the Stem class to
     * determine the position of the stem (left or right of notes).
     */
    private static boolean NotesOverlap(NoteData[] notedata, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!notedata[i].leftside) {
                return true;
            }
        }
        return false;
    }

    /** Get the time (in pulses) this symbol occurs at.
     * This is used to determine the measure this symbol belongs to.
     */
    public int getStartTime() { return starttime; }

    /** Get the end time (in pulses) of the longest note in the chord.
     * Used to determine whether two adjacent chords can be joined
     * by a stem.
     */
    public int getEndTime() { return endtime; }

    /** Return the clef this chord is drawn in. */
    public Clef getClef() { return clef; }

    /** Return true if this chord has two stems */
    public boolean getHasTwoStems() { return hastwostems; }

    /* Return the stem will the smallest duration.  This property
     * is used when making chord pairs (chords joined by a horizontal
     * beam stem). The stem durations must match in order to make
     * a chord pair.  If a chord has two stems, we always return
     * the one with a smaller duration, because it has a better
     * chance of making a pair.
     */
    public Stem getStem() {
        if (stem1 == null) { return stem2; }
        else if (stem2 == null) { return stem1; }
        else if (stem1.getDuration().ordinal() < stem2.getDuration().ordinal()) { return stem1; }
        else { return stem2; }
    }

    /** Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     */
    public int getWidth() { return width; }
    public void setWidth(int value) { width = value; }

    /* Return the minimum width needed to display this chord.
     *
     * The accidental symbols can be drawn above one another as long
     * as they don't overlap (they must be at least 6 notes apart).
     * If two accidental symbols do overlap, the accidental symbol
     * on top must be shifted to the right.  So the width needed for
     * accidental symbols depends on whether they overlap or not.
     *
     * If we are also displaying the letters, include extra width.
     */
    public int getMinWidth() {
        /* The width needed for the note circles */
        int result = 2*SheetMusic.NoteHeight + SheetMusic.NoteHeight*3/4;

        if (accidsymbols.length > 0) {
            result += accidsymbols[0].getMinWidth();
            for (int i = 1; i < accidsymbols.length; i++) {
                AccidSymbol accid = accidsymbols[i];
                AccidSymbol prev = accidsymbols[i-1];
                if (accid.getNote().Dist(prev.getNote()) < 6) {
                    result += accid.getMinWidth();
                }
            }
        }
        if (sheetmusic != null && sheetmusic.getShowNoteLetters() != MidiOptions.NoteNameNone) {
            result += 8;
        }
        return result;
    }


    /** Get the number of pixels this symbol extends above the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getAboveStaff() {
        /* Find the topmost note in the chord */
        WhiteNote topnote = notedata[ notedata.length-1 ].whitenote;

        /* The stem.End is the note position where the stem ends.
         * Check if the stem end is higher than the top note.
         */
        if (stem1 != null)
            topnote = WhiteNote.Max(topnote, stem1.getEnd());
        if (stem2 != null)
            topnote = WhiteNote.Max(topnote, stem2.getEnd());

        int dist = topnote.Dist(WhiteNote.Top(clef)) * SheetMusic.NoteHeight/2;
        int result = 0;
        if (dist > 0)
            result = dist;

        /* Check if any accidental symbols extend above the staff */
        for (AccidSymbol symbol : accidsymbols) {
            if (symbol.getAboveStaff() > result) {
                result = symbol.getAboveStaff();
            }
        }

        /* Check if any accidental symbols extend above the staff */
        for (AccentSymbol accentsymbol : accentSymbols) {
            if (accentsymbol.getAboveStaff() > result) {
                result = accentsymbol.getAboveStaff();
            }
        }

        /* Check if any roll symbols extend above the staff */
        for (RollSymbol rollsymbol : rollSymbols) {
            if (rollsymbol.getAboveStaff() > result) {
                result = rollsymbol.getAboveStaff();
            }
        }
        return result;
    }

    /** Get the number of pixels this symbol extends below the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff() {
        /* Find the bottom note in the chord */
        WhiteNote bottomnote = notedata[0].whitenote;

        /* The stem.End is the note position where the stem ends.
         * Check if the stem end is lower than the bottom note.
         */
        if (stem1 != null)
            bottomnote = WhiteNote.Min(bottomnote, stem1.getEnd());
        if (stem2 != null)
            bottomnote = WhiteNote.Min(bottomnote, stem2.getEnd());

        int dist = WhiteNote.Bottom(clef).Dist(bottomnote) *
                SheetMusic.NoteHeight/2;

        int result = 0;
        if (dist > 0)
            result = dist;

        /* Check if any accidental symbols extend below the staff */
        for (AccidSymbol symbol : accidsymbols) {
            if (symbol.getBelowStaff() > result) {
                result = symbol.getBelowStaff();
            }
        }

                /* Check if any accidental symbols extend below the staff */
        for (AccentSymbol accentsymbol : accentSymbols) {
            if (accentsymbol.getBelowStaff() > result) {
                result = accentsymbol.getBelowStaff();
            }
        }

                /* Check if any roll symbols extend below the staff */
        for (RollSymbol rollsymbol : rollSymbols) {
            if (rollsymbol.getBelowStaff() > result) {
                result = rollsymbol.getBelowStaff();
            }
        }
        return result;
    }


    /** Get the name for this note */
    private String NoteName(int notenumber, WhiteNote whitenote) {
        if (sheetmusic.getShowNoteLetters() == MidiOptions.NoteNameLetter) {
            return Letter(notenumber, whitenote);
        }
        else if (sheetmusic.getShowNoteLetters() == MidiOptions.NoteNameFixedDoReMi) {
            String[] fixedDoReMi = {
                    "La", "Li", "Ti", "Do", "Di", "Re", "Ri", "Mi", "Fa", "Fi", "So", "Si"
            };
            int notescale = NoteScale.FromNumber(notenumber);
            return fixedDoReMi[notescale];
        }
        else if (sheetmusic.getShowNoteLetters() == MidiOptions.NoteNameMovableDoReMi) {
            String[] fixedDoReMi = {
                    "La", "Li", "Ti", "Do", "Di", "Re", "Ri", "Mi", "Fa", "Fi", "So", "Si"
            };
            int mainscale = sheetmusic.getMainKey().Notescale();
            int diff = NoteScale.C - mainscale;
            notenumber += diff;
            if (notenumber < 0) {
                notenumber += 12;
            }
            int notescale = NoteScale.FromNumber(notenumber);
            return fixedDoReMi[notescale];
        }
        else if (sheetmusic.getShowNoteLetters() == MidiOptions.NoteNameFixedNumber) {
            String[] num = {
                    "10", "11", "12", "1", "2", "3", "4", "5", "6", "7", "8", "9"
            };
            int notescale = NoteScale.FromNumber(notenumber);
            return num[notescale];
        }
        else if (sheetmusic.getShowNoteLetters() == MidiOptions.NoteNameMovableNumber) {
            String[] num = {
                    "10", "11", "12", "1", "2", "3", "4", "5", "6", "7", "8", "9"
            };
            int mainscale = sheetmusic.getMainKey().Notescale();
            int diff = NoteScale.C - mainscale;
            notenumber += diff;
            if (notenumber < 0) {
                notenumber += 12;
            }
            int notescale = NoteScale.FromNumber(notenumber);
            return num[notescale];
        }
        else {
            return "";
        }
    }


    /** Get the letter (A, A#, Bb) representing this note */
    private String Letter(int notenumber, WhiteNote whitenote) {
        int notescale = NoteScale.FromNumber(notenumber);
        switch(notescale) {
            case NoteScale.A: return "A";
            case NoteScale.B: return "B";
            case NoteScale.C: return "C";
            case NoteScale.D: return "D";
            case NoteScale.E: return "E";
            case NoteScale.F: return "F";
            case NoteScale.G: return "G";
            case NoteScale.Asharp:
                if (whitenote.getLetter() == WhiteNote.A)
                    return "A#";
                else
                    return "Bb";
            case NoteScale.Csharp:
                if (whitenote.getLetter() == WhiteNote.C)
                    return "C#";
                else
                    return "Db";
            case NoteScale.Dsharp:
                if (whitenote.getLetter() == WhiteNote.D)
                    return "D#";
                else
                    return "Eb";
            case NoteScale.Fsharp:
                if (whitenote.getLetter() == WhiteNote.F)
                    return "F#";
                else
                    return "Gb";
            case NoteScale.Gsharp:
                if (whitenote.getLetter() == WhiteNote.G)
                    return "G#";
                else
                    return "Ab";
            default:
                return "";
        }
    }

    /** Draw the Chord Symbol:
     * - Draw the accidental symbols.
     * - Draw the black circle notes.
     * - Draw the stems.
     *   @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop) {
        paint.setStyle(Paint.Style.STROKE);

        /* Align the chord to the right */
        canvas.translate(getWidth() - getMinWidth(), 0);

        /* Draw the accidentals. */
        WhiteNote topstaff = WhiteNote.Top(clef);
        int xpos = DrawAccid(canvas, paint, ytop);
        DrawAccent(canvas,paint,ytop,xpos);

        DrawRoll(canvas,paint,ytop,xpos);

        DrawFlam(canvas,paint,ytop,xpos);



        /* Draw the notes */
        canvas.translate(xpos, 0);
        DrawNotes(canvas, paint, ytop, topstaff);


        if (sheetmusic != null && sheetmusic.getShowNoteLetters() != 0) {
            DrawNoteLetters(canvas, paint, ytop, topstaff);
        }

        /* Draw the stems */
        if (stem1 != null)
            stem1.Draw(canvas, paint, ytop, topstaff);
        if (stem2 != null)
            stem2.Draw(canvas, paint, ytop, topstaff);

        canvas.translate(-xpos, 0);
        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    /* Draw the accidental symbols.  If two symbols overlap (if they
     * are less than 6 notes apart), we cannot draw the symbol directly
     * above the previous one.  Instead, we must shift it to the right.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     * @return The x pixel width used by all the accidentals.
     */
    public int DrawAccid(Canvas canvas, Paint paint, int ytop) {
        int xpos = 0;

        AccidSymbol prev = null;
        for (AccidSymbol symbol : accidsymbols) {
            if (prev != null && symbol.getNote().Dist(prev.getNote()) < 6) {
                xpos += symbol.getWidth();
            }
            canvas.translate(xpos, 0);
            symbol.Draw(canvas, paint, ytop);
            canvas.translate(-xpos, 0);
            prev = symbol;
        }
        if (prev != null) {
            xpos += prev.getWidth();
        }
        return xpos;
    }


    public void DrawAccent(Canvas canvas, Paint paint, int ytop, int xpos) {

        AccentSymbol prev = null;
        for (AccentSymbol accentsymbol : accentSymbols) {
            if (prev != null && accentsymbol.getNote().Dist(prev.getNote()) < 6) {
                xpos += accentsymbol.getWidth();
            }
            canvas.translate(xpos, 0);
            accentsymbol.Draw(canvas, paint, ytop);
            canvas.translate(-xpos, 0);
            prev = accentsymbol;
        }
        if (prev != null) {
            xpos += prev.getWidth();
        }
    }


    public void DrawRoll(Canvas canvas, Paint paint, int ytop, int xpos) {

        RollSymbol prev = null;
        for (RollSymbol rollsymbol : rollSymbols) {
            if (prev != null && rollsymbol.getNote().Dist(prev.getNote()) < 6) {
                xpos += rollsymbol.getWidth();
            }
            canvas.translate(xpos, 0);
            rollsymbol.Draw(canvas, paint, ytop);
            canvas.translate(-xpos, 0);
            prev = rollsymbol;
        }
    }

    public void DrawFlam(Canvas canvas, Paint paint, int ytop, int xpos) {

        FlamSymbol prev = null;
        for (FlamSymbol flamsymbol : flamSymbols) {
            if (prev != null && flamsymbol.getNote().Dist(prev.getNote()) < 6) {
                xpos += flamsymbol.getWidth();
            }
            canvas.translate(xpos, 0);
            flamsymbol.Draw(canvas, paint, ytop);
            canvas.translate(-xpos, 0);
            prev = flamsymbol;
        }
    }

    /** Draw the black circle notes.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     * @param topstaff The white note of the top of the staff.
     */
    public void DrawNotes(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        paint.setStrokeWidth(1);
        for (NoteData note : notedata) {
            /* Get the x,y position to draw the note */
            int ynote = ytop + topstaff.Dist(note.whitenote) *
                    SheetMusic.NoteHeight/2;

            int xnote = SheetMusic.LineSpace/4;
            if (!note.leftside)
                xnote += SheetMusic.NoteWidth;

            /* Draw rotated ellipse.  You must first translate (0,0)
             * to the center of the ellipse.
             */
            canvas.translate(xnote + SheetMusic.NoteWidth/2 + 1,
                    ynote - SheetMusic.LineWidth + SheetMusic.NoteHeight/2);
            canvas.rotate(-45);

            if (sheetmusic != null) {
                paint.setColor( sheetmusic.NoteColor(note.number) );
            }
            else {
                paint.setColor(Color.BLACK);
            }

            if (note.duration == NoteDuration.Whole ||
                    note.duration == NoteDuration.Half ||
                    note.duration == NoteDuration.DottedHalf) {

                RectF rect = new RectF(-SheetMusic.NoteWidth/2, -SheetMusic.NoteHeight/2,
                        -SheetMusic.NoteWidth/2 + SheetMusic.NoteWidth,
                        -SheetMusic.NoteHeight/2 + SheetMusic.NoteHeight-1);
                canvas.drawOval(rect, paint);
                rect = new RectF(-SheetMusic.NoteWidth/2, -SheetMusic.NoteHeight/2 + 1,
                        -SheetMusic.NoteWidth/2 +  SheetMusic.NoteWidth,
                        -SheetMusic.NoteHeight/2 + 1 + SheetMusic.NoteHeight-2);
                canvas.drawOval(rect, paint);
                rect = new RectF(-SheetMusic.NoteWidth/2, -SheetMusic.NoteHeight/2 + 1,
                        -SheetMusic.NoteWidth/2 + SheetMusic.NoteWidth,
                        -SheetMusic.NoteHeight/2 + 1 + SheetMusic.NoteHeight-3);
                canvas.drawOval(rect, paint);

            }
            else {
                paint.setStyle(Paint.Style.FILL);
                RectF rect = new RectF(-SheetMusic.NoteWidth/2, -SheetMusic.NoteHeight/2,
                        -SheetMusic.NoteWidth/2 + SheetMusic.NoteWidth,
                        -SheetMusic.NoteHeight/2 + SheetMusic.NoteHeight-1);
                canvas.drawOval(rect, paint);
                paint.setStyle(Paint.Style.STROKE);
            }

            paint.setColor(Color.BLACK);

            canvas.rotate(45);
            canvas.translate(- (xnote + SheetMusic.NoteWidth/2 + 1),
                    - (ynote - SheetMusic.LineWidth + SheetMusic.NoteHeight/2));

            /* Draw a dot if this is a dotted duration. */
            if (note.duration == NoteDuration.DottedHalf ||
                    note.duration == NoteDuration.DottedQuarter ||
                    note.duration == NoteDuration.DottedEighth) {

                RectF rect = new RectF(xnote + SheetMusic.NoteWidth + SheetMusic.LineSpace/3,
                        ynote + SheetMusic.LineSpace/3,
                        xnote + SheetMusic.NoteWidth + SheetMusic.LineSpace/3 + 4,
                        ynote + SheetMusic.LineSpace/3 + 4);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawOval(rect, paint);
                paint.setStyle(Paint.Style.STROKE);
            }

            /* Draw horizontal lines if note is above/below the staff */
            WhiteNote top = topstaff.Add(1);
            int dist = note.whitenote.Dist(top);
            int y = ytop - SheetMusic.LineWidth;

            if (dist >= 2) {
                for (int i = 2; i <= dist; i += 2) {
                    y -= SheetMusic.NoteHeight;
                    canvas.drawLine(xnote - SheetMusic.LineSpace/4, y,
                            xnote + SheetMusic.NoteWidth + SheetMusic.LineSpace/4,
                            y, paint);
                }
            }

            WhiteNote bottom = top.Add(-8);
            y = ytop + (SheetMusic.LineSpace + SheetMusic.LineWidth) * 4 - 1;
            dist = bottom.Dist(note.whitenote);
            if (dist >= 2) {
                for (int i = 2; i <= dist; i+= 2) {
                    y += SheetMusic.NoteHeight;
                    canvas.drawLine(xnote - SheetMusic.LineSpace/4, y,
                            xnote + SheetMusic.NoteWidth + SheetMusic.LineSpace/4,
                            y, paint);
                }
            }
            /* End drawing horizontal lines */

        }
    }

    /** Draw the note letters (A, A#, Bb, etc) next to the note circles.
     * @param ytop The y location (in pixels) where the top of the staff starts.
     * @param topstaff The white note of the top of the staff.
     */
    public void DrawNoteLetters(Canvas canvas, Paint paint, int ytop, WhiteNote topstaff) {
        boolean overlap = NotesOverlap(notedata, 0, notedata.length);
        paint.setStrokeWidth(1);

        for (NoteData note : notedata) {
            if (!note.leftside) {
                // There's not enough pixel room to show the letter
                continue;
            }

            // Get the x,y position to draw the note
            int ynote = ytop + topstaff.Dist(note.whitenote) *
                    SheetMusic.NoteHeight/2;

            // Draw the letter to the right side of the note
            int xnote = SheetMusic.NoteWidth + SheetMusic.NoteWidth/2;

            if (note.duration == NoteDuration.DottedHalf ||
                    note.duration == NoteDuration.DottedQuarter ||
                    note.duration == NoteDuration.DottedEighth || overlap) {

                xnote += SheetMusic.NoteWidth/2;
            }
            canvas.drawText(NoteName(note.number, note.whitenote),
                    xnote,
                    ynote + SheetMusic.NoteHeight/2, paint);
        }
    }


    /** Return true if the chords can be connected, where their stems are
     * joined by a horizontal beam. In order to create the beam:
     *
     * - The chords must be in the same measure.
     * - The chord stems should not be a dotted duration.
     * - The chord stems must be the same duration, with one exception
     *   (Dotted Eighth to Sixteenth).
     * - The stems must all point in the same direction (up or down).
     * - The chord cannot already be part of a beam.
     *
     * - 6-chord beams must be 8th notes in 3/4, 6/8, or 6/4 time
     * - 3-chord beams must be either triplets, or 8th notes (12/8 time signature)
     * - 4-chord beams are ok for 2/2, 2/4 or 4/4 time, any duration
     * - 4-chord beams are ok for other times if the duration is 16th
     * - 2-chord beams are ok for any duration
     *
     * If startQuarter is true, the first note should start on a quarter note
     * (only applies to 2-chord beams).
     */
    public static
    boolean CanCreateBeam(ChordSymbol[] chords, TimeSignature time, boolean startQuarter) {
        int numChords = chords.length;

        Stem firstStem = chords[0].getStem();
        Stem lastStem = chords[chords.length-1].getStem();
        if (firstStem == null || lastStem == null) {
            return false;
        }
        int measure = chords[0].getStartTime() / time.getMeasure();
        NoteDuration dur = firstStem.getDuration();
        NoteDuration durLast = lastStem.getDuration();

        boolean dotted8_to_16 = false;
        if (chords.length == 2 && dur == NoteDuration.DottedEighth &&
                durLast == NoteDuration.Sixteenth) {
            dotted8_to_16 = true;
        }

        //note combos in X/4 time sig
        if (time.getDenominator() == 4) {
            //2 16th notes followed by an 8th note
            boolean two16th_to_8th = false;
            if (chords.length == 3) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) == 0) {
                    Stem middleStem = chords[1].getStem();
                    NoteDuration durMiddle = middleStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && durMiddle == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Eighth) {
                        two16th_to_8th = true;
                        chords[0].getStem().setEnd_two16th_one8th(true);
                        return two16th_to_8th;
                    }
                }
            }

            //an 8th note followed by 2 16th notes
            boolean eighth_to_two16th = false;
            if (chords.length == 3) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) == 0) {
                    Stem middleStem = chords[1].getStem();
                    NoteDuration durMiddle = middleStem.getDuration();
                    if (dur == NoteDuration.Eighth && durMiddle == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Sixteenth) {
                        eighth_to_two16th = true;
                        chords[0].getStem().setEnd_one8th_two16th(true);
                        return eighth_to_two16th;
                    }
                }
            }


            //a 16th note followed by an 8th note followed by a 16th note
            boolean sixteenth_to8th_to_16th = false;
            if (chords.length == 3) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) == 0) {
                    Stem middleStem = chords[1].getStem();
                    NoteDuration durMiddle = middleStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && durMiddle == NoteDuration.Eighth
                            && durLast == NoteDuration.Sixteenth) {
                        sixteenth_to8th_to_16th = true;
                        chords[0].getStem().setEnd_16th_8th_16th(true);
                        return sixteenth_to8th_to_16th;
                    }
                }
            }

            // triplets
            boolean triplets = false;
            if (chords.length == 3) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) == 0) {
                    Stem middleStem = chords[1].getStem();
                    NoteDuration durMiddle = middleStem.getDuration();
                    if (dur == NoteDuration.Triplet && durMiddle == NoteDuration.Triplet
                            && durLast == NoteDuration.Triplet) {
                        triplets = true;
                        chords[0].getStem().setEnd_triplet(true);
                        return triplets;
                    }
                }
            }

            // sixteenth triplets
            boolean sixteenthTriplets = false;
            if (chords.length == 6) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) == 0) {
                    Stem middleStem = chords[1].getStem();
                    NoteDuration durMiddle = middleStem.getDuration();
                    if (dur == NoteDuration.SixteenthTriplet && durMiddle == NoteDuration.SixteenthTriplet
                            && durLast == NoteDuration.SixteenthTriplet) {
                        sixteenthTriplets = true;
                        chords[0].getStem().setEnd_16thtriplet(true);
                        return sixteenthTriplets;
                    }
                }
            }
        } // end time sig denom 4 if

        //note combos in X/8 time sig
        else if (time.getDenominator() == 8) {
            int startTime = measure*time.getQuarter()*3; //starttime in pulses of the current measure, used to check
                                                         //whether note groups should be connected with horizontal beam
                                                         // default is 6/8 time
            if (time.getNumerator() == 12) {
                startTime = measure*time.getQuarter()*6;
            }
            else if (time.getNumerator() == 3) {
                startTime = measure*time.getQuarter() * 3 / 2;
            }
            int beat = time.getQuarter();

            //12/8 check (if note group starts on this beat, then connect the note combos with a horizontal beam)
            boolean noteGroupStartOn4thBeat = chords[0].getStartTime()== startTime + beat * 3;

            if (numChords == 3) {
                boolean threeNoteGroup_12_8_Tail = chords[1].getStartTime()== startTime + beat * 5;
                //if the 1st note is on the 1st count, draw the beam or if the 2nd note is on the third count
                if ( (chords[0].getStartTime()== startTime) || (chords[1].getStartTime()  == startTime + beat*2 || noteGroupStartOn4thBeat || threeNoteGroup_12_8_Tail) ) {
                    int totalCount = chords[2].getEndTime() - chords[0].getStartTime();
                    if (totalCount == beat*3/2 && firstStem.getDuration() == NoteDuration.Eighth && durLast == NoteDuration.Eighth) { // 3 8th notes in a row
                        return true;
                    }
                }
                return false;
            }
            if (numChords == 6) { //6 16th notes in a row OR 6 8th notes in a row
                boolean sixNoteGroupCheck = chords[0].getStartTime() == startTime;
                if (!sixNoteGroupCheck) sixNoteGroupCheck = chords[0].getStartTime() == startTime + beat * 3 /2;
                if (!sixNoteGroupCheck) sixNoteGroupCheck = noteGroupStartOn4thBeat;
                if (!sixNoteGroupCheck) sixNoteGroupCheck = chords[0].getStartTime() == startTime + beat * 9 /2;

                if ( sixNoteGroupCheck ) {
                    int totalCount = chords[5].getEndTime() - chords[0].getStartTime();
                    if (totalCount == beat * 3 / 2) { //6 16th notes in a row
                        return true;
                    }
                    else if (totalCount == beat*3) {
                        Stem Stem2 = chords[1].getStem();
                        Stem Stem3 = chords[2].getStem();
                        Stem Stem4 = chords[3].getStem();
                        Stem Stem5 = chords[4].getStem();
                        if (firstStem.getDuration() == NoteDuration.Eighth &&
                                Stem2.getDuration() == NoteDuration.Eighth &&
                                Stem3.getDuration() == NoteDuration.Eighth &&
                                Stem4.getDuration() == NoteDuration.Eighth &&
                                Stem5.getDuration() == NoteDuration.Eighth &&
                                lastStem.getDuration() == NoteDuration.Eighth) {
                            return true;
                        }
                    }
                }
                return false;
            }

            boolean eighth_2_16th_8th = false;
            boolean two_16th_2_8th = false;
            boolean two_8th_2_16th = false;
            boolean sixteenth_8th_16th_8th = false;
            boolean eighth_16th_8th_16th = false;
            boolean sixteenth_2_8th_16th = false;
            if (numChords == 4) {
                boolean fourNoteGroup_12_8_tailCombo = chords[0].getStartTime() == startTime + beat * 9 / 2 ;

                //8th note followed by 2 16th followed by 8th
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    if (dur == NoteDuration.Eighth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Sixteenth && durLast == NoteDuration.Eighth) {
                        eighth_2_16th_8th = true;
                        chords[0].getStem().setEnd_8th_2_16th_8th(true);
                        return eighth_2_16th_8th;
                    }
                }
                //2 16th notes followed by 2 8th notes
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Eighth && durLast == NoteDuration.Eighth) {
                        two_16th_2_8th = true;
                        chords[0].getStem().setEnd_2_16th_2_8th(true);
                        return two_16th_2_8th;
                    }
                }
                //2 8th notes followed by 2 16th notes
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem secondStem = chords[1].getStem();
                    NoteDuration dur2nd = secondStem.getDuration();
                    Stem thirdStem = chords[2].getStem();
                    NoteDuration dur3nd = thirdStem.getDuration();
                    if (dur == NoteDuration.Eighth && dur2nd == NoteDuration.Eighth
                            && dur3nd == NoteDuration.Sixteenth && durLast == NoteDuration.Sixteenth) {
                        two_8th_2_16th = true;
                        chords[0].getStem().setEnd_2_8th_2_16th(true);
                        return two_8th_2_16th;
                    }
                }

                //16th followed by 8th followed by 16th followed by 8th
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem secondStem = chords[1].getStem();
                    NoteDuration dur2nd = secondStem.getDuration();
                    Stem thirdStem = chords[2].getStem();
                    NoteDuration dur3nd = thirdStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Eighth
                            && dur3nd == NoteDuration.Sixteenth && durLast == NoteDuration.Eighth) {
                        sixteenth_8th_16th_8th = true;
                        chords[0].getStem().setEnd_16th_8th_16th_8th(true);
                        return sixteenth_8th_16th_8th;
                    }
                }

                //16th followed by 2 8th followed by 16th
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem secondStem = chords[1].getStem();
                    NoteDuration dur2nd = secondStem.getDuration();
                    Stem thirdStem = chords[2].getStem();
                    NoteDuration dur3nd = thirdStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Eighth
                            && dur3nd == NoteDuration.Eighth && durLast == NoteDuration.Sixteenth) {
                        sixteenth_2_8th_16th = true;
                        chords[0].getStem().setEnd_16th_2_8th_16th(true);
                        return sixteenth_2_8th_16th;
                    }
                }

                //8th followed by 16th followed by 8th followed by 16th
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fourNoteGroup_12_8_tailCombo) {
                    Stem secondStem = chords[1].getStem();
                    NoteDuration dur2nd = secondStem.getDuration();
                    Stem thirdStem = chords[2].getStem();
                    NoteDuration dur3nd = thirdStem.getDuration();
                    if (dur == NoteDuration.Eighth && dur2nd == NoteDuration.Sixteenth
                            && dur3nd == NoteDuration.Eighth && durLast == NoteDuration.Sixteenth) {
                        eighth_16th_8th_16th = true;
                        chords[0].getStem().setEnd_8th_16th_8th_16th(true);
                        return eighth_16th_8th_16th;
                    }
                }
                //prevent other note sequences from connecting
                return false;

            } //end numChords == 4 if

            if (numChords == 5) {
                boolean fiveNoteGroup_12_8_tailCombo = chords[0].getStartTime() == startTime + beat * 9 / 2;

                //1 8th note followed by 4 16th
                boolean eighth_4_16th = false;
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fiveNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    tempStem = chords[3].getStem();
                    NoteDuration dur4th = tempStem.getDuration();

                    if (dur == NoteDuration.Eighth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Sixteenth && dur4th == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Sixteenth) {
                        eighth_4_16th = true;
                        chords[0].getStem().setEnd_8th_4_16th(true);
                        return eighth_4_16th;
                    }
                }

                //4 16th notes followed by 1 8th
                boolean four_16th_1_8th = false;
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fiveNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    tempStem = chords[3].getStem();
                    NoteDuration dur4th = tempStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Sixteenth && dur4th == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Eighth) {
                        four_16th_1_8th = true;
                        chords[0].getStem().setEnd_4_16th_8th(true);
                        return four_16th_1_8th;
                    }
                }

                //2 16th notes followed by 1 8th followed by 2 16th
                boolean two_16th_1_8th_2_16h = false;
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fiveNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    tempStem = chords[3].getStem();
                    NoteDuration dur4th = tempStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Eighth && dur4th == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Sixteenth) {
                        two_16th_1_8th_2_16h = true;
                        chords[0].getStem().setEnd_2_16th_8th_2_16th(true);
                        return two_16th_1_8th_2_16h;
                    }
                }

                //3 16th notes followed by 1 8th followed by 16th
                boolean three_16th_8th_16th = false;
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fiveNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    tempStem = chords[3].getStem();
                    NoteDuration dur4th = tempStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Sixteenth
                            && dur3rd == NoteDuration.Sixteenth && dur4th == NoteDuration.Eighth
                            && durLast == NoteDuration.Sixteenth) {
                        three_16th_8th_16th = true;
                        chords[0].getStem().setEnd_3_16th_8th_16th(true);
                        return three_16th_8th_16th;
                    }
                }

                //16th note followed by 1 8th followed by 3 16th
                boolean one_16th_8th_3_16th = false;
                if (chords[0].getStartTime()== startTime || noteGroupStartOn4thBeat ||
                        chords[0].getStartTime() == startTime + beat * 3 / 2 || fiveNoteGroup_12_8_tailCombo) {
                    Stem tempStem = chords[1].getStem();
                    NoteDuration dur2nd = tempStem.getDuration();
                    tempStem = chords[2].getStem();
                    NoteDuration dur3rd = tempStem.getDuration();
                    tempStem = chords[3].getStem();
                    NoteDuration dur4th = tempStem.getDuration();
                    if (dur == NoteDuration.Sixteenth && dur2nd == NoteDuration.Eighth
                            && dur3rd == NoteDuration.Sixteenth && dur4th == NoteDuration.Sixteenth
                            && durLast == NoteDuration.Sixteenth) {
                        one_16th_8th_3_16th = true;
                        chords[0].getStem().setEnd_16th_8th_3_16th(true);
                        return one_16th_8th_3_16th;
                    }
                }

                //prevent other note sequences from connecting
                return false;

            } //end 5 note chord sequences
        } // end time sig denom 8 else if


        if (dur == NoteDuration.Whole || dur == NoteDuration.Half ||
                dur == NoteDuration.DottedHalf || dur == NoteDuration.Quarter ||
                dur == NoteDuration.DottedQuarter ||
                (dur == NoteDuration.DottedEighth && !dotted8_to_16)) {

            return false;
        }

        if (numChords == 6) {
            if (dur != NoteDuration.Eighth) {
                return false;
            }
            boolean correctTime =
                    ((time.getNumerator() == 3 && time.getDenominator() == 4) ||
                            (time.getNumerator() == 6 && time.getDenominator() == 8) ||
                            (time.getNumerator() == 6 && time.getDenominator() == 4) );
            if (!correctTime) {
                return false;
            }

            if (time.getNumerator() == 6 && time.getDenominator() == 4) {
                /* first chord must start at 1st or 4th quarter note */
                int beat = time.getQuarter() * 3;
                if ((chords[0].getStartTime() % beat) > time.getQuarter()/6) {
                    return false;
                }
            }
        }
        else if (numChords == 4) {
            if (time.getNumerator() == 3 && time.getDenominator() == 8) {
                return false;
            }
            boolean correctTime =
                    (time.getNumerator() == 2 || time.getNumerator() == 4 || ((time.getNumerator() == 6 || time.getNumerator() == 3) && time.getDenominator() == 4) || time.getNumerator() == 8);
            if (!correctTime && dur != NoteDuration.Sixteenth) {
                return false;
            }

            /* chord must start on quarter note */
            int beat = time.getQuarter();
            if (dur == NoteDuration.Eighth) {
                /* 8th note chord must start on 1st or 3rd quarter beat */
                beat = time.getQuarter() * 2;
            }
            else if (dur == NoteDuration.ThirtySecond) {
                /* 32nd note must start on an 8th beat */
                beat = time.getQuarter() / 2;
            }

            if ((chords[0].getStartTime() % beat) > time.getQuarter()/6) {
                return false;
            }
        }
        else if (numChords == 3) {
            boolean valid = (dur == NoteDuration.Triplet) ||
                    (dur == NoteDuration.Eighth &&
                            time.getNumerator() == 12 && time.getDenominator() == 8);
            if (!valid) {
                return false;
            }

            /* chord must start on quarter note */
            int beat = time.getQuarter();
            if (time.getNumerator() == 12 && time.getDenominator() == 8) {
                /* In 12/8 time, chord must start on 3*8th beat */
                beat = time.getQuarter()/2 * 3;
            }
            if ((chords[0].getStartTime() % beat) > time.getQuarter()/6) {
                return false;
            }
        }
        else if (numChords == 2) {
            if (startQuarter) {
                int beat = time.getQuarter();
                if ((chords[0].getStartTime() % beat) > time.getQuarter()/6) {
                    return false;
                }
            }
        }

        for (ChordSymbol chord : chords) {
            if ((chord.getStartTime() / time.getMeasure()) != measure)
                return false;
            if (chord.getStem() == null)
                return false;
            if (chord.getStem().getDuration() != dur && !dotted8_to_16)
                return false;
            if (chord.getStem().IsBeam())
                return false;
        }

        /* Check that all stems can point in same direction */
        boolean hasTwoStems = false;
        int direction = Stem.Up;
        for (ChordSymbol chord : chords) {
            if (chord.getHasTwoStems()) {
                if (hasTwoStems && chord.getStem().getDirection() != direction) {
                    return false;
                }
                hasTwoStems = true;
                direction = chord.getStem().getDirection();
            }
        }

        /* Get the final stem direction */
        if (!hasTwoStems) {
            WhiteNote note1;
            WhiteNote note2;
            note1 = (firstStem.getDirection() == Stem.Up ? firstStem.getTop() : firstStem.getBottom());
            note2 = (lastStem.getDirection() == Stem.Up ? lastStem.getTop() : lastStem.getBottom());
            direction = StemDirection(note1, note2, chords[0].getClef());
        }

        /* If the notes are too far apart, don't use a beam */
        if (direction == Stem.Up) {
            if (Math.abs(firstStem.getTop().Dist(lastStem.getTop())) >= 11) {
                return false;
            }
        }
        else {
            if (Math.abs(firstStem.getBottom().Dist(lastStem.getBottom())) >= 11) {
                return false;
            }
        }
        return true;
    }


    /** Connect the chords using a horizontal beam.
     *
     * spacing is the horizontal distance (in pixels) between the right side
     * of the first chord, and the right side of the last chord.
     *
     * To make the beam:
     * - Change the stem directions for each chord, so they match.
     * - In the first chord, pass the stem location of the last chord, and
     *   the horizontal spacing to that last stem.
     * - Mark all chords (except the first) as "receiver" pairs, so that
     *   they don't draw a curvy stem.
     */
    public static
    void CreateBeam(ChordSymbol[] chords, int spacing) {
        Stem firstStem = chords[0].getStem();
        Stem lastStem = chords[chords.length-1].getStem();

        /* Calculate the new stem direction */
        int newdirection = -1;
        for (ChordSymbol chord : chords) {
            if (chord.getHasTwoStems()) {
                newdirection = chord.getStem().getDirection();
                break;
            }
        }

        if (newdirection == -1) {
            WhiteNote note1;
            WhiteNote note2;
            note1 = (firstStem.getDirection() == Stem.Up ? firstStem.getTop() : firstStem.getBottom());
            note2 = (lastStem.getDirection() == Stem.Up ? lastStem.getTop() : lastStem.getBottom());
            newdirection = StemDirection(note1, note2, chords[0].getClef());
        }
        for (ChordSymbol chord : chords) {
            chord.getStem().setDirection(newdirection);
        }

        if (chords.length == 2) {
            BringStemsCloser(chords);
        }
        else {
            LineUpStemEnds(chords);
        }

        firstStem.SetPair(lastStem, spacing);
        chords[0].getStem().setReceiver(false);  //explicitly set the first note of the sequence to not be a "receiver" note
                                                 // and that the beam will start here (jin)
        for (int i = 1; i < chords.length; i++) {
            chords[i].getStem().setReceiver(true);
        }
    }

    /** We're connecting the stems of two chords using a horizontal beam.
     *  Adjust the vertical endpoint of the stems, so that they're closer
     *  together.  For a dotted 8th to 16th beam, increase the stem of the
     *  dotted eighth, so that it's as long as a 16th stem.
     */
    static void
    BringStemsCloser(ChordSymbol[] chords) {
        Stem firstStem = chords[0].getStem();
        Stem lastStem = chords[1].getStem();

        /* If we're connecting a dotted 8th to a 16th, increase
         * the stem end of the dotted eighth.
         */
        if (firstStem.getDuration() == NoteDuration.DottedEighth &&
                lastStem.getDuration() == NoteDuration.Sixteenth) {
            if (firstStem.getDirection() == Stem.Up) {
                firstStem.setEnd(firstStem.getEnd().Add(2));
            }
            else {
                firstStem.setEnd(firstStem.getEnd().Add(-2));
            }
        }

        /* Bring the stem ends closer together */
        int distance = Math.abs(firstStem.getEnd().Dist(lastStem.getEnd()));
        if (firstStem.getDirection() == Stem.Up) {
            if (WhiteNote.Max(firstStem.getEnd(), lastStem.getEnd()) == firstStem.getEnd())
                lastStem.setEnd(lastStem.getEnd().Add(distance/2));
            else
                firstStem.setEnd(firstStem.getEnd().Add(distance/2));
        }
        else {
            if (WhiteNote.Min(firstStem.getEnd(), lastStem.getEnd()) == firstStem.getEnd())
                lastStem.setEnd(lastStem.getEnd().Add(-distance/2));
            else
                firstStem.setEnd(firstStem.getEnd().Add(-distance/2));
        }
    }

    /** We're connecting the stems of three or more chords using a horizontal beam.
     *  Adjust the vertical endpoint of the stems, so that the middle chord stems
     *  are vertically in between the first and last stem.
     */
    static void
    LineUpStemEnds(ChordSymbol[] chords) {
        Stem firstStem = chords[0].getStem();
        Stem lastStem = chords[chords.length-1].getStem();
        Stem middleStem = chords[1].getStem();

        if (firstStem.getDirection() == Stem.Up) {
            /* Find the highest stem. The beam will either:
             * - Slant downwards (first stem is highest)
             * - Slant upwards (last stem is highest)
             * - Be straight (middle stem is highest)
             */
            WhiteNote top = firstStem.getEnd();
            for (ChordSymbol chord : chords) {
                top = WhiteNote.Max(top, chord.getStem().getEnd());
            }
            if (top == firstStem.getEnd() && top.Dist(lastStem.getEnd()) >= 2) {
                firstStem.setEnd(top);
                middleStem.setEnd(top.Add(-1));
                lastStem.setEnd(top.Add(-2));
            }
            else if (top == lastStem.getEnd() && top.Dist(firstStem.getEnd()) >= 2) {
                firstStem.setEnd(top.Add(-2));
                middleStem.setEnd(top.Add(-1));
                lastStem.setEnd(top);
            }
            else {
                firstStem.setEnd(top);
                middleStem.setEnd(top);
                lastStem.setEnd(top);
            }
        }
        else {
            /* Find the bottommost stem. The beam will either:
             * - Slant upwards (first stem is lowest)
             * - Slant downwards (last stem is lowest)
             * - Be straight (middle stem is highest)
             */
            WhiteNote bottom = firstStem.getEnd();
            for (ChordSymbol chord : chords) {
                bottom = WhiteNote.Min(bottom, chord.getStem().getEnd());
            }

            if (bottom == firstStem.getEnd() && lastStem.getEnd().Dist(bottom) >= 2) {
                middleStem.setEnd(bottom.Add(1));
                lastStem.setEnd(bottom.Add(2));
            }
            else if (bottom == lastStem.getEnd() && firstStem.getEnd().Dist(bottom) >= 2) {
                middleStem.setEnd(bottom.Add(1));
                firstStem.setEnd(bottom.Add(2));
            }
            else {
                firstStem.setEnd(bottom);
                middleStem.setEnd(bottom);
                lastStem.setEnd(bottom);
            }
        }

        /* All middle stems have the same end */
        for (int i = 1; i < chords.length-1; i++) {
            Stem stem = chords[i].getStem();
            stem.setEnd(middleStem.getEnd());
        }
    }


    @Override
    public String toString() {
        String result = String.format("ChordSymbol clef=%1$s start=%2$s end=%3$s width=%4$s hastwostems=%5$s ",
                clef, getStartTime(), getEndTime(), getWidth(), hastwostems);
        for (AccidSymbol symbol : accidsymbols) {
            result += symbol.toString() + " ";
        }
        for (NoteData note : notedata) {
            result += String.format("Note whitenote=%1$s duration=%2$s leftside=%3$s ",
                    note.whitenote, note.duration, note.leftside);
        }
        if (stem1 != null) {
            result += stem1.toString() + " ";
        }
        if (stem2 != null) {
            result += stem2.toString() + " ";
        }
        return result;
    }

}


