package com.emojidex.emojidexandroid;

import android.graphics.Canvas;
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
    private boolean isRunning = false;

    public EmojidexAnimationDrawable()
    {
        super();
    }

    @Override
    public void start()
    {
        isRunning = true;
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void stop()
    {
        calcTime();
        isRunning = false;
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

    @Override
    public boolean isRunning()
    {
        return isRunning;
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
        ++currentIndex;

        if(currentIndex == getNumberOfFrames())
        {
            currentIndex = 0;
            if(isOneShot())
                stop();
        }
    }

    @Override
    public void draw(Canvas canvas)
    {
        calcTime();
        selectDrawable(currentIndex);
        super.draw(canvas);
    }
}
