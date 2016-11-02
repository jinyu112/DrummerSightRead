package com.tidisventures.drummersightread;

import android.graphics.*;


/** @class AccidSymbol
 * An accidental (accid) symbol represents a sharp, flat, or natural
 * accidental that is displayed at a specific position (note and clef).
 */
public class AccentSymbol implements MusicSymbol {
    private Accent accent;          /** The accidental (sharp, flat, natural) */
    private WhiteNote whitenote;  /** The white note where the symbol occurs */
    private Clef clef;            /** Which clef the symbols is in */
    private int width;            /** Width of symbol */
    private NoteDuration dur = NoteDuration.Quarter;

    /**
     * Create a new AccidSymbol with the given accidental, that is
     * displayed at the given note in the given clef.
     */
    public AccentSymbol(Accent accent, WhiteNote note, Clef clef, NoteDuration dur_in) {
        this.accent = accent;
        this.whitenote = note;
        this.clef = clef;
        this.dur = dur_in;
        width = getMinWidth();
    }

    public AccentSymbol(Accent accent, WhiteNote note, Clef clef) {
        this.accent = accent;
        this.whitenote = note;
        this.clef = clef;
        width = getMinWidth();
    }

    /** Return the white note this accidental is displayed at */
    public WhiteNote getNote() { return whitenote; }

    /** Get the time (in pulses) this symbol occurs at.
     * Not used.  Instead, the StartTime of the ChordSymbol containing this
     * AccidSymbol is used.
     */
    public int getStartTime() { return -1; }

    /** Get the minimum width (in pixels) needed to draw this symbol */
    public int getMinWidth() { return 3*SheetMusic.NoteHeight/2; }

    /** Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     */
    public int getWidth() { return width; }
    public void setWidth(int value) { width = value; }

    /** Get the number of pixels this symbol extends above the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getAboveStaff() {
        int dist = WhiteNote.Top(clef).Dist(whitenote) *
                SheetMusic.NoteHeight/2;
            dist -= SheetMusic.NoteHeight;


        if (dist < 0)
            return -dist;
        else
            return 0;
    }

    /** Get the number of pixels this symbol extends below the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff() {
        int dist = WhiteNote.Bottom(clef).Dist(whitenote) *
                SheetMusic.NoteHeight/2 +
                SheetMusic.NoteHeight;
            dist += SheetMusic.NoteHeight;

        if (dist > 0)
            return dist;
        else
            return 0;
    }

    /** Draw the symbol.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    public void Draw(Canvas canvas, Paint paint, int ytop) {
        /* Align the symbol to the right */
        canvas.translate(getWidth() - getMinWidth(), 0);

        /* Store the y-pixel value of the top of the whitenote in ynote. */
        int ynote = ytop + WhiteNote.Top(clef).Dist(whitenote) *
                SheetMusic.NoteHeight/2;

        if (accent == Accent.Marcato)
            DrawMarcato(canvas, paint, ynote);
        else if (accent == Accent.Regular)
            DrawRegularAccent(canvas, paint, ynote);

        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    /** Draw a marcato symbol.
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawMarcato(Canvas canvas, Paint paint, int ynote) {


        int ystart = ynote  - SheetMusic.NoteHeight*3 - 8;

        if (dur == NoteDuration.Sixteenth || dur == NoteDuration.SixteenthTriplet) {
            ystart = ystart - SheetMusic.NoteHeight;
        }
        else if (dur == NoteDuration.ThirtySecond) {
            ystart = ystart - SheetMusic.NoteHeight * 2;
        }
        int yend = ystart - SheetMusic.NoteHeight;
        int x = SheetMusic.NoteHeight / 2;
        paint.setStrokeWidth(2);
        canvas.drawLine(x, ystart, x + 6, yend, paint);
        x += SheetMusic.NoteHeight/2 + 4;
        paint.setStrokeWidth(3);
        canvas.drawLine(x, ystart, x - 6, yend, paint);

        paint.setStrokeWidth(1);
    }

    /** Draw a flat symbol.
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawRegularAccent(Canvas canvas, Paint paint, int ynote) {

        int ystart = ynote - SheetMusic.NoteHeight*3 - 8;
        if (dur == NoteDuration.Sixteenth || dur == NoteDuration.SixteenthTriplet) {
            ystart = ystart - SheetMusic.NoteHeight;
        }
        else if (dur == NoteDuration.ThirtySecond) {
            ystart = ystart - SheetMusic.NoteHeight * 2;
        }
        int yend = ystart - 6;
        int x = SheetMusic.NoteHeight/2 - 2;
        paint.setStrokeWidth(2);
        canvas.drawLine(x, ystart, x + SheetMusic.NoteWidth, yend, paint);
        canvas.drawLine(x, ystart - 6*2, x + SheetMusic.NoteWidth, yend, paint);

        paint.setStrokeWidth(1);
    }


    public String toString() {
        return String.format(
                "AccentSymbol accent={0} whitenote={1} clef={2} width={3}",
                accent, whitenote, clef, width);
    }

}



