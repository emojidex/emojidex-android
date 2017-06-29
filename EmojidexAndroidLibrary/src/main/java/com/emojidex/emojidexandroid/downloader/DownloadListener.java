package com.emojidex.emojidexandroid.downloader;

/**
 * Created by kou on 14/12/01.
 */
public class DownloadListener {
    /**
     * Called when update emojidex database.
     * @param handle    Download handle.
     */
    public void onUpdateDatabase(int handle)
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
     * Called when complete download emoji.
     * @param handle        Download handle.
     * @param emojiName     Download emoji name.
     */
    public void onDownloadEmoji(int handle, String emojiName)
    {
        // nop
    }

    /**
     * Called when complete download emoji archive.
     * @param handle        Download handle.
     * @param emojiNames    Download emoji names.
     */
    public void onDownloadEmojiArchive(int handle, String... emojiNames)
    {
        // nop
    }

    /**
     * Called when finish download task.
     * @param handle    Download handle.
     * @param result    Download result.
     */
    public void onFinish(int handle, EmojiDownloader.Result result)
    {
        // nop
    }

    /**
     * Called when cancelled emoji download.
     * @param handle    Download handle.
     * @param result    Download result.
     */
    public void onCancelled(int handle, EmojiDownloader.Result result)
    {
        // nop
    }
}
