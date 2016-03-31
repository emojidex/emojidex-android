package com.emojidex.emojidexandroid;

import android.util.Log;

import com.emojidex.libemojidex.Emojidex.Data.Collection;

import java.util.List;

/**
 * Created by kou on 14/12/01.
 */
public class DownloadListener {
    static final String TAG = Emojidex.TAG + "::DownloadListener";

    /**
     * Called when before one json download.
     */
    public void onPreOneJsonDownload()
    {
        // nop
    }

    /**
     * Called when after one json download.
     * @param emojiNames
     */
    public void onPostOneJsonDownload(List<String> emojiNames)
    {
        Log.d(TAG, "onPostOneJsonDownload:");
    }

    /**
     * Called when after all json download.
     * @param downloader    EmojiDownloader object.
     */
    public void onPostAllJsonDownload(EmojiDownloader downloader)
    {
        Log.d(TAG, "onPostAllJsonDownload");

        // Start download.
        downloader.startDownload();
    }

    /**
     * Called when before all emoji download.
     */
    public void onPreAllEmojiDownload()
    {
        Log.d(TAG, "onPreAllEmojiDownload");
    }

    /**
     * Called when before one emoji download.
     * @param emojiName     Download emoji name.
     */
    public void onPreOneEmojiDownload(String emojiName)
    {
        // nop
    }

    /**
     * Called when after one emoji download.
     * @param emojiName     Downloaded emoji name.
     */
    public void onPostOneEmojiDownload(String emojiName)
    {
        Log.d(TAG, "onPostOneEmojiDownload: emojiName = \"" + emojiName + "\"");
    }

    /**
     * Called when before all emoji download.
     */
    public void onPostAllEmojiDownload()
    {
        Log.d(TAG, "onPostAllEmojiDownload");
    }

    /**
     * Called when finish download task.
     */
    public void onFinish()
    {
        // nop
    }

    /**
     * Called when cancelled emoji download.
     */
    public void onCancelled()
    {
        Log.d(TAG, "onCancelled");
    }
}
