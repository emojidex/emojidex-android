package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojidexLocale;

/**
 * Created by kou on 18/01/23.
 */

public class MojiCodesDownloadArguments implements ArgumentsInterface {
    private EmojidexLocale locale;

    /**
     * Construct object.
     */
    public MojiCodesDownloadArguments()
    {
        super();

        locale = EmojidexLocale.getDefault();
    }

    /**
     * Set locale.
     * @param locale    Locale.(Default value is EmojidexLocale.DEFAULT)
     * @return          Self.
     */
    public MojiCodesDownloadArguments setLocale(EmojidexLocale locale)
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
        if( !(obj instanceof  MojiCodesDownloadArguments) )
            return false;

        final MojiCodesDownloadArguments arg = (MojiCodesDownloadArguments)obj;

        return ArgumentsUtils.equals(locale.getLocale(), arg.locale.getLocale());
    }
}
