package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojidexLocale;

/**
 * Created by kou on 17/08/29.
 */

public class ExtendedDownloadArguments extends AbstractJsonDownloadArguments<ExtendedDownloadArguments> {
    private EmojidexLocale locale;

    /**
     * Construct object.
     */
    public ExtendedDownloadArguments()
    {
        super();

        locale = EmojidexLocale.getDefault();
    }

    /**
     * Set locale.
     * @param locale    Locale.(Default value is EmojidexLocale.DEFAULT)
     * @return          Self.
     */
    public ExtendedDownloadArguments setLocale(EmojidexLocale locale)
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
        if( !(obj instanceof  ExtendedDownloadArguments) )
            return false;

        final ExtendedDownloadArguments arg = (ExtendedDownloadArguments)obj;

        return      ArgumentsUtils.equals(locale.getLocale(), arg.locale.getLocale())
                &&  super.equals(obj)
                ;
    }
}
