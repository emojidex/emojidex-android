package com.emojidex.emojidexandroid.downloader;

import android.os.AsyncTask;

import com.emojidex.emojidexandroid.downloader.arguments.ArgumentsInterface;

/**
 * Download task.
 */
abstract class AbstractDownloadTask extends AsyncTask<Void, Void, Boolean>
{
    private static int nextHandle = 0;

    private final int handle;
    private final ArgumentsInterface arguments;

    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public AbstractDownloadTask(ArgumentsInterface arguments)
    {
        handle = nextHandle++;
        this.arguments = arguments;
    }

    /**
     * Get download handle.
     * @return      Download handle.
     */
    public int getHandle()
    {
        return handle;
    }

    /**
     * Get download arguments.
     * @return      Download arguments.
     */
    public ArgumentsInterface getArguments()
    {
        return arguments;
    }

    /**
     * Get download type.
     * @return      Download task type.
     */
    public abstract TaskType getType();

    /**
     * Get downloader.
     * @return  downloader.
     */
    protected EmojiDownloader getDownloader()
    {
        return EmojiDownloader.getInstance();
    }

    @Override
    protected void onPreExecute()
    {
        // nop
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        return isCancelled() ? false : download();
    }

    @Override
    protected void onPostExecute(final Boolean result)
    {
        final EmojiDownloader downloader = getDownloader();
        downloader.finishTask(handle);
        downloader.notifyToListener(new EmojiDownloader.NotifyInterface() {
            @Override
            public void notify(DownloadListener listener)
            {
                listener.onFinish(handle, result);
            }
        });
    }

    @Override
    protected void onCancelled(final Boolean result)
    {
        final EmojiDownloader downloader = getDownloader();
        downloader.finishTask(handle);
        downloader.notifyToListener(new EmojiDownloader.NotifyInterface() {
            @Override
            public void notify(DownloadListener listener)
            {
                listener.onCancelled(handle, result);
            }
        });
    }

    /**
     * Download method.
     * @return          true if download succeeded.
     */
    protected abstract boolean download();
}
