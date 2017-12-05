package com.emojidex.emojidexandroid.downloader.arguments;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;

/**
 * Created by kou on 17/08/29.
 */

public class ImageDownloadArguments extends AbstractFileDownloadArguments<ImageDownloadArguments> {
    private String emojiName;

    /**
     * Construct object.
     * @param emojiName     Emoji name.
     */
    public ImageDownloadArguments(String emojiName)
    {
        super();

        setEmojiName(emojiName);
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

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof ImageDownloadArguments) )
            return false;

        final ImageDownloadArguments arg = (ImageDownloadArguments)obj;

        return      ArgumentsUtils.equals(emojiName, arg.emojiName)
                &&  super.equals(obj)
                ;
    }
}
