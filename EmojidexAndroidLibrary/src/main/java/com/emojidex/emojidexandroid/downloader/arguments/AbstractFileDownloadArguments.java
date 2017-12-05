package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;

/**
 * Created by kou on 17/12/05.
 */

public abstract class AbstractFileDownloadArguments<Type extends AbstractFileDownloadArguments> implements ArgumentsInterface {
    private EmojiFormat format;

    /**
     * Construct object.
     */
    public AbstractFileDownloadArguments()
    {
        format = Emojidex.getInstance().getDefaultFormat();
    }

    /**
     * Get emoji format.
     * @return      Emoji format.
     */
    public EmojiFormat getFormat()
    {
        return format;
    }

    /**
     * Set emoji format.
     * @param format        Emoji format.
     * @return              Self.
     */
    public Type setFormat(EmojiFormat format)
    {
        this.format = (format == null) ? Emojidex.getInstance().getDefaultFormat() : format;
        return (Type)this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof AbstractFileDownloadArguments) )
            return false;

        final AbstractFileDownloadArguments arg = (AbstractFileDownloadArguments)obj;

        return  ArgumentsUtils.equals(format, arg.format);
    }
}
