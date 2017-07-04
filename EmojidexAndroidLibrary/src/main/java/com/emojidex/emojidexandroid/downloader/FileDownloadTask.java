package com.emojidex.emojidexandroid.downloader;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * File download task.
 */
class FileDownloadTask extends AbstractDownloadTask<AbstractFileDownloadExecutor> {
    private final ArrayList<AbstractFileDownloadExecutor> executors = new ArrayList<AbstractFileDownloadExecutor>();
    private final FileDownloadTaskListener listener;

    /**
     * Construct object.
     * @param downloader    Emoji downloader.
     * @param listener      Download event listener.
     */
    public FileDownloadTask(EmojiDownloader downloader, FileDownloadTaskListener listener)
    {
        super(downloader);

        this.listener = listener;
    }

    /**
     * Add file download executor.
     * @param executor      File download executor.
     */
    public void add(AbstractFileDownloadExecutor executor)
    {
        executors.add(executor);
    }

    /**
     * Start download.
     * @param executor
     * @return
     */
    public AsyncTask start(Executor executor)
    {
        if(executors.isEmpty())
            return null;
        return executeOnExecutor(executor, executors.toArray(new AbstractFileDownloadExecutor[executors.size()]));
    }

    @Override
    protected void onPreExecute()
    {
        listener.onStart();

        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(EmojiDownloader.Result result)
    {
        super.onPostExecute(result);

        listener.onFinish(result);
    }

    /**
     * Event listener of FileDownloadTask.
     */
    interface FileDownloadTaskListener
    {
        /**
         * Start download task.
         */
        void onStart();

        /**
         * Finish download task.
         * @param result    Download result.
         */
        void onFinish(EmojiDownloader.Result result);
    }
}
