package org.genshin.emojidexandroid;

import android.content.Context;
import android.text.SpannableStringBuilder;

/**
 * Created by kou on 13/09/09.
 */
public class Emojidex {
    private final String separator = ":";
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
     * Normal text_view encode to Emojidex text_view.
     * @param text      Normal text_view.
     * @return          Emojidex text_view.
     */
    public CharSequence emojify(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();

        final int length = text.length();
        int charCount = 0;
        int startIndex = 0;
        boolean startIsSeparator = false;
        for(int i = 0;  i < length;  i += charCount)
        {
            final int codePoint = Character.codePointAt(text, i);
            charCount = Character.charCount(codePoint);

            if( String.valueOf(Character.toChars(codePoint)).equals(separator) )
            {
                final int endIndex = i;

                // Start character is not separator.
                if( !startIsSeparator )
                {
                    result.append( text.subSequence(startIndex, endIndex) );
                    startIndex = endIndex;
                    startIsSeparator = true;
                    continue;
                }

                // Get EmojiData by emoji_view name.
                final String emojiName = text.subSequence(startIndex + 1, endIndex).toString();
                final EmojiData emojiData = emojiDataManager.getEmojiData(emojiName);

                // String is not emoji_view tag.
                if(emojiData == null)
                {
                    result.append( text.subSequence(startIndex, endIndex) );
                    startIndex = endIndex;
                    continue;
                }

                // This string is emoji_view tag !!!!!!!!!!!!!!!!!!!!!!!!!!!!
                result.append( emojiData.createImageString() );
                startIndex = endIndex + charCount;
                startIsSeparator = false;
            }
        }

        // Last string is not emoji_view tag.
        if(startIndex < length)
        {
            result.append( text.subSequence(startIndex, length) );
        }

        android.util.Log.d("lib", "emojify : " + text + " -> " + result);

        return result;
    }

    /**
     * Emojidex text_view encode to normal text_view.
     * @param text      Emojidex text_view.
     * @return          Normal text_view.
     */
    public CharSequence deEmojify(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();

        final int length = text.length();
        int charCount = 0;
        for(int i = 0;  i < length;  i += charCount)
        {
            final int codePoint = Character.codePointAt(text, i);
            charCount = Character.charCount(codePoint);
            if( !isEmoji(codePoint) )
            {
                result.append( text.subSequence(i, i + charCount) );
                continue;
            }

            // Get EmojiData by code.
            final EmojiData emojiData = emojiDataManager.getEmojiData(codePoint);
            if(emojiData == null)
            {
                result.append( text.subSequence(i, i + charCount) );
                continue;
            }

            // Replace emoji_view to emoji_view tag.
            result.append(separator + emojiData.getName() + separator);
        }

        android.util.Log.d("lib", "deEmojify : " + text + " -> " + result);

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
