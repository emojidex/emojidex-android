package com.emojidex.emojidexandroid.comparator;

import com.emojidex.emojidexandroid.Emoji;

import java.util.Comparator;

/**
 * Created by kou on 17/09/27.
 */

public class ScoreComparator implements Comparator<Emoji> {
    @Override
    public int compare(Emoji lhs, Emoji rhs)
    {
        return rhs.getScore() - lhs.getScore();
    }
}
