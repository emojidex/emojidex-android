package com.emojidex.emojidexandroid;

import android.content.Context;
import android.text.SpannableStringBuilder;

import java.util.ArrayList;

/**
 * Created by nazuki on 2014/02/13.
 */
public class EmojidexEditor extends Emojidex {
    private final String separator = ":";
    private ArrayList<String> emojiNames;

    /**
     * Construct Emojidex object.
     * @param context
     */
    public EmojidexEditor(Context context) {
        super(context);
    }

    /**
     * Normal text encode to Emojidex text.
     * @param text      Normal text.
     * @param useImage  If false use unicode emoji.
     * @return          Emojidex text.
     */
    @Override
    protected CharSequence emojifyImpl(CharSequence text, boolean useImage)
    {
        final SpannableStringBuilder result = new SpannableStringBuilder();
        emojiNames = new ArrayList<String>();

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
                final EmojiData emojiData = getEmojiDataManager().getEmojiData(emojiName);

                // String is not emoji tag.
                if(emojiData == null)
                {
                    result.append( text.subSequence(startIndex, endIndex) );
                    startIndex = endIndex;
                    continue;
                }

                // This string is emoji tag !!!!!!!!!!!!!!!!!!!!!!!!!!!!
                if(useImage)
                {
                    emojiNames.add(emojiData.getName());
                    result.append(emojiData.createImageString());
                }
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

    public ArrayList<String> getEmojiNames()
    {
        return emojiNames;
    }
}
