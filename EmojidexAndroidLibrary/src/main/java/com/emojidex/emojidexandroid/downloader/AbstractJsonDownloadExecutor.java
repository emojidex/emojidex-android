package com.emojidex.emojidexandroid.downloader;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Json download executor.
 */
abstract class AbstractJsonDownloadExecutor extends AbstractDownloadExecutor
{
    private final DownloadConfig config;
    private final JsonDownloadTask parentTask;
    private final CustomFileDownloadTaskListener listener = new CustomFileDownloadTaskListener();

    private final ArrayList<String> emojiNames = new ArrayList<String>();
    private final ArrayList<FileDownloadTask> tasks = new ArrayList<FileDownloadTask>();

    private final Handler finishHandler;
    private final EmojiDownloader.Result resultTotal = new EmojiDownloader.Result();

    /**
     * Construct object.
     * @param downloader    Emoji downloader.
     * @param config        Download config.
     * @param parentTask    Parent task.
     */
    public AbstractJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask)
    {
        super(downloader);

        this.config = config;
        this.parentTask = parentTask;

        finishHandler = new Handler() {
            @Override
            public void handleMessage(Message msg)
            {
                AbstractJsonDownloadExecutor.this.parentTask.onFinish(resultTotal);
            }
        };
    }

    /**
     * Get download config.
     * @return      Download config.
     */
    protected DownloadConfig getConfig()
    {
        return config;
    }

    /**
     * Get parent task.
     * @return      Parent task.
     */
    protected JsonDownloadTask getParentTask()
    {
        return parentTask;
    }

    /**
     * Create libemojidex client.
     * @return      Libemojidex client.
     */
    protected Client createClient()
    {
        // Create client.
        final Client client = new Client();

        // Login if has username and auth_token.
        final String username = config.getUserName();
        final String authtoken = config.getAuthToken();
        if(     username != null && !username.isEmpty()
                &&  authtoken != null && !authtoken.isEmpty()   )
        {
            client.getUser().authorize(username, authtoken);
        }

        return client;
    }

    /**
     * Download emoji images.
     * @param emojies   Emoji names.
     * @return          true if need download.
     */
    protected boolean downloadEmojies(EmojiVector emojies)
    {
        // Create download emoji images list.
        tasks.addAll(createEmojiDownloadTasks(emojies));

        // Skip if have no executor.
        if(tasks.isEmpty())
            return false;

        // Update local json and reload database.
        getDownloader().updateDatabase(getParentTask());

        // Start download emoji images.
        for(FileDownloadTask task : tasks)
            task.start(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    protected List<FileDownloadTask> createEmojiDownloadTasks(EmojiVector emojies)
    {
        final ArrayList<FileDownloadTask> result = new ArrayList<FileDownloadTask>();
        final EmojiDownloader downloader = getDownloader();
        final int threadCount = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).getCorePoolSize();
        int current = 0;

        for(int i = 0;  i < emojies.size();  ++i)
        {
            final Emoji emoji = emojies.get(i);
            emojiNames.add(emoji.getCode());

            final AbstractFileDownloadExecutor executor = downloader.createEmojiDownloadExecutor(emoji, config, getParentTask());
            if(executor == null)
                continue;
            if(result.size() <= current)
                result.add(new FileDownloadTask(downloader, listener));
            result.get(current).add(executor);
            current = (current + 1) % threadCount;
        }

        return result;
    }

    /**
     * Get FileDownloadTask event listener.
     * @return      FileDownloadTask event listener.
     */
    protected CustomFileDownloadTaskListener getListener()
    {
        return listener;
    }

    @Override
    public int download()
    {
        final Collection collection = downloadJson();
        final EmojiVector emojies = collection.all();

        // Notify to listener.
        final String[] emojiNames = new String[(int)emojies.size()];
        for(int i = 0;  i < emojies.size();  ++i)
            emojiNames[i] = emojies.get(i).getCode();
        getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
            @Override
            public void notify(DownloadListener listener)
            {
                listener.onDownloadJson(getParentTask().getHandle(), emojiNames);
            }
        });

        // Start download emoji images.
        if( !downloadEmojies(emojies) )
            finishHandler.sendMessage(finishHandler.obtainMessage());

        return emojies.size() > 0 ? 1 : 0;
    }

    @Override
    protected int getDownloadCount()
    {
        return 1;
    }

    /**
     * Download json.
     * @return      Emoji collection.
     */
    protected abstract Collection downloadJson();

    /**
     * Custom event listener of FileDownloadTask.
     */
    private class CustomFileDownloadTaskListener implements FileDownloadTask.FileDownloadTaskListener
    {
        private int finishCount = 0;

        @Override
        public void onStart()
        {
            // nop
        }

        @Override
        public void onFinish(EmojiDownloader.Result result)
        {
            resultTotal.add(result);

            if(++finishCount == tasks.size())
            {
                tasks.clear();
                parentTask.onFinish(resultTotal);
            }
        }
    }
}
