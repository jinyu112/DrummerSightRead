package com.tidisventures.drummersightread;

/** @class LyricSymbol
 *  A lyric contains the lyric to display, the start time the lyric occurs at,
 *  the the x-coordinate where it will be displayed.
 */
public class LyricSymbol {
    private int starttime;   /** The start time, in pulses */
    private String text;     /** The lyric text */
    private int x;           /** The x (horizontal) position within the staff */

    public LyricSymbol(int starttime, String text) {
        this.starttime = starttime;
        this.text = text;
    }

    public int getStartTime() { return starttime; }
    public void setStartTime(int value) { starttime = value; }

    public String getText() { return text; }
    public void setText(String value) { text = value; }

    public int getX() { return x; }
    public void setX(int value) { x = value; }

    /* Return the minimum width in pixels needed to display this lyric.
     * This is an estimation, not exact.
     */
    public int getMinWidth() {
        float widthPerChar = 10.0f * 2.0f/3.0f;
        float width = text.length() * widthPerChar;
        if (text.indexOf("i") >= 0) {
            width -= widthPerChar/2.0f;
        }
        if (text.indexOf("j") >= 0) {
            width -= widthPerChar/2.0f;
        }
        if (text.indexOf("l") >= 0) {
            width -= widthPerChar/2.0f;
        }
        return (int)width;
    }

    @Override
    public String toString() {
        return String.format("Lyric start={0} x={1} text={2}",
                starttime, x, text);
    }

}

