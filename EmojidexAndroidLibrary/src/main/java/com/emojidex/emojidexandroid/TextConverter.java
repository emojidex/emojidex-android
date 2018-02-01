package com.emojidex.emojidexandroid;

import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;

import com.emojidex.emojidexandroid.animation.EmojidexAnimationDrawable;
import com.emojidex.emojidexandroid.animation.EmojidexAnimationImageSpan;
import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kou on 14/10/09.
 */
class TextConverter {
    private static final String TAG = "EmojidexLibrary::TextConverter";
    private static final String CODE_REGEX = "(^.*?" + Emojidex.SEPARATOR + "|\\G[^" + Emojidex.SEPARATOR + "]*?[\\r\\n]+.*?" + Emojidex.SEPARATOR + "|.*?)([^" + Emojidex.SEPARATOR + "\\r\\n]+)" + Emojidex.SEPARATOR;
    private static final int CODE_GROUP = 2;

    private static final Emojidex emojidex = Emojidex.getInstance();

    /**
     * Normal text encode to emojidex text.
     * @param text              Normal text.
     * @param format            Image format.
     * @param autoDownload      Auto emoji download flag.
     *                          If true, auto download emoji when find unknown emoji.
     * @return                  Emojidex text.
     */
    public static CharSequence emojify(CharSequence text, EmojiFormat format, boolean autoDownload)
    {
        final SpannableString result = new SpannableString(text);
        final MojiCodesManager mojiCodesManager = MojiCodesManager.getInstance();

        // For moji.
        {
            final Pattern pattern = Pattern.compile(mojiCodesManager.getMojiRegex());
            final Matcher matcher = pattern.matcher(result);

            while(matcher.find())
            {
                final String code = matcher.group();
                if(code.isEmpty())
                    continue;

                emojifyOne(
                        result,
                        matcher.start(),
                        matcher.end(),
                        mojiCodesManager.MojiToCode(code),
                        format,
                        autoDownload
                );
            }
        }

        // For code.
        {
            final Pattern pattern = Pattern.compile(CODE_REGEX);
            final Matcher matcher = pattern.matcher(result);
            final int separatorLength = Emojidex.SEPARATOR.length();

            while(matcher.find())
            {
                emojifyOne(
                        result,
                        matcher.start(CODE_GROUP) - separatorLength,
                        matcher.end(CODE_GROUP) + separatorLength,
                        matcher.group(CODE_GROUP),
                        format,
                        autoDownload
                );
            }
        }

        return result;
    }

    /**
     * Emojidex text decode to normal text.
     * @param text  Emojidex text.
     * @return      Normal text.
     */
    public static CharSequence deEmojify(CharSequence text)
    {
        if( !(text instanceof Spanned) )
            return text;

        final SpannableString result = new SpannableString(text);
        for(DynamicDrawableSpan span : result.getSpans(0, result.length(), DynamicDrawableSpan.class))
            result.removeSpan(span);

        return result;
    }

    /**
     * Replace utf string to emojidex code.
     * @param text      Source text.
     * @return          Result text.
     */
    public static CharSequence codify(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder(text);
        final MojiCodesManager mojiCodesManager = MojiCodesManager.getInstance();

        {
            final Pattern pattern = Pattern.compile(mojiCodesManager.getMojiRegex());
            final Matcher matcher = pattern.matcher(result);

            int offset = 0;
            while(matcher.find())
            {
                final int start = matcher.start() + offset;
                final int end = matcher.end() + offset;

                if(start == end)
                    continue;

                final String code = Emojidex.SEPARATOR + mojiCodesManager.MojiToCode(matcher.group()) + Emojidex.SEPARATOR;

                result.replace(start, end, code);

                offset += code.length() - (end - start);
            }
        }

        // Put log.
        Log.d(TAG, "codify: " + text + " -> " + result);

        return result;
    }

    /**
     * Replace emojidex code to utf string.
     * @param text      Source text.
     * @return          Result text.
     */
    public static CharSequence mojify(CharSequence text)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder(text);
        final MojiCodesManager mojiCodesManager = MojiCodesManager.getInstance();

        {
            final Pattern pattern = Pattern.compile(CODE_REGEX);
            final Matcher matcher = pattern.matcher(result);
            final int separatorLength = Emojidex.SEPARATOR.length();

            int offset = 0;
            while(matcher.find())
            {
                final String moji = mojiCodesManager.CodeToMoji(matcher.group(CODE_GROUP));
                if(moji == null)
                    continue;

                final int start = matcher.start(CODE_GROUP) - separatorLength + offset;
                final int end = matcher.end(CODE_GROUP) + separatorLength + offset;

                result.replace(start, end, moji);

                offset += moji.length() - (end - start);
            }
        }

        // Put log.
        Log.d(TAG, "codify: " + text + " -> " + result);

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
        final DynamicDrawableSpan span = createSpan(emoji, format);
        final SpannableString result = new SpannableString(emoji.hasOriginalCodes() ? emoji.toTagString() : emoji.getText());
        result.setSpan(span, 0, result.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }

    /**
     * Emojify support method.
     * @param str               Target string.
     * @param start             Emojify area start.
     * @param end               Emojify area end.
     * @param code              Emoji name.
     * @param format            Image format.
     * @param autoDownload      Auto download flag.
     */
    private static void emojifyOne(SpannableString str, int start, int end, String code, EmojiFormat format, boolean autoDownload)
    {
        // Skip if already emojify.
        if(str.getSpans(start, end, DynamicDrawableSpan.class).length > 0)
            return;

        // Find emoji.
        final Emoji emoji = emojidex.getEmoji(code);

        // Unknown emoji.
        if(emoji == null)
        {
            if(autoDownload)
            {
                emojidex.getEmojiDownloader().downloadEmojies(
                        new EmojiDownloadArguments(code)
                                .addFormat(format)
                );
            }
            return;
        }

        // Emojify.
        final DynamicDrawableSpan span = createSpan(emoji, format);
        str.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**
     * Create image span from emoji.
     * @param emoji     Emoji.
     * @param format    Image format.
     * @return          Image span.
     */
    private static DynamicDrawableSpan createSpan(Emoji emoji, EmojiFormat format)
    {
        final Drawable drawable = emoji.getDrawable(format);
        return (drawable instanceof EmojidexAnimationDrawable)
                ? new EmojidexAnimationImageSpan((EmojidexAnimationDrawable)drawable)
                : new ImageSpan(drawable)
                ;
    }
}
