package com.emojidex.emojidexandroid.animation.updater;

import com.emojidex.emojidexandroid.animation.EmojidexAnimationDrawable;

import java.util.Collection;

/**
 * Created by kou on 17/11/21.
 */

public interface AnimationUpdater {
    /**
     * Update animation.
     */
    void update();

    /**
     * Get animation drawable array.
     * @return      Animation drawable array.
     */
    Collection<EmojidexAnimationDrawable> getDrawables();
}
