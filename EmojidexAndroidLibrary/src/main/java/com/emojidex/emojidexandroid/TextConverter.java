package com.emojidex.emojidexandroid;

import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;

import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;

import java.util.LinkedList;

/**
 * Created by kou on 14/10/09.
 */
class TextConverter {
    private static final String TAG = "EmojidexLibrary::TextConverter";

    private static final Emojidex emojidex = Emojidex.getInstance();

    /**
     * Normal text encode to emojidex text.
     * @param text              Normal text.
     * @param useImage          If true, use phantom-emoji image.
     * @param format            Image format.
     * @param autoDownload      Auto emoji download flag.
     *                          If true, auto download emoji when find unknown emoji.
     * @return                  Emojidex text.
     */
    public static CharSequence emojify(CharSequence text, boolean useImage, EmojiFormat format, boolean autoDownload)
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
            if( String.valueOf(Character.toChars(codePoint)).equals(Emojidex.SEPARATOR) )
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

                // Unknown emoji.
                if(emoji == null)
                {
                    // Auto download emoji.
                    if(     autoDownload
                        &&  useImage
                        &&  !emojiName.isEmpty()    )
                    {
                        emojidex.getEmojiDownloader().downloadEmojies(
                                new EmojiDownloadArguments(emojiName)
                                        .addFormat(format)
                                        // TODO .setUser(username, authotoken) sinakucha nano de ha
                        );
                    }

                    // String is not emoji tag.
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
                    result.append( text.subSequence(startIndex, endIndex + charCount) );
                else
                    result.append(emoji.getMoji());
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
        final LinkedList<Integer> tmp = new LinkedList<Integer>();
        int lastMojiCount = 0;

        final int length = text.length();
        int i = 0;
        while(i < length)
        {
            final int codePoint = Character.codePointAt(text, i);
            final int charCount = Character.charCount(codePoint);

            // 0x200D = ZWJ
            if(codePoint == 0x200D)
                lastMojiCount = 0;
            else if(++lastMojiCount >= 3)
            {
                lastMojiCount = 0;

                while(codes.size() > 1)
                {
                    // To emoji string when has emoji.
                    do
                    {
                        Emoji emoji = emojidex.getEmoji(codes);
                        if(emoji == null)
                        {
                            tmp.addFirst(codes.removeLast());
                            continue;
                        }

                        // Emoji to tag.
                        result.append(emoji.toTagString());

                        // Next.
                        codes.clear();
                        codes.addAll(tmp);
                        tmp.clear();
                    } while(codes.size() > 0 && codes.size() + tmp.size() > 1);

                    codes.addAll(tmp);
                    tmp.clear();

                    // First character is not emoji.
                    if(codes.size() > 1)
                        result.append(new String(Character.toChars(codes.removeFirst())));
                }
            }

            codes.addLast(codePoint);
            i += charCount;
        }

        while( !codes.isEmpty() )
        {
            // To emoji string when has emoji.
            do
            {
                Emoji emoji = emojidex.getEmoji(codes);
                if(emoji == null)
                {
                    tmp.addFirst(codes.removeLast());
                    continue;
                }

                // Emoji to tag.
                result.append(emoji.toTagString());

                // Next.
                codes.clear();
                codes.addAll(tmp);
                tmp.clear();
            } while(codes.size() > 0);

            codes.addAll(tmp);
            tmp.clear();

            // First character is not emoji.
            if( !codes.isEmpty() )
                result.append(new String(Character.toChars(codes.removeFirst())));
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
        final Drawable drawable = emoji.getDrawable(format);
        final DynamicDrawableSpan span =
                (drawable instanceof EmojidexAnimationDrawable)
                        ? new EmojidexAnimationImageSpan((EmojidexAnimationDrawable)drawable)
                        : new ImageSpan(drawable);
        final SpannableString result = new SpannableString(emoji.hasOriginalCodes() ? emoji.toTagString() : emoji.getMoji());
        result.setSpan(span, 0, result.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }
}
