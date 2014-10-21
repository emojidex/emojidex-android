package org.genshin.emojidexandroid2;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import org.genshin.emojidexandroid.EmojiData;

import java.util.LinkedList;

/**
 * Created by kou on 14/10/09.
 */
class TextConverter {
    static final String TAG = Emojidex.TAG + "::TextConverter";

    private static final String SEPARATOR = ":";

    private static final Emojidex emojidex = Emojidex.getInstance();

    /**
     * Normal text encode to emojidex text.
     * @param text      Normal text.
     * @param useImage  If true, use phantom-emoji image.
     * @param format    Image format.
     * @return          Emojidex text.
     */
    public static CharSequence emojify(CharSequence text, boolean useImage, EmojiFormat format)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();

        final int length = text.length();
        int charCount;
        int startIndex = 0;
        boolean startIsSeparator = false;
        for(int i = 0;  i < length;  i += charCount)
        {
            final int codePoint = Character.codePointAt(text, i);
            charCount = Character.charCount(codePoint);

            // Find separator.
            if( String.valueOf(Character.toChars(codePoint)).equals(SEPARATOR) )
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
                final Emoji emoji = emojidex.getEmoji(emojiName);

                // String is not emoji tag.
                if(emoji == null)
                {
                    result.append( text.subSequence(startIndex, endIndex) );
                    startIndex = endIndex;
                    continue;
                }

                // This string is emoji tag !!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if(useImage)
                {
                    result.append(createEmojidexText(emoji, format));
                }
                else if(emoji.hasOriginalCodes())
                    result.append( text.subSequence(startIndex, endIndex) );
                else
                    result.append(emoji.getText());
                startIndex = endIndex + charCount;
                startIsSeparator = false;
            }
        }

        // Last string is not emoji tag.
        if(startIndex < length)
        {
            result.append( text.subSequence(startIndex, length) );
        }

        // Put log.
        Log.d(TAG, "emojify: " + text + " -> " + result);

        return result;
    }

    /**
     * Emojidex text decode to normal text.
     * @param text  Emojidex text.
     * @return      Normal text.
     */
    public static CharSequence deEmojify(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();

        final LinkedList<Integer> codes = new LinkedList<Integer>();
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
            Emoji emoji = emojidex.getEmoji(codes);
            if( emoji != null )
            {
                start = next;
                codes.clear();
            }

            // Find single character emoji.
            else
            {
                emoji = emojidex.getEmoji(codes.subList(0, 1));
                codes.removeFirst();

                // Not emoji.
                if(emoji == null)
                {
                    result.append( text.subSequence(start, cur) );
                    start = cur;
                    continue;
                }
                start = cur;
            }

            // Emoji to tag.
            result.append(SEPARATOR + emoji.getName() + SEPARATOR);
        }
        if( !codes.isEmpty() )
        {
            final Emoji emoji = emojidex.getEmoji(codes);

            if(emoji == null)
                result.append( text.subSequence(start, next) );
            else
                result.append(SEPARATOR + emoji.getName() + SEPARATOR);
        }

        // Put log.
        Log.d(TAG, "deEmojify: " + text + " -> " + result);

        return result;
    }

    /**
     * Create emojidex text from emoji.
     * @param emoji     Emoji.
     * @param format    Image format.
     * @return      Emojidex text.
     */
    static CharSequence createEmojidexText(Emoji emoji, EmojiFormat format)
    {
        final ImageSpan imageSpan = new ImageSpan(emoji.getDrawable(format));
        final SpannableString result = new SpannableString(emoji.getText());
        result.setSpan(imageSpan, 0, result.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
}
