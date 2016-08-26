package com.leff.midi.util;

import com.leff.midi.event.MidiEvent;

/**
 * An event specifically for MidiProcessor to broadcast metronome ticks so that
 * observers need not rely on time conversions or measure tracking
 */
public class MetronomeTick extends MidiEvent
{
    private int mResolution;
    private com.leff.midi.event.meta.TimeSignature_leff mSignature;

    private int mCurrentMeasure;
    private int mCurrentBeat;

    private double mMetronomeProgress;
    private int mMetronomeFrequency;

    public MetronomeTick(com.leff.midi.event.meta.TimeSignature_leff sig, int resolution)
    {
        super(0, 0);

        mResolution = resolution;

        setTimeSignature(sig);
        mCurrentMeasure = 1;
    }

    public void setTimeSignature(com.leff.midi.event.meta.TimeSignature_leff sig)
    {
        mSignature = sig;
        mCurrentBeat = 0;

        setMetronomeFrequency(sig.getMeter());
    }

    public boolean update(double ticksElapsed)
    {
        mMetronomeProgress += ticksElapsed;

        if(mMetronomeProgress >= mMetronomeFrequency)
        {

            mMetronomeProgress %= mMetronomeFrequency;

            mCurrentBeat = (mCurrentBeat + 1) % mSignature.getNumerator();
            if(mCurrentBeat == 0)
            {
                mCurrentMeasure++;
            }

            return true;
        }
        return false;
    }

    public void setMetronomeFrequency(int meter)
    {
        switch(meter)
        {
            case com.leff.midi.event.meta.TimeSignature_leff.METER_EIGHTH:
                mMetronomeFrequency = mResolution / 2;
                break;
            case com.leff.midi.event.meta.TimeSignature_leff.METER_QUARTER:
                mMetronomeFrequency = mResolution;
                break;
            case com.leff.midi.event.meta.TimeSignature_leff.METER_HALF:
                mMetronomeFrequency = mResolution * 2;
                break;
            case com.leff.midi.event.meta.TimeSignature_leff.METER_WHOLE:
                mMetronomeFrequency = mResolution * 4;
                break;
        }
    }

    public int getBeatNumber()
    {
        return mCurrentBeat + 1;
    }

    public int getMeasure()
    {
        return mCurrentMeasure;
    }

    @Override
    public String toString()
    {
        return "Metronome: " + mCurrentMeasure + "\t" + getBeatNumber();
    }

    @Override
    public int compareTo(MidiEvent o)
    {
        return 0;
    }

    @Override
    protected int getEventSize()
    {
        return 0;
    }

    @Override
    public int getSize()
    {
        return 0;
    }
}
