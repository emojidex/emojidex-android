package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;

/**
 * Created by kou on 17/08/29.
 */

public class ImageDownloadArguments implements ArgumentsInterface {
    private String emojiName;
    private EmojiFormat format;

    /**
     * Construct object.
     * @param emojiName     Emoji name.
     */
    public ImageDownloadArguments(String emojiName)
    {
        setEmojiName(emojiName);
        format = Emojidex.getInstance().getDefaultFormat();
    }

    /**
     * Get emoji name.
     * @return      Emoji name.
     */
    public String getEmojiName()
    {
        return emojiName;
    }

    /**
     * Set emoji name.
     * @param emojiName     Emoji name.
     * @return              Self.
     */
    public ImageDownloadArguments setEmojiName(String emojiName)
    {
        this.emojiName = (emojiName != null) ? emojiName : "";
        return this;
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
    public ImageDownloadArguments setFormat(EmojiFormat format)
    {
        this.format = (format == null) ? Emojidex.getInstance().getDefaultFormat() : format;
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof ImageDownloadArguments) )
            return false;

        final ImageDownloadArguments arg = (ImageDownloadArguments)obj;

        return      ArgumentsUtils.equals(emojiName, arg.emojiName)
                &&  ArgumentsUtils.equals(format, arg.format)
                ;
    }
}
