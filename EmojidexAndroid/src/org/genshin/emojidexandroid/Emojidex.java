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
        return text;
    }

    /**
     * Emojidex text encode to normal text.
     * @param text      Emojidex text.
     * @return          Normal text.
     */
    public String deEmojify(String text)
    {
        return text;
    }
}
