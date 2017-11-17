package com.emojidex.emojidexandroid;

import java.util.Locale;

/**
 * Created by kou on 17/08/29.
 */

public enum EmojidexLocale {
    JAPANESE("ja"),
    ENGLISH("en")
    ;

    private final String locale;

    /**
     * Get default locale.
     */
    public static EmojidexLocale getDefault()
    {
        final Locale defaultLocale = Locale.getDefault();

        if(     defaultLocale.equals(Locale.JAPAN)
            ||  defaultLocale.equals(Locale.JAPANESE)   )
            return JAPANESE;

        return ENGLISH;
    }

    /**
     * Construct object.
     * @param locale    Locale.
     */
    EmojidexLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Get locale string.
     * @return      Locale string.
     */
    public String getLocale()
    {
        return locale;
    }
}
