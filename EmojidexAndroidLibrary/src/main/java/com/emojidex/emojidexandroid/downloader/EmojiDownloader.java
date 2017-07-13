package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.JsonParam;
import com.emojidex.emojidexandroid.VersionManager;
import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;
import com.emojidex.libemojidex.StringVector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Created by kou on 15/12/24.
 */
public class EmojiDownloader
{
    static final String TAG = "EmojidexLibrary::EmojiDownloader";

    public static int HANDLE_NULL = -1;

    private static final EmojiDownloader INSTANCE = new EmojiDownloader();

    private final Map<String, JsonParam> localJsonParams = new LinkedHashMap<String, JsonParam>();

    private Context context = null;

    private final Map<Integer, JsonDownloadTask> tasks = new TreeMap<Integer, JsonDownloadTask>();
    private final Set<DownloadListener> listeners = new LinkedHashSet<DownloadListener>();

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

        final ArrayList<JsonParam> jsonParams = JsonParam.readFromFile(
                context,
                EmojidexFileUtils.getLocalJsonUri()
        );

        for(JsonParam param : jsonParams)
            localJsonParams.put(param.getCode(), param);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadIndex(DownloadConfig config)
    {
        return downloadIndex(config, 50);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadIndex(DownloadConfig config, final int limit)
    {
        return downloadIndex(config, limit, 1);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     * @param startpage     Start page.(value >= 1)
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadIndex(DownloadConfig config, final int limit, final int startpage)
    {
        return downloadIndex(config, limit, startpage, startpage);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     * @param startpage     Start page.(value >= 1)
     * @param endpage       End page.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadIndex(DownloadConfig config, final int limit, final int startpage, final int endpage)
    {
        final TaskInfo taskInfo = new TaskInfo(TaskInfo.TYPE.INDEX);
        if(hasTask(taskInfo))
            return HANDLE_NULL;

        final JsonDownloadTask task = new JsonDownloadTask(this, taskInfo, config);
        task.executeOnExecutor(
                AsyncTask.SERIAL_EXECUTOR,
                new IndexJsonDownloadExecutor(this, config, task, limit, startpage, endpage)
        );

        return task.getHandle();
    }

    /**
     * Download UTF emojies.
     * @param config    Download config.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadUTFEmoji(DownloadConfig config)
    {
        final TaskInfo taskInfo = new TaskInfo(TaskInfo.TYPE.UTF);
        if(hasTask(taskInfo))
            return HANDLE_NULL;

        final JsonDownloadTask task = new JsonDownloadTask(this, taskInfo, config);
        task.executeOnExecutor(
                AsyncTask.SERIAL_EXECUTOR,
                new UTFJsonDownloadExecutor(this, config, task)
        );

        return task.getHandle();
    }

    /**
     * Download extended emojies.
     * @param config    Download config.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadExtendedEmoji(DownloadConfig config)
    {
        final TaskInfo taskInfo = new TaskInfo(TaskInfo.TYPE.EXTENDED);
        if(hasTask(taskInfo))
            return HANDLE_NULL;

        final JsonDownloadTask task = new JsonDownloadTask(this, taskInfo, config);
        task.executeOnExecutor(
                AsyncTask.SERIAL_EXECUTOR,
                new ExtendedJsonDownloadExecutor(this, config, task)
        );

        return task.getHandle();
    }

    /**
     * Search and download emojies.
     * @param word          Search word.
     * @param config        DownloadConfig.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadSearchEmoji(String word, DownloadConfig config)
    {
        return downloadSearchEmoji(word, null, config);
    }

    /**
     * Search and download emojies.
     * @param word          Search word.
     * @param category      Search category.(If value is null, search from all category.)
     * @param config        DownloadConfig.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadSearchEmoji(final String word, final String category, DownloadConfig config)
    {
        final TaskInfo taskInfo = new TaskInfo(TaskInfo.TYPE.SEARCH);
        if(hasTask(taskInfo))
            return HANDLE_NULL;

        final JsonDownloadTask task = new JsonDownloadTask(this, taskInfo, config);
        task.executeOnExecutor(
                AsyncTask.SERIAL_EXECUTOR,
                new SearchJsonDownloadExecutor(this, config, task, word, category)
        );

        return task.getHandle();
    }

    /**
     *  Download one emoji.
     * @param name          Emoji name.
     * @param config        Download config.
     * @return              Download handle.
     *                      If failed start download, return {@link EmojiDownloader#HANDLE_NULL}.
     */
    public int downloadEmoji(final String name, DownloadConfig config)
    {
        final TaskInfo taskInfo = new TaskInfo(TaskInfo.TYPE.EMOJI, name);
        if(hasTask(taskInfo))
            return HANDLE_NULL;

        final JsonDownloadTask task = new JsonDownloadTask(this, taskInfo, config);
        task.executeOnExecutor(
                AsyncTask.SERIAL_EXECUTOR,
                new EmojiJsonDownloadExecutor(this, config, task, name)
        );

        return task.getHandle();
    }

    /**
     * Cancel download emoji task.
     * @param handle    Download handle.
     */
    public void cancelDownload(int handle)
    {
        final JsonDownloadTask task = tasks.get(handle);
        if(task != null)
            task.cancel(true);
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
     * Regist download task.
     * @param handle    Download handle.
     * @param task      Download task.
     */
    void registTask(int handle, JsonDownloadTask task)
    {
        tasks.put(handle, task);
    }

    /**
     * Unregist download task.
     * @param handle    Download handle.
     */
    void unregistTask(int handle)
    {
        tasks.remove(handle);
    }

    /**
     * Check downloader has same task.
     * @param taskInfo      Task information.
     * @return              true if has same task.
     */
    private boolean hasTask(TaskInfo taskInfo)
    {
        for(JsonDownloadTask task : tasks.values())
            if(task.getTaskInfo().equals(taskInfo))
                return true;
        return false;
    }

    /**
     * Create EmojiDownloadExecutor object.
     * @param emoji         Emoji.
     * @param config        Download config.
     * @param parentTask    Parent task.
     * @return              Executor.
     *                      If emoji is newest, return null.
     */
    AbstractFileDownloadExecutor createEmojiDownloadExecutor(Emoji emoji, DownloadConfig config, JsonDownloadTask parentTask)
    {
        // Skip if not found checksums..
        if(emoji.getChecksums() == null)
            return null;

        // Find jsonParam from local data.
        final String emojiName = emoji.getCode();
        JsonParam localParam = findLocalParam(emojiName);

        // Add download task.
        EmojiDownloadExecutor executor = null;
        for(EmojiFormat format : config.getFormats())
        {
            // Skip if emoji is already downloaded.
            if(     !config.getForceFlag()
                &&  isAlreadyDownloaded(localParam, emoji, format))
                continue;

            // Delete old file.
            final Uri uri = EmojidexFileUtils.getLocalEmojiUri(emojiName, format);
            EmojidexFileUtils.deleteFiles(uri);

            // Add download task.
            if(executor == null)
            {
                // Copy json parameter to local.
                copyParam(localParam, emoji);

                // Create executor.
                executor = new EmojiDownloadExecutor(this, context, emojiName, parentTask);
            }
            executor.add(
                    uri,
                    EmojidexFileUtils.getRemoteEmojiPath(emojiName, format, config.getSourceRootPath())
            );
        }

        return executor;
    }

    /**
     * Create emoji archive download executors.
     * @param emojies       Emoji array.
     * @param formats       Emoji format array.
     * @param config        Download config.
     * @param parentTask    Parent task.
     * @return              Emoji archive download executors.
     */
    EmojiArchiveDownloadExecutor[] createEmojiArchiveDownloadExecutors(EmojiVector emojies, List<EmojiFormat> formats, DownloadConfig config, JsonDownloadTask parentTask)
    {
        // Skip if formats is empty.
        if(formats.isEmpty())
            return null;

        // Copy emoji parameters.
        final String[] emojiNames = new String[(int)emojies.size()];
        for(int i = 0;  i < emojies.size();  ++i)
        {
            final Emoji emoji = emojies.get(i);
            final JsonParam localParam = findLocalParam(emoji.getCode());

            copyParam(localParam, emoji);

            for(EmojiFormat format : formats)
            {
                localParam.getChecksums().set(
                        format,
                        (format == EmojiFormat.SVG)
                            ? emoji.getChecksums().getSvg()
                            : emoji.getChecksums().sum("png", format.getResolution())
                );
            }

            emojiNames[i] = emoji.getCode();
        }

        // Download emoji archive.
        final EmojiArchiveDownloadExecutor[] executors = new EmojiArchiveDownloadExecutor[formats.size()];
        for(int i = 0;  i < formats.size();  ++i)
        {
            final EmojiFormat format = formats.get(i);

            executors[i] = new EmojiArchiveDownloadExecutor(this, context, format, parentTask, emojiNames);
            executors[i].add(
                    Uri.parse("file:" + EmojidexFileUtils.getTemporaryPath()),
                    EmojidexFileUtils.getRemoteEmojiArchivePath(format, config.getSourceRootPath())
            );
        }

        return executors;
    }

    /**
     * Update emojidex database.
     * @param parentTask    Parent task.
     */
    void updateDatabase(JsonDownloadTask parentTask)
    {
        // Update local json file.
        JsonParam.writeToFile(
                context,
                EmojidexFileUtils.getLocalJsonUri(),
                localJsonParams.values()
        );
        VersionManager.getInstance().save(context);

        // Reload emojidex.
        Emojidex.getInstance().reload();

        // Notify to listeners.
        for(DownloadListener listener : listeners)
            listener.onUpdateDatabase(parentTask.getHandle());
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
    private JsonParam findLocalParam(String emojiName)
    {
        JsonParam result = localJsonParams.get(emojiName);
        if(result == null)
        {
            result = new JsonParam();
            localJsonParams.put(emojiName, result);
        }
        return result;
    }

    /**
     * Copy parameter to dest from src.
     * @param dest  Destination.
     * @param src   Source.
     */
    private void copyParam(JsonParam dest, Emoji src)
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

        // Skip checksums.

        dest.setFavorited(src.getFavorited());
    }

    /**
     * Check emoji is already downloaded.
     * @param local     Local parameter.
     * @param remote    Remote parameter.
     * @param format    Emoji format.
     * @return          Return true if emoji is already downloaded.
     */
    private boolean isAlreadyDownloaded(JsonParam local, Emoji remote, EmojiFormat format)
    {
        boolean existsFile = EmojidexFileUtils.existsLocalFile(EmojidexFileUtils.getLocalEmojiUri(remote.getCode(), format));

        // Check checksums.
        final String localChecksum = local.getChecksums().get(format);
        final String remoteChecksum = (format == EmojiFormat.SVG)
                ? remote.getChecksums().sum("svg", null)
                : remote.getChecksums().sum("png", format.getResolution());

        if(     existsFile
            &&  (   remoteChecksum == null
            ||  remoteChecksum.equals(localChecksum)   ))
            return true;

        local.getChecksums().set(format, remoteChecksum);

        return false;
    }

    /**
     * Download result.
     */
    public static class Result
    {
        private int succeeded = 0;
        private int total = 0;

        public int getSucceededCount()
        {
            return succeeded;
        }

        public int getFailedCount()
        {
            return total - succeeded;
        }

        public int getTotalCount()
        {
            return total;
        }

        void addSucceeded(int count)
        {
            succeeded += count;
        }

        void addTotal(int count)
        {
            total += count;
        }

        void add(Result result)
        {
            succeeded += result.succeeded;
            total += result.total;
        }
    }

}
