package com.emojidex.emojidexandroid;

import android.util.Log;

/**
 * Created by kou on 14/10/07.
 */
public class DownloadListener {
    static final String TAG = Emojidex.TAG + "::DownloadListener";

    public void onJsonDownloadCompleted()
    {
        Log.d(TAG, "Called onJsonDownloadCompleted.");
    }

    public void onEmojiDownloadCompleted(String emojiName)
    {
        Log.d(TAG, "Called onEmojiDownloadCompleted.(emojiName = \"" + emojiName + "\")");
    }

    public void onAllDownloadCompleted()
    {
        Log.d(TAG, "Called onAllDownloadCompleted.");
    }
}
