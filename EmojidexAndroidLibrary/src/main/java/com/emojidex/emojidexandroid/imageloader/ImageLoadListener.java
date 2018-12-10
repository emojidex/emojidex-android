package com.emojidex.emojidexandroid.imageloader;

import com.emojidex.emojidexandroid.EmojiFormat;

public interface ImageLoadListener {
    void onLoad(int handle, EmojiFormat format, String emojiName);
}
