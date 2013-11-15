package org.genshin.emojidexandroid;

import android.content.Context;
import android.inputmethodservice.Keyboard;

/**
 * Created by nazuki on 2013/11/14.
 */
public class KeyboardTest extends Keyboard {

    public KeyboardTest(Context context) {
        super(context, R.xml.keyboard);
    }

    /*
    public static KeyboardTest create(Context context, List<EmojiData> emojiDatas, int minHeight)
    {
        // Set keyboard parameters.
        emojiCount = (emojiDatas != null) ? emojiDatas.size() : 0;
        EmojidexKeyboard.minHeight = minHeight;

        // Create keyboard.
        KeyboardTest newKeyboard = new KeyboardTest(context);

        // Key add test.
        for(int i = 0;  i < emojiCount;  ++i)
        {
            newKeyboard.addKey( emojiDatas.get(i) );
        }

        return newKeyboard;
    }
    */
}
