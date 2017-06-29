package com.emojidex.emojidexandroid.downloader;

/**
 * Download executor.
 */
abstract class AbstractDownloadExecutor
{
    private final EmojiDownloader downloader;

    /**
     * Construct object.
     * @param downloader    Emoji downloader.
     */
    AbstractDownloadExecutor(EmojiDownloader downloader)
    {
        this.downloader = downloader;
    }

    /**
     * Get EmojiDownloader object.
     * @return      EmojiDownloader object.
     */
    protected EmojiDownloader getDownloader()
    {
        return downloader;
    }

    /**
     * Download file.
     * @return  Succeeded download count.
     */
    protected abstract int download();

    /**
     * Get download count.
     * @return  Download count.
     */
    protected abstract int getDownloadCount();
}
