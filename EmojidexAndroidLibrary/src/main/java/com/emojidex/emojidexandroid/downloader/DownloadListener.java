package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojiFormat;

/**
 * Created by kou on 14/12/01.
 */
public class DownloadListener {
    /**
     * Called when update emojidex database.
     */
    public void onUpdateDatabase()
    {
        // nop
    }

    /**
     * Called when complete download json.
     * @param handle        Download handle.
     * @param emojiNames    Emoji names from download json.
     */
    public void onDownloadJson(int handle, String... emojiNames)
    {
        // nop
    }

    /**
     * Called when complete download emoji image.
     * @param handle        Download handle.
     * @param emojiName     Download emoji name.
     * @param format        Download emoji format.
     */
    public void onDownloadImage(int handle, String emojiName, EmojiFormat format)
    {
        // nop
    }

    /**
     * Called when finish download task.
     * @param handle    Download handle.
     * @param result    true if download succeeded.
     */
    public void onFinish(int handle, boolean result)
    {
        // nop
    }

    /**
     * Called when cancelled emoji download.
     * @param handle    Download handle.
     * @param result    true if download succeeded.
     */
    public void onCancelled(int handle, boolean result)
    {
        // nop
    }
}
