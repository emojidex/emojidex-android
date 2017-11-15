package com.emojidex.emojidexandroid.comparator;

import com.emojidex.emojidexandroid.Emoji;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

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
    private SimpleDateFormat format;

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
                temp = dateCompare(rhs.getCreatedAt(), lhs.getCreatedAt());
                break;
            case OLDEST:
                temp = dateCompare(lhs.getCreatedAt(), rhs.getCreatedAt());
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

        if (temp == 0 && type != SortType.NEWEST && type != SortType.OLDEST) {
            temp = dateCompare(lhs.getCreatedAt(), rhs.getCreatedAt());
        }

        return temp;
    }

    private Date stringToDate(String dateString) {
        if (format == null) format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private int dateCompare(String dateString1, String dateString2) {
        if (dateString1 == null || dateString2 == null) return 0;

        Date date1 = stringToDate(dateString1);
        Date date2 = stringToDate(dateString2);
        if (date1 == null || date2 == null) return 0;

        return date1.compareTo(date2);
    }
}
