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
    private Status status = Status.STOP;

    private boolean skipFirst = false;

    private enum Status
    {
        STOP,
        PLAYING,
        PAUSE
    }

    public EmojidexAnimationDrawable()
    {
        super();
    }

    @Override
    public void start()
    {
        status = Status.PLAYING;
        lastTime = System.currentTimeMillis();
        if(skipFirst && currentIndex == 0)
            currentIndex = 1;
    }

    @Override
    public void stop()
    {
        status = Status.STOP;
        currentIndex = 0;
        timer = 0;
    }

    public void pause()
    {
        calcTime();
        status = Status.PAUSE;
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
        return status == Status.PLAYING;
    }

    public void setSkipFirst(boolean skipFirst)
    {
        this.skipFirst = skipFirst;
    }

    public boolean isSkipFirst()
    {
        return skipFirst;
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
            if(isOneShot())
                stop();
            else
                currentIndex = skipFirst ? 1 : 0;
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
