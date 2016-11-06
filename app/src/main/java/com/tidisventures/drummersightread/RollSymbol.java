package com.tidisventures.drummersightread;

import android.graphics.*;

public class RollSymbol implements MusicSymbol {
    private Roll roll;          /** The accidental (sharp, flat, natural) */
    private WhiteNote whitenote;  /** The white note where the symbol occurs */
    private Clef clef;            /** Which clef the symbols is in */
    private int width;            /** Width of symbol */
    private NoteDuration dur = NoteDuration.Quarter;

    /**
     * Create a new rollSymbol with the given accidental, that is
     * displayed at the given note in the given clef.
     */
    public RollSymbol(Roll roll, WhiteNote note, Clef clef, NoteDuration dur_in) {
        this.roll = roll;
        this.whitenote = note;
        this.clef = clef;
        this.dur = dur_in;
        width = getMinWidth();
    }

    public RollSymbol(Roll roll, WhiteNote note, Clef clef) {
        this.roll = roll;
        this.whitenote = note;
        this.clef = clef;
        width = getMinWidth();
    }

    /** Return the white note this roll is displayed at */
    public WhiteNote getNote() { return whitenote; }

    /** Get the time (in pulses) this symbol occurs at.
     * Not used.  Instead, the StartTime of the ChordSymbol containing this
     * rollSymbol is used.
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
        if (roll == Roll.Single) {
            DrawSingleRoll(canvas, paint, ynote);
        }
        else if (roll == Roll.Double) {
            DrawDoubleRoll(canvas,paint,ynote);
        }
        else if (roll == Roll.Triple) {
            DrawTripleRoll(canvas, paint, ynote);
        }


        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    /** Draw a single roll symbol.
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawSingleRoll(Canvas canvas, Paint paint, int ynote) {

        int ystart = ynote  - SheetMusic.NoteHeight - SheetMusic.NoteHeight/5;
        int yend = ystart - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        int x = SheetMusic.NoteHeight * 7 / 10 ;
        if (dur == NoteDuration.Whole) x = SheetMusic.NoteHeight / 4 ;
        int xend = x + SheetMusic.NoteHeight*3/2;

        paint.setStrokeWidth(5);
        canvas.drawLine(x, ystart, xend, yend, paint);

    }

    public void DrawDoubleRoll(Canvas canvas, Paint paint, int ynote) {

        int ystart = ynote  - SheetMusic.NoteHeight - SheetMusic.NoteHeight/4;
        int yend = ystart - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        int x = SheetMusic.NoteHeight * 7 / 10 ;
        if (dur == NoteDuration.Whole) x = SheetMusic.NoteHeight / 4 ;
        int xend = x + SheetMusic.NoteHeight*3/2;
        paint.setStrokeWidth(5);
        canvas.drawLine(x, ystart, xend, yend, paint);

        ystart = ystart + SheetMusic.NoteHeight/2;
        yend = ystart  - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        canvas.drawLine(x, ystart, xend, yend, paint);

    }

    public void DrawTripleRoll(Canvas canvas, Paint paint, int ynote) {

        int ystart = ynote  - SheetMusic.NoteHeight - SheetMusic.NoteHeight/5;
        int yend = ystart - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        int x = SheetMusic.NoteHeight * 7 / 10 ;
        if (dur == NoteDuration.Whole) x = SheetMusic.NoteHeight / 4 ;
        int xend = x + SheetMusic.NoteHeight*3/2;
        paint.setStrokeWidth(5);
        canvas.drawLine(x, ystart, xend, yend, paint);

        int ystart0 = ystart;

        ystart = ystart0 + SheetMusic.NoteHeight/2;
        yend = ystart  - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        canvas.drawLine(x, ystart, xend, yend, paint);

        ystart = ystart0 - SheetMusic.NoteHeight/2;
        yend = ystart  - SheetMusic.NoteHeight + SheetMusic.NoteHeight*2/3 - SheetMusic.NoteHeight/5;
        canvas.drawLine(x, ystart, xend, yend, paint);


    }

    public String toString() {
        return String.format(
                "RollSymbol accent={0} whitenote={1} clef={2} width={3}",
                roll, whitenote, clef, width);
    }

}
