package com.emojidex.emojidexandroid.comparator;

import com.emojidex.emojidexandroid.Emoji;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by kou on 17/09/27.
 */

public class EmojiComparator implements Comparator<Emoji> {
    private static final int SCORE = 0;
    private static final int UNPOPULAR = 1;
    private static final int NEWEST = 2;
    private static final int OLDEST = 3;
    private static final int LIKED = 4;
    private static final int UNLIKED = 5;
    private static final int SHORTEST = 6;
    private static final int LONGEST = 7;
    public static final String[] SORT_KEYS = { "score", "unpopular", "newest", "oldest",
                                               "liked", "unliked", "shortest", "longest" };
    private static final HashMap<String, Integer> keyMap = new HashMap<String, Integer>() {
        {
            put("score", SCORE);
            put("unpopular", UNPOPULAR);
            put("newest", NEWEST);
            put("oldest", OLDEST);
            put("liked", LIKED);
            put("unliked", UNLIKED);
            put("shortest", SHORTEST);
            put("longest", LONGEST);
        }
    } ;

    private int type;

    public EmojiComparator() {
        this.type = SCORE;
    }

    public EmojiComparator(String type) {
        this.type = keyMap.get(type);
    }

    @Override
    public int compare(Emoji lhs, Emoji rhs)
    {
        int temp;
        switch (type) {
            case SCORE:
                temp = rhs.getScore() - lhs.getScore();
                break;
            case UNPOPULAR:
                temp = lhs.getScore() - rhs.getScore();
                break;
            case NEWEST:
                // TODO: created_at
                temp = rhs.getScore() - lhs.getScore();
                break;
            case OLDEST:
                // TODO: created_at
                temp = rhs.getScore() - lhs.getScore();
                break;
            case LIKED:
                temp = (int)(rhs.getFavorited() - lhs.getFavorited());
                break;
            case UNLIKED:
                temp =  (int)(lhs.getFavorited() - rhs.getFavorited());
                break;
            case SHORTEST:
                temp = lhs.getCode().length() - rhs.getCode().length();
                break;
            case LONGEST:
                temp = rhs.getCode().length() - lhs.getCode().length();
                break;
            default:
                temp = rhs.getScore() - lhs.getScore();

        }

        if (temp == 0 && type != SCORE && type != UNPOPULAR) temp = rhs.getScore() - lhs.getScore();

        return temp;
    }
}
