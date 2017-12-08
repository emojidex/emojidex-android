package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.Emoji;
import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojiManager;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ImageArchiveDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.IndexDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.MyEmojiDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.SearchDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.UTFDownloadArguments;
import com.emojidex.libemojidex.EmojiVector;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Created by kou on 15/12/24.
 */
public class EmojiDownloader
{
    static final String TAG = "EmojidexLibrary::EmojiDownloader";

    public static final int HANDLE_NULL = -1;
    private static final int DIRTY_COUNT_MAX = 500;
    private static final int SAVE_DELAY = 3000;

    private static final EmojiDownloader INSTANCE = new EmojiDownloader();

//    private final Map<String, Emoji> localJsonParams = new LinkedHashMap<String, Emoji>();

    private Context context = null;
    private EmojiManager manager = null;

    private final Set<DownloadListener> listeners = new LinkedHashSet<DownloadListener>();

    private final TaskManager taskManager = new TaskManager();

    /**
     * Get singleton instance.
     * @return      Singleton instance.
     */
    public static EmojiDownloader getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private EmojiDownloader()
    {
        // nop
    }

    /**
     * Initialize EmojiDownloader object.
     * @param context       Context
     * @param manager       Emoji manager.
     */
    public void initialize(Context context, EmojiManager manager)
    {
        this.context = context;
        this.manager = manager;
    }

    /**
     * Download UTF emojies.
     * @param arguments     Download arguments.
     * @return              Download handle.
     */
    public int downloadUTFEmoji(UTFDownloadArguments arguments)
    {
        final int handle = taskManager.registUTF(arguments);
        if(handle != HANDLE_NULL)
            taskManager.runNextTasks();
        return handle;
    }

    /**
     * Download extended emojies.
     * @param arguments     Download arguments.
     * @return              Download handle.
     */
    public int downloadExtendedEmoji(ExtendedDownloadArguments arguments)
    {
        final int handle = taskManager.registExtended(arguments);
        if(handle != HANDLE_NULL)
            taskManager.runNextTasks();
        return handle;
    }

    /**
     * Download index emojies.
     * @param arguments     Download arguments.
     * @return              Download handle.
     */
    public int downloadIndexEmoji(IndexDownloadArguments arguments)
    {
        final int handle = taskManager.registIndex(arguments);
        if(handle != HANDLE_NULL)
            taskManager.runNextTasks();
        return handle;
    }

    /**
     * Download search emojies.
     * @param arguments     Download arguments.
     * @return              Download handle.
     */
    public int downloadSearchEmoji(SearchDownloadArguments arguments)
    {
        final int handle = taskManager.registSearch(arguments);
        if(handle != HANDLE_NULL)
            taskManager.runNextTasks();
        return handle;
    }

    /**
     * Download emojies.
     * @param argumentsArray    Download arguments array.
     * @return                  Download handle array.
     */
    public int[] downloadEmojies(EmojiDownloadArguments... argumentsArray)
    {
        final int[] handles = new int[argumentsArray.length];
        boolean doRun = false;

        for(int i = argumentsArray.length - 1;  i >= 0;  --i)
        {
            final EmojiDownloadArguments arguments = argumentsArray[i];
            handles[i] = taskManager.registEmoji(arguments);
            if(handles[i] != HANDLE_NULL)
                doRun = true;
        }

        if(doRun)
            taskManager.runNextTasks();

        return handles;
    }

    /**
     * Download emoji images.
     * @param argumentsArray    Download arguments array.
     * @return                  Download handle array.
     */
    public int[] downloadImages(ImageDownloadArguments... argumentsArray)
    {
        return downloadImages(false, argumentsArray);
    }

    /**
     * Download emoji images.
     * @param ignoreArchive     Ignore archive file flag,
     * @param argumentsArray    Download arguments array.
     * @return                  Download handle array.
     */
    public int[] downloadImages(boolean ignoreArchive, ImageDownloadArguments... argumentsArray)
    {
        final int[] handles = new int[argumentsArray.length];
        boolean doRun = false;
        boolean doDownloadArchive = false;

        for(int i = argumentsArray.length - 1;  i >= 0;  --i)
        {
            final ImageDownloadArguments arguments = argumentsArray[i];
            final Emoji emoji = manager.getEmoji(arguments.getEmojiName());

            // SKip if illegal arguments.
            // Skip if image is already newest.
            if(     emoji == null
                ||  emoji.hasNewestImage(arguments.getFormat())  )
            {
                handles[i] = HANDLE_NULL;
                continue;
            }

            // Download archive if emoji is utf and not found image.
            if(     !ignoreArchive
                &&  emoji.getType().equals("utf")   )
            {
                handles[i] = HANDLE_NULL;
                if( !doDownloadArchive )
                {
                    final Uri uri = EmojidexFileUtils.getLocalEmojiUri(emoji.getCode(), arguments.getFormat());
                    if( !EmojidexFileUtils.existsLocalFile(uri) )
                    {
                        handles[i] = taskManager.registImageArchive(
                                new ImageArchiveDownloadArguments()
                                        .setFormat(arguments.getFormat()),
                                context
                        );
                        doDownloadArchive = true;
                        doRun = doRun || (handles[i] != HANDLE_NULL);
                        continue;
                    }
                }
                else
                    continue;
            }

            // Download image.
            handles[i] = taskManager.registImage(arguments, context);
            doRun = doRun || (handles[i] != HANDLE_NULL);
        }
        if( doRun )
            taskManager.runNextTasks();
        return handles;
    }

    /**
     * Download my emojies.
     * @param arguments     Download arguments.
     * @return              Download handle.
     */
    public int downloadMyEmoji(MyEmojiDownloadArguments arguments)
    {
        final int handle = taskManager.registMyEmoji(arguments);
        if(handle != HANDLE_NULL)
            taskManager.runNextTasks();
        return handle;
    }

    /**
     * Cancel download emoji task.
     * @param handle    Download handle.
     */
    public void cancelDownload(int handle)
    {
        taskManager.cancelTask(handle);
    }

    /**
     * Add download event listener.
     * @param listener      Listener.
     */
    public void addListener(DownloadListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Remove download event listener.
     * @param listener      Listener.
     */
    public void removeListener(DownloadListener listener)
    {
        listeners.remove(listener);
    }

    /**
     * Get initialized flag.
     * @return  true if EmojiDownloader object is initialized.
     */
    public boolean isInitialized()
    {
        return context != null;
    }

    /**
     * Finish task.
     * @param handle        Task handle.
     */
    void finishTask(int handle)
    {
        taskManager.finishTask(handle);
    }

    /**
     * Update emojidex database.
     * @param handle    Download handle.
     * @param emojies   Download json parameters.
     * @param type      Task type.
     */
    void updateDatabase(int handle, EmojiVector emojies, TaskType type)
    {
        if(emojies.size() == 0)
            return;

        // Update parameters.
        String typeString;
        if(type == TaskType.UTF)
            typeString = "utf";
        else if(type == TaskType.EXTENDED)
            typeString = "extended";
        else
            typeString = "unknown";

        manager.updateEmojies(typeString, emojies);
    }

    /**
     * Update emoji checksum.
     * @param emojiName     Emoji name.
     * @param format        Emoji format.
     */
    void updateChecksums(String emojiName, EmojiFormat format)
    {
        manager.updateChecksum(emojiName, format);
    }

    /**
     * Notify interface.
     */
    interface NotifyInterface
    {
        void notify(DownloadListener listener);
    }

    /**
     * Notify download event to listener.
     * @param ni    Notify interface.
     */
    void notifyToListener(NotifyInterface ni)
    {
        for(DownloadListener listener : listeners.toArray(new DownloadListener[listeners.size()]))
            ni.notify(listener);
    }
}
