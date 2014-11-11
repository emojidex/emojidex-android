package com.emojidex.emojidexandroid;

import android.util.Log;

/**
 * Created by kou on 14/10/07.
 */
public class DownloadListener {
    static final String TAG = Emojidex.TAG + "::DownloadListener";

    /**
     * Called when download completed all json.
     */
    public void onJsonDownloadCompleted()
    {
        Log.d(TAG, "Called onJsonDownloadCompleted.");
    }

    /**
     * Called when before emoji download.
     * @param downloadCount     Count download.
     * @return      true if download start, false if download cancel.
     */
    public boolean onPreEmojiDownload(int downloadCount)
    {
        return true;
    }

    /**
     * Called when download completed one emoji.
     * @param emojiName     Emoji name.
     */
    public void onEmojiDownloadCompleted(String emojiName)
    {
        Log.d(TAG, "Called onEmojiDownloadCompleted.(emojiName = \"" + emojiName + "\")");
    }

    /**
     * Called when downloader task completed.
     */
    public void onAllDownloadCompleted()
    {
        Log.d(TAG, "Called onAllDownloadCompleted.");
    }
}
