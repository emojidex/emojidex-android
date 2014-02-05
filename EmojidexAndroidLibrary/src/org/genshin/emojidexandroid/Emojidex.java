package org.genshin.emojidexandroid;

import android.content.Context;
import android.text.SpannableStringBuilder;

import java.util.LinkedList;

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
     * Normal text encode to Emojidex text.
     * @param text      Normal text.
     * @return          Emojidex text.
     */
    public CharSequence emojify(CharSequence text)
    {
        final CharSequence result = emojifyImpl(text, true);
        android.util.Log.d("lib", "emojify : " + text + " -> " + result);
        return result;
    }

    /**
     * Emojidex text encode to normal text.
     * @param text      Emojidex text.
     * @return          Normal text.
     */
    public CharSequence deEmojify(CharSequence text)
    {
        final CharSequence result = deEmojifyImpl(text);
        android.util.Log.d("lib", "deEmojify : " + text + " -> " + result);
        return result;
    }

    /**
     * Text encode to unicode text.
     * @param text  Text.
     * @return      Unicode text.
     */
    public CharSequence toUnicodeString(CharSequence text)
    {
        final CharSequence result = emojifyImpl( deEmojifyImpl(text), false );
        android.util.Log.d("lib", "toUnicodeString : " + text + " -> " + result);
        return result;
    }

    /**
     * Normal text encode to Emojidex text.
     * @param text      Normal text.
     * @param useImage  If false use unicode emoji.
     * @return          Emojidex text.
     */
    private CharSequence emojifyImpl(CharSequence text, boolean useImage)
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

                // Get EmojiData by emoji name.
                final String emojiName = text.subSequence(startIndex + 1, endIndex).toString();
                final EmojiData emojiData = emojiDataManager.getEmojiData(emojiName);

                // String is not emoji tag.
                if(emojiData == null)
                {
                    result.append( text.subSequence(startIndex, endIndex) );
                    startIndex = endIndex;
                    continue;
                }

                // This string is emoji tag !!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if(useImage)
                    result.append(emojiData.createImageString());
                else if(emojiData.isOriginalEmoji())
                    result.append( text.subSequence(startIndex, endIndex) );
                else
                    result.append(emojiData.getMoji());
                startIndex = endIndex + charCount;
                startIsSeparator = false;
            }
        }

        // Last string is not emoji tag.
        if(startIndex < length)
        {
            result.append( text.subSequence(startIndex, length) );
        }

        return result;
    }

    /**
     * Emojidex text decode to normal text.
     * @param text      Emojidex text.
     * @return          Normal text.
     */
    private CharSequence deEmojifyImpl(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();

        LinkedList<Integer> codes = new LinkedList<Integer>();
        int start = 0;
        int next = 0;

        final int count = Character.codePointCount(text, 0, text.length());
        for(int i = 0;  i < count;  ++i)
        {
            final int codePoint = Character.codePointAt(text, next);
            final int cur = next;
            next += Character.charCount(codePoint);
            codes.addLast(codePoint);

            if(codes.size() < 2)
                continue;

            // Find combining character emoji.
            EmojiData emojiData = emojiDataManager.getEmojiData(codes);
            if( emojiData != null )
            {
                start = next;
                codes.clear();
            }

            // Find single character emoji.
            else
            {
                emojiData = emojiDataManager.getEmojiData(codes.subList(0, 1));
                codes.removeFirst();

                // Not emoji.
                if(emojiData == null)
                {
                    result.append( text.subSequence(start, cur) );
                    start = cur;
                    continue;
                }
                start = cur;
            }

            // Emoji to tag.
            result.append(separator + emojiData.getName() + separator);
        }
        if( !codes.isEmpty() )
        {
            final EmojiData emojiData = emojiDataManager.getEmojiData(codes);

            if(emojiData == null)
                result.append( text.subSequence(start, next) );
            else
                result.append(separator + emojiData.getName() + separator);
        }
         return result;
    }
}
