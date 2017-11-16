package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojidexLocale;

/**
 * Created by kou on 17/08/29.
 */

public class UTFDownloadArguments extends AbstractJsonDownloadArguments<UTFDownloadArguments> {
    private EmojidexLocale locale;

    /**
     * Construct object.
     */
    public UTFDownloadArguments()
    {
        super();

        locale = EmojidexLocale.getDefault();
    }

    /**
     * Set locale.
     * @param locale    Locale.(Default value is EmojidexLocale.DEFAULT)
     * @return          Self.
     */
    public UTFDownloadArguments setLocale(EmojidexLocale locale)
    {
        this.locale = (locale == null) ? EmojidexLocale.getDefault() : locale;
        return this;
    }

    /**
     * Get locale.
     * @return      Locale.
     */
    public EmojidexLocale getLocale()
    {
        return locale;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof  UTFDownloadArguments) )
            return false;

        final UTFDownloadArguments arg = (UTFDownloadArguments)obj;

        return      ArgumentsUtils.equals(locale.getLocale(), arg.locale.getLocale())
                &&  super.equals(obj)
                ;
    }
}
