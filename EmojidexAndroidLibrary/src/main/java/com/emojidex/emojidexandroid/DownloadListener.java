package com.emojidex.emojidexandroid;

import android.util.Log;

/**
 * Created by kou on 14/12/01.
 */
public class DownloadListener {
    static final String TAG = Emojidex.TAG + "::DownloadListener";

    /**
     * Called when before one json download.
     * @param source        Download from.
     * @param destination   Save to.
     */
    public void onPreOneJsonDownload(String source, String destination)
    {
        // nop
    }

    /**
     * Called when before one json download.
     */
    public void onPreOneJsonDownload()
    {
        // nop
    }

    /**
     * Called when after one json download.
     * @param source        Download from.
     * @param destination   Save to.
     */
    public void onPostOneJsonDownload(String source, String destination)
    {
        Log.d(TAG, "onPostOneJsonDownload: source = " + source + ", destination = " + destination);
    }

    /**
     * Called when after one json download.
     */
    public void onPostOneJsonDownload()
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
        downloader.download();
    }

    /**
     * Called when after all json download.
     * @param downloader    EmojiDownloader object.
     */
    public void onPostAllJsonDownload(NewEmojiDownloader downloader)
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
