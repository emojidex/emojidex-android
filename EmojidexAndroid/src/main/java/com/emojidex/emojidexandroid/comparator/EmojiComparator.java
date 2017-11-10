package com.emojidex.emojidexandroid.comparator;

import com.emojidex.emojidexandroid.Emoji;

import java.util.Comparator;

/**
 * Created by kou on 17/09/27.
 */

public class EmojiComparator implements Comparator<Emoji> {
    public enum SortType {
        SCORE(0),
        UNPOPULAR(1),
        NEWEST(2),
        OLDEST(3),
        LIKED(4),
        UNLIKED(5),
        SHORTEST(6),
        LONGEST(7);

        private final int value;

        SortType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SortType fromInt(int value) {
            for (SortType type : SortType.values()) {
                if (type.getValue() == value)  return type;
            }
            return SCORE;
        }
    }

    private SortType type;

    public EmojiComparator() {
        this.type = SortType.SCORE;
    }

    public EmojiComparator(SortType type) {
        this.type = type;
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

        if (temp == 0 && type != SortType.SCORE && type != SortType.UNPOPULAR) temp = rhs.getScore() - lhs.getScore();

        return temp;
    }
}
