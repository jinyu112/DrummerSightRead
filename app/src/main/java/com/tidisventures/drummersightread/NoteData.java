package com.tidisventures.drummersightread;

/** @class NoteData
 *  Contains fields for displaying a single note in a chord.
 */
public class NoteData {
    public int number;             /** The Midi note number, used to determine the color */
    public WhiteNote whitenote;    /** The white note location to draw */
    public NoteDuration duration;  /** The duration of the note */
    public boolean leftside;       /** Whether to draw note to the left or right of the stem */
    public Accid accid;            /** Used to create the AccidSymbols for the chord */
    public Accent accent;
}
