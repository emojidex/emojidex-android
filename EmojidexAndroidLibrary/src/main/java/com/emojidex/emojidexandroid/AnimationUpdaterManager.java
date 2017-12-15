package com.emojidex.emojidexandroid;

import android.os.Handler;

import com.emojidex.emojidexandroid.animation.EmojidexAnimationDrawable;
import com.emojidex.emojidexandroid.animation.updater.AnimationUpdater;

import java.util.HashSet;

/**
 * Created by kou on 17/11/17.
 */

class AnimationUpdaterManager {
    private final static long NEXT_UPDATE_TIME_MAX = 0x7FFFFFFF;
    private final static long NEXT_UPDATE_TIME_MIN = 1000 / 60;

    private final HashSet<AnimationUpdater> updaters = new HashSet<AnimationUpdater>();

    /**
     * Regist view.
     * @param updater   Animation updater.
     */
    public void regist(AnimationUpdater updater)
    {
        // Skip if already regist updater.
        if(updaters.contains(updater))
            return;

        // Run animation.
        updaters.add(updater);
        startAnimation(updater);
    }

    /**
     * Unregist view.
     * @param updater   Animation updater.
     */
    public void unregist(AnimationUpdater updater)
    {
        updaters.remove(updater);
    }

    /**
     * Start animation.
     * @param updater   Animation updater.
     */
    private void startAnimation(final AnimationUpdater updater)
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run()
            {
                // End animation if updater is not found.
                if( !updaters.contains(updater) )
                    return;

                // Animation.
                updater.update();

                // Schedule next update.
                final long nextUpdateTime = calcNextUpdateTime(updater);
                if(nextUpdateTime < NEXT_UPDATE_TIME_MAX)
                    handler.postDelayed(this, nextUpdateTime);
            }
        }, calcNextUpdateTime(updater));
    }

    /**
     * Calculation time of next update.
     * @param updater   Animation updater.
     * @return          Time of next update.
     */
    private long calcNextUpdateTime(AnimationUpdater updater)
    {
        long result = NEXT_UPDATE_TIME_MAX;
        for(EmojidexAnimationDrawable d : updater.getDrawables())
        {
            final long time = d.getCurrentDuration() - d.getCurrentTime();
            result = Math.min(result, time);
        }
        result = Math.max(result, NEXT_UPDATE_TIME_MIN);
        return result;
    }
}
