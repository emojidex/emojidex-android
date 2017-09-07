package com.emojidex.emojidexandroid.downloader.arguments;

/**
 * Created by kou on 17/08/29.
 */

public class EmojiDownloadArguments extends AbstractJsonDownloadArguments<EmojiDownloadArguments> {
    private String emojiName;

    /**
     * Construct object.
     * @param emojiName     Emoji name.
     */
    public EmojiDownloadArguments(String emojiName)
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
    public EmojiDownloadArguments setEmojiName(String emojiName)
    {
        this.emojiName = (emojiName != null) ? emojiName : "";
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof EmojiDownloadArguments) )
            return false;

        final EmojiDownloadArguments arg = (EmojiDownloadArguments)obj;

        return      ArgumentsUtils.equals(emojiName, arg.emojiName)
                &&  super.equals(obj)
                ;
    }
}
