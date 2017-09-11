package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.os.Handler;

import com.emojidex.emojidexandroid.Emoji;
import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.VersionManager;
import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.IndexDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.SearchDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.UTFDownloadArguments;
import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.StringVector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

    private final Map<String, Emoji> localJsonParams = new LinkedHashMap<String, Emoji>();

    private Context context = null;

//    private final Map<Integer, AbstractDownloadTask> tasks = new TreeMap<Integer, AbstractDownloadTask>();
    private final Set<DownloadListener> listeners = new LinkedHashSet<DownloadListener>();

    private final TaskManager taskManager = new TaskManager();

    private int dirtyCount = 0;
    private final Handler saveHandler = new Handler();
    private final Runnable saveRunnable = new Runnable(){
        @Override
        public void run()
        {
            saveJson();
            dirtyCount = 0;
        }
    };

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
     */
    public void initialize(Context context)
    {
        this.context = context;

        reload();
    }

    /**
     * Read local json.
     */
    public void reload()
    {
        localJsonParams.clear();

        final ArrayList<Emoji> jsonParams = EmojidexFileUtils.readJsonFromFile(EmojidexFileUtils.getLocalJsonUri());

        for(Emoji param : jsonParams)
            localJsonParams.put(param.getCode(), param);
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
        final int[] handles = new int[argumentsArray.length];
        boolean doRun = false;

        for(int i = argumentsArray.length - 1;  i >= 0;  --i)
        {
            final ImageDownloadArguments arguments = argumentsArray[i];
            final Emoji emoji = localJsonParams.get(arguments.getEmojiName());

            // SKip if illegal arguments.
            // Skip if image is already newest.
            if(     emoji == null
                ||  emoji.hasNewestImage(arguments.getFormat())  )
            {
                handles[i] = HANDLE_NULL;
                continue;
            }

            // Download image.
            handles[i] = taskManager.registImage(arguments, context);
            if(handles[i] != HANDLE_NULL)
                doRun = true;
        }
        if( doRun )
            taskManager.runNextTasks();
        return handles;
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

        // Copy parameters.
        for(int i = 0;  i < emojies.size();  ++i)
        {
            final com.emojidex.libemojidex.Emojidex.Data.Emoji emoji = emojies.get(i);
            final Emoji localParam = findLocalParam(emoji.getCode());
            copyParam(localParam, emoji);

            // Log task type when utf or extended.
            if(type == TaskType.UTF)
                localParam.setType("utf");
            else if(type == TaskType.EXTENDED)
                localParam.setType("extended");
        }

        // Overwrite local json file.
        saveJson();
    }

    /**
     * Update emoji checksum.
     * @param emojiName     Emoji name.
     * @param format        Emoji format.
     */
    void updateChecksums(String emojiName, EmojiFormat format)
    {
        final Emoji emoji = findLocalParam(emojiName);
        emoji.getCurrentChecksums().set(
                format,
                emoji.getChecksums().get(format)
        );

        saveHandler.removeCallbacks(saveRunnable);
        if(++dirtyCount >= DIRTY_COUNT_MAX)
        {
            saveJson();
            dirtyCount = 0;
        }
        else
        {
            saveHandler.postDelayed(saveRunnable, SAVE_DELAY);
        }
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

    /**
     * Find local json parameter object.
     * If object is not found, create new object.
     * @param emojiName     Emoji name.
     * @return              Local json parameter object.
     */
    private Emoji findLocalParam(String emojiName)
    {
        Emoji result = localJsonParams.get(emojiName);
        if(result == null)
        {
            result = new Emoji();
            localJsonParams.put(emojiName, result);
        }
        return result;
    }

    /**
     * Copy parameter to dest from src.
     * @param dest  Destination.
     * @param src   Source.
     */
    private void copyParam(Emoji dest, com.emojidex.libemojidex.Emojidex.Data.Emoji src)
    {
        dest.setCode(src.getCode());
        dest.setMoji(src.getMoji());
        dest.setUnicode(src.getUnicode());
        dest.setCategory(src.getCategory());

        // tags
        {
            final StringVector srcTags = src.getTags();
            final long tagsCount = srcTags.size();
            List<String> destTags = dest.getTags();
            if(destTags == null)
                destTags = new ArrayList<String>();
            else
                destTags.clear();
            for(int i = 0;  i < tagsCount;  ++i)
                destTags.add(srcTags.get(i));
            dest.setTags(destTags);
        }


        dest.setLink(src.getLink());
        dest.setBase(src.getBase());

        // variants
        {
            final StringVector srcVariants = src.getVariants();
            final long variantsCount = srcVariants.size();
            List<String> destVariants = dest.getVariants();
            if(destVariants == null)
                destVariants = new ArrayList<String>();
            else
                destVariants.clear();
            for(int i = 0;  i < variantsCount;  ++i)
                destVariants.add(srcVariants.get(i));
            dest.setVariants(destVariants);
        }

        dest.setScore(src.getScore());
        dest.setCurrentPrice(src.getCurrent_price());
        dest.setPrimary(src.getPrimary());
        dest.setRegisteredAt(src.getRegistered_at());
        dest.setPermalock(src.getPermalock());
        dest.setCopyrightLock(src.getCopyright_lock());
        dest.setLinkExpiration(src.getLink_expiration());
        dest.setLockExpiration(src.getLock_expiration());
        dest.setTimesChanged(src.getTimes_changed());
        dest.setWide(src.getIs_wide());
        dest.setTimesUsed(src.getTimes_used());
        dest.setAttribution(src.getAttribution());
        dest.setUserID(src.getUser_id());

        // checksums
        {
            final com.emojidex.libemojidex.Emojidex.Data.Checksums srcChecksums = src.getChecksums();
            final Emoji.Checksums destChecksums = dest.getChecksums();

            destChecksums.setSvg(srcChecksums.sum("svg", ""));

            for(EmojiFormat format : EmojiFormat.values())
            {
                if(format == EmojiFormat.SVG)
                    continue;

                destChecksums.setPng(
                        format,
                        srcChecksums.sum("png", format.getResolution())
                );
            }
        }

        dest.setFavorited(src.getFavorited());
    }

    /**
     * Save json file.
     */
    private void saveJson()
    {
        // Overwrite local json file.
        EmojidexFileUtils.writeJsonToFile(
                EmojidexFileUtils.getLocalJsonUri(),
                localJsonParams.values()
        );
        VersionManager.getInstance().save(context);

        // Reload emojidex.
        Emojidex.getInstance().reload();

        // Notify to listeners.
        for(DownloadListener listener : listeners)
            listener.onUpdateDatabase();
    }
}
