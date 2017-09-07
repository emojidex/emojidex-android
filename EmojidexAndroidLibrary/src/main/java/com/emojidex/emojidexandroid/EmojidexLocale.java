package com.emojidex.emojidexandroid;

import java.util.Locale;

/**
 * Created by kou on 17/08/29.
 */

public enum EmojidexLocale {
    DEFAULT,
    JAPANESE("ja"),
    ENGLISH("en")
    ;

    private final String locale;

    /**
     * Construct object for DEFAULT.
     */
    EmojidexLocale()
    {
        final Locale defaultLocale = Locale.getDefault();

        if(     defaultLocale.equals(Locale.JAPAN)
            ||  defaultLocale.equals(Locale.JAPANESE)   )
            locale = "ja";

        else
            locale = "en";
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
