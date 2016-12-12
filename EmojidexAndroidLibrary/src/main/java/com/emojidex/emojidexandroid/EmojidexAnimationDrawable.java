package com.emojidex.emojidexandroid;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * Created by kou on 16/12/09.
 */

public class EmojidexAnimationDrawable extends AnimationDrawable
{
    private int currentIndex = 0;
    private long timer = 0;
    private long lastTime = 0;

    public EmojidexAnimationDrawable()
    {
        super();
    }

    @Override
    public void start()
    {
        super.start();
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void stop()
    {
        calcTime();
        super.stop();
    }

    public Drawable getCurrentFrame()
    {
        calcTime();
        return getFrame(currentIndex);
    }

    @Override
    public Drawable getCurrent()
    {
        return super.getCurrent();
    }

    private void calcTime()
    {
        if(!isRunning())
            return;

        final long now = System.currentTimeMillis();
        timer += now - lastTime;

        long duration = getDuration(currentIndex);
        while(timer >= duration)
        {
            timer -= duration;
            next();
            duration = getDuration(currentIndex);
        }

        lastTime = now;
    }

    private void next()
    {
        currentIndex = (currentIndex + 1) % getNumberOfFrames();
    }
}
