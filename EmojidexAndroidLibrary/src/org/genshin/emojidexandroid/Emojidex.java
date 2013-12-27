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
     * Normal text encode to Emojidex text.
     * @param text      Normal text.
     * @return          Emojidex text.
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
                result.append( emojiData.createImageString() );
                startIndex = endIndex + charCount;
                startIsSeparator = false;
            }
        }

        // Last string is not emoji tag.
        if(startIndex < length)
        {
            result.append( text.subSequence(startIndex, length) );
        }

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
        final CharSequence result = text;

//        final SpannableStringBuilder result = new SpannableStringBuilder();
//
//        final int length = text.length();
//        int charCount = 0;
//        for(int i = 0;  i < length;  i += charCount)
//        {
//            final int codePoint = Character.codePointAt(text, i);
//            charCount = Character.charCount(codePoint);
//            if( !isEmoji(codePoint) )
//            {
//                result.append( text.subSequence(i, i + charCount) );
//                continue;
//            }
//
//            // Get EmojiData by code.
//            final EmojiData emojiData = emojiDataManager.getEmojiData(codePoint);
//            if(emojiData == null)
//            {
//                result.append( text.subSequence(i, i + charCount) );
//                continue;
//            }
//
//            // Replace emoji to emoji tag.
//            result.append(separator + emojiData.getName() + separator);
//        }

        android.util.Log.d("lib", "deEmojify : " + text + " -> " + result);

        return result;
    }

    public CharSequence toUnicodeString(CharSequence text)
    {
        final CharSequence result = text;

//        final CharSequence src = emojify(text);
//        final SpannableStringBuilder result = new SpannableStringBuilder();
//
//        final int length = src.length();
//        int charCount = 0;
//        for(int i = 0;  i < length;  i += charCount)
//        {
//            final int codePoint = Character.codePointAt(src, i);
//            charCount = Character.charCount(codePoint);
//            if( !isEmoji(codePoint) )
//            {
//                result.append( src.subSequence(i, i + charCount) );
//                continue;
//            }
//
//            // Get EmojiData by code.
//            final EmojiData emojiData = emojiDataManager.getEmojiData(codePoint);
//            if(emojiData == null)
//            {
//                result.append( src.subSequence(i, i + charCount) );
//                continue;
//            }
//            if(emojiData.isUnicodeEmoji())
//            {
//                result.append(emojiData.getMoji());
//                continue;
//            }
//            result.append( separator + emojiData.getName() + separator );
//        }

        android.util.Log.d("lib", "toUnicodeString : " + text + " -> " + result);

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
