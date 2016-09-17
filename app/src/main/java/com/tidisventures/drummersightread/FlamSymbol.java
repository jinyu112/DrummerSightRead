package com.tidisventures.drummersightread;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class FlamSymbol implements MusicSymbol {
    private Flam flam;          /** The accidental (sharp, flat, natural) */
    private WhiteNote whitenote;  /** The white note where the symbol occurs */
    private Clef clef;            /** Which clef the symbols is in */
    private int width;            /** Width of symbol */

    /**
     * Create a new AccidSymbol with the given accidental, that is
     * displayed at the given note in the given clef.
     */
    public FlamSymbol(Flam flam, WhiteNote note, Clef clef) {
        this.flam = flam;
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

        if (flam == Flam.Flam)
            DrawFlam(canvas, paint, ynote);

        canvas.translate(-(getWidth() - getMinWidth()), 0);
    }

    /** Draw a marcato symbol.
     * @param ynote The pixel location of the top of the accidental's note.
     */
    public void DrawFlam(Canvas canvas, Paint paint, int ynote) {


        //draw the oval for the flam note
        ynote = ynote - SheetMusic.NoteHeight;
        int xnote = SheetMusic.NoteHeight/2;
        int flamCoordFactor = 3;
        float flamSizeFactor = (float) 2.25;
        paint.setColor(Color.BLACK);
        canvas.translate(xnote - SheetMusic.NoteWidth/2,
                ynote - SheetMusic.LineWidth + SheetMusic.NoteHeight*3/2);
        canvas.rotate(-45);

        paint.setStyle(Paint.Style.FILL);
        RectF rect = new RectF(-SheetMusic.NoteWidth/flamCoordFactor, -SheetMusic.NoteHeight/flamCoordFactor,
                -SheetMusic.NoteWidth/flamCoordFactor + SheetMusic.NoteWidth/flamSizeFactor,
                -SheetMusic.NoteHeight/flamCoordFactor + SheetMusic.NoteHeight/flamSizeFactor-1);
        canvas.drawOval(rect, paint);
        paint.setStyle(Paint.Style.STROKE);

        canvas.rotate(45);
        canvas.translate(-(xnote - SheetMusic.NoteWidth / 2),
                -(ynote - SheetMusic.LineWidth + SheetMusic.NoteHeight * 3 / 2));

        //draw vertial line
        paint.setStrokeWidth(1);
        canvas.drawLine(-SheetMusic.NoteWidth / (flamCoordFactor * 4),
                ynote + SheetMusic.NoteHeight * 3 / 2, -SheetMusic.NoteWidth / (flamCoordFactor * 4),
                ynote - SheetMusic.NoteHeight / 3, paint);

        //draw curvy part
        Path bezierPath;
        bezierPath = new Path();
        int xstart =-SheetMusic.NoteWidth / (flamCoordFactor * 4);
        int ystem =ynote - SheetMusic.NoteHeight / 3;
        bezierPath.moveTo(xstart, ystem);
        bezierPath.cubicTo(xstart + 1, ystem + 3 * SheetMusic.LineSpace / 2,
                xstart + SheetMusic.LineSpace * (float) .85, ystem + SheetMusic.NoteHeight * (float) .65,
                xstart + SheetMusic.LineSpace * (float) .2, ystem + SheetMusic.NoteHeight * 3 / 2);
        canvas.drawPath(bezierPath, paint);

        //draw slanted line
        xstart = xstart - SheetMusic.NoteWidth/4;
        int xend = xstart + SheetMusic.NoteWidth * 4/5;
        int ystart = ystem + SheetMusic.NoteWidth;
        int yend = ystem + SheetMusic.NoteWidth*3/5;
        paint.setStrokeWidth(1);
        canvas.drawLine(xstart,ystart,xend,yend,paint);



    }


    public String toString() {
        return String.format(
                "FlamSymbol flam={0} whitenote={1} clef={2} width={3}",
                flam, whitenote, clef, width);
    }

}