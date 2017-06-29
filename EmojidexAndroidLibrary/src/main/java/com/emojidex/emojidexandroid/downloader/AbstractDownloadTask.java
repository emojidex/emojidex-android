package com.emojidex.emojidexandroid.downloader;

import android.os.AsyncTask;

/**
 * Download task.
 */
abstract class AbstractDownloadTask<Executor extends AbstractDownloadExecutor> extends AsyncTask<Executor, Void, EmojiDownloader.Result>
{
    private EmojiDownloader downloader;

    public AbstractDownloadTask(EmojiDownloader downloader)
    {
        this.downloader = downloader;
    }

    protected EmojiDownloader getDownloader()
    {
        return downloader;
    }

    @Override
    protected EmojiDownloader.Result doInBackground(Executor... params)
    {
        final EmojiDownloader.Result result = new EmojiDownloader.Result();

        for(Executor executor : params)
        {
            result.addTotal(executor.getDownloadCount());

            // Skip if download cancelled.
            if(isCancelled())
                continue;

            // Download file.
            result.addSucceeded(executor.download());
        }

        return result;
    }
}
