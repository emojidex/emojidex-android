package com.emojidex.emojidexandroid;

import android.os.Handler;

import com.emojidex.emojidexandroid.animation.updater.AnimationUpdater;

import java.util.HashSet;

/**
 * Created by kou on 17/11/17.
 */

class AnimationUpdaterManager {
    private final HashSet<AnimationUpdater> updaters = new HashSet<AnimationUpdater>();

    private int animationInterval = 100;
    private boolean isAnimating = false;

    /**
     * Regist view.
     * @param updater   Animation updater.
     */
    public void regist(AnimationUpdater updater)
    {
        updaters.add(updater);

        if( !isAnimating )
            startAnimation();
    }

    /**
     * Unregist view.
     * @param updater   Animation updater.
     */
    public void unregist(AnimationUpdater updater)
    {
        updaters.remove(updater);

        if( updaters.isEmpty() )
            stopAnimation();
    }

    /**
     * Set animation update interval time.
     * @param interval      Animation update interval time.(milli seconds/default value is 100)
     */
    public void setAnimationUpdateInterval(int interval)
    {
        animationInterval = interval;
    }

    /**
     * Start animation.
     */
    private void startAnimation()
    {
        isAnimating = true;

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                if( !isAnimating )
                    return;

                for(AnimationUpdater updater : updaters)
                    updater.update();

                handler.postDelayed(this, animationInterval);
            }
        });
    }

    /**
     * Stop animation.
     */
    private void stopAnimation()
    {
        isAnimating = false;
    }
}
