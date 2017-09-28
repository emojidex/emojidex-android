package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojiFormat;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by kou on 17/08/29.
 */

public abstract class AbstractJsonDownloadArguments<Type extends AbstractJsonDownloadArguments> implements ArgumentsInterface {
    private final Collection<EmojiFormat> formats;

    /**
     * Construct object.
     */
    public AbstractJsonDownloadArguments()
    {
        formats = new LinkedHashSet();
    }

    /**
     * Add emoji format.
     * @param format    Emoji format.
     * @return          Self.
     */
    public Type addFormat(EmojiFormat format)
    {
        formats.add(format);
        return (Type)this;
    }

    /**
     * Remove emoji format.
     * @param format    Emoji format.
     * @return          Self.
     */
    public Type removeFormat(EmojiFormat format)
    {
        formats.remove(format);
        return (Type)this;
    }

    /**
     * Clear and set emoji formats.
     * @param formats   Emoji formats.
     * @return          Self.
     */
    public Type setFormats(EmojiFormat... formats)
    {
        clearFormats();
        for(EmojiFormat format : formats)
            addFormat(format);
        return (Type)this;
    }

    /**
     * Clear emoji formats.
     * @return      Self.
     */
    public Type clearFormats()
    {
        formats.clear();
        return (Type)this;
    }

    /**
     * Get emoji formats.
     * @return      Emoji formats.
     */
    public Collection<EmojiFormat> getFormats()
    {
        return formats;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof AbstractJsonDownloadArguments) )
            return false;

        final AbstractJsonDownloadArguments<Type> arg = (AbstractJsonDownloadArguments<Type>)obj;

        if(formats.size() != arg.formats.size())
            return false;

        for(EmojiFormat format : formats)
        {
            if( !arg.formats.contains(format) )
                return false;
        }

        return true;
    }
}
