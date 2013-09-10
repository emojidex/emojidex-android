package org.genshin.emojidexandroid;

import android.content.Context;

/**
 * Created by kou on 13/09/09.
 */
public class Emojidex {
    private final char separator = ':';
    private final EmojiDataManager emojiDataManager;

    /**
     * Construct Emojidex object.
     * @param context
     */
    public Emojidex(Context context)
    {
        emojiDataManager = EmojiDataManager.create(context);
    }

    /**
     * Normal text encode to Emojidex text.
     * @param text      Normal text.
     * @return          Emojidex text.
     */
    public String emojify(String text)
    {
        String result = text;
        int startIndex = -1;
        while( (startIndex = result.indexOf(separator, startIndex + 1)) != -1 )
        {
            final int endIndex = result.indexOf(separator, startIndex + 1);
            if(endIndex == -1)
                break;

            // Get EmojiData by emoji name.
            final String emojiName = result.substring(startIndex + 1, endIndex);
            final EmojiData emojiData = emojiDataManager.getEmojiDataByName(emojiName);
            if(emojiData == null)
                continue;

            // Replace emoji tag to emoji.
            final String regex = result.substring(startIndex, endIndex + 1);
            final String replacement = emojiData.getMoji();
            result = result.replace(regex, replacement);
        }
        return result;
    }

    /**
     * Emojidex text encode to normal text.
     * @param text      Emojidex text.
     * @return          Normal text.
     */
    public String deEmojify(String text)
    {
        String result = text;
        for(int i = 0;  i < result.length();  ++i)
        {
            final int codePoint = result.codePointAt(i);
            if( !isEmoji(codePoint) )
                continue;

            // Get EmojiData by unicode.
            final EmojiData emojiData = emojiDataManager.getEmojiDataByUnicode(codePoint);
            if(emojiData == null)
                continue;

            // Replace emoji to emoji tag.
            result = result.replace(
                    String.valueOf(Character.toChars(codePoint)),
                    separator + emojiData.getName() + separator
            );
        }
        return result;
    }

    /**
     *
     * @param codePoint
     * @return
     */
    private boolean isEmoji(int codePoint)
    {
        return codePoint >= 0x2000;
    }
}
