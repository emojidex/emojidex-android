package com.emojidex.emojidexandroid.imageloader;

import android.graphics.Bitmap;

/**
 * Image parameter.
 */
public class ImageParam
{
    boolean oneShot;
    boolean skipFirst;
    Frame[] frames;

    public static class Frame
    {
        Bitmap bitmap;
        int duration;

        public Bitmap getBitmap()
        {
            return bitmap;
        }

        public int getDuration()
        {
            return duration;
        }
    }

    public boolean isOneShot()
    {
        return oneShot;
    }

    public boolean isSkipFirst()
    {
        return skipFirst;
    }

    public Frame[] getFrames()
    {
        return frames;
    }

    public boolean hasAnimation()
    {
        return frames.length > 1;
    }
}
