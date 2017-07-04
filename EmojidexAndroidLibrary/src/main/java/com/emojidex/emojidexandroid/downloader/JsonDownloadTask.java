package com.emojidex.emojidexandroid.downloader;

/**
 * Json download task.
 */
class JsonDownloadTask extends AbstractDownloadTask<AbstractJsonDownloadExecutor>
{
    private static int nextHandle = 0;

    private final TaskInfo taskInfo;
    private final DownloadConfig config;
    private final int handle;

    private final EmojiDownloader.Result resultTotal = new EmojiDownloader.Result();

    /**
     * Construct json download task.
     * @param downloader    Emoji downloader.
     * @param taskInfo      Task information.
     * @param config        Download config.
     */
    public JsonDownloadTask(EmojiDownloader downloader, TaskInfo taskInfo, DownloadConfig config)
    {
        super(downloader);

        this.taskInfo = taskInfo;
        this.config = config;

        handle = nextHandle++;
    }

    /**
     * Get task information.
     * @return      Task information.
     */
    public TaskInfo getTaskInfo()
    {
        return taskInfo;
    }

    /**
     * Get download config.
     * @return      Download config.
     */
    public DownloadConfig getConfig()
    {
        return config;
    }

    /**
     * Get task handle.
     * @return      Task handle.
     */
    public int getHandle()
    {
        return handle;
    }

    /**
     * Called when finish download json and emoji images.
     * @param result    Download result.
     */
    void onFinish(EmojiDownloader.Result result)
    {
        resultTotal.add(result);

        final EmojiDownloader downloader = getDownloader();
        downloader.unregistTask(handle);
        downloader.notifyToListener(new EmojiDownloader.NotifyInterface() {
            @Override
            public void notify(DownloadListener listener)
            {
                listener.onFinish(handle, resultTotal);
            }
        });
    }

    @Override
    protected void onPreExecute()
    {
        getDownloader().registTask(handle, this);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(EmojiDownloader.Result result)
    {
        super.onPostExecute(result);

        resultTotal.add(result);
    }

    @Override
    protected void onCancelled(EmojiDownloader.Result result)
    {
        super.onCancelled(result);

        resultTotal.add(result);

        final EmojiDownloader downloader = getDownloader();
        downloader.unregistTask(handle);
        downloader.notifyToListener(new EmojiDownloader.NotifyInterface() {
            @Override
            public void notify(DownloadListener listener)
            {
                listener.onCancelled(handle, resultTotal);
            }
        });
    }
}
