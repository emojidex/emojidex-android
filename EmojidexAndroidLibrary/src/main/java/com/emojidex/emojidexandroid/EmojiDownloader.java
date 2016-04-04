package com.emojidex.emojidexandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;
import com.emojidex.libemojidex.Emojidex.Service.QueryOpts;
import com.emojidex.libemojidex.StringVector;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by kou on 15/12/24.
 */
public class EmojiDownloader
{
    static final String TAG = Emojidex.TAG + "::EmojiDownloader";

    private final Result resultTotal = new Result();
    private DownloadListener listener = new DownloadListener();
    private final ArrayList<AbstractDownloadTask> runningTasks = new ArrayList<AbstractDownloadTask>();

    private final LinkedHashMap<String, JsonParam> localJsonParams = new LinkedHashMap<String, JsonParam>();

    private final Client client;
    private final String locale;

    private final EmojiDownloadTask[] emojiDownloadTasks;
    private int nextThreadIndex = 0;
    private int downloadEmojiCount = 0;

    /**
     * Construct EmojiDownloader object.
     */
    public EmojiDownloader()
    {
        this(0);
    }

    /**
     * Construct EmojiDownloader object.
     * @param threadCount   Download thread count.
     */
    public EmojiDownloader(int threadCount)
    {
        // Initialize download thread count.
        if(threadCount <= 0)
            threadCount = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).getCorePoolSize();
        emojiDownloadTasks = new EmojiDownloadTask[threadCount];
        for(int i = 0;  i < threadCount;  ++i)
            emojiDownloadTasks[i] = new EmojiDownloadTask();

        // Read local json.
        final File file = new File(PathUtils.getLocalJsonPath());
        final ArrayList<JsonParam> jsonParams = JsonParam.readFromFile(file);

        for(JsonParam param : jsonParams)
            localJsonParams.put(param.name, param);

        // Create libemojidex object.
        client = new Client();
        locale = Locale.getDefault().equals(Locale.JAPAN) ? "ja" : "en";
    }

    /**
     * Download index emojies.
     * @param config    Download config.
     */
    public void downloadIndex(DownloadConfig config)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                return client.getIndexes().emoji(0, 50, true);
            }
        });
    }

    /**
     * Download UTF emojies.
     * @param config    Download config.
     */
    public void downloadUTFEmoji(DownloadConfig config)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                return client.getIndexes().utfEmoji(locale, true);
            }
        });
    }

    /**
     * Download extended emojies.
     * @param config    Download config.
     */
    public void downloadExtendedEmoji(DownloadConfig config)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                return client.getIndexes().extendedEmoji(locale, true);
            }
        });
    }

    public void downloadSearchEmoji(String word, DownloadConfig config)
    {
        downloadSearchEmoji(word, null, config);
    }

    public void downloadSearchEmoji(final String word, final String category, DownloadConfig config)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                final QueryOpts options = new QueryOpts();
                options.detailed(true);
                if(category != null)
                    options.category(category);
                return client.getSearch().term(word, options);
            }
        });
    }

    public void downloadEmoji(final String name, DownloadConfig config)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                final Emoji emoji = client.getSearch().find(name, true);
                final Collection collection = new Collection();
                collection.add(emoji);
                return collection;
            }
        });
    }

    /**
     * Start download emoji task.
     */
    public void startDownload()
    {
        // Update local json file.
        JsonParam.writeToFile(
                new File(PathUtils.getLocalJsonPath()),
                localJsonParams.values()
        );

        // Error check.
        if(downloadEmojiCount <= 0 || !runningTasks.isEmpty())
        {
            // Notify to listener.
            listener.onFinish();
            return;
        }

        // Notify to listener.
        listener.onPreAllEmojiDownload();

        // Start download task.
        for(EmojiDownloadTask task : emojiDownloadTasks)
            task.start(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Cancel download emoji task.
     */
    public void cancelDownload()
    {
        for(AbstractDownloadTask task : runningTasks)
            task.cancel(true);
    }

    /**
     * Set download event listener.
     * @param listener      Listener.(If value is null, set default listener.)
     */
    public void setListener(DownloadListener listener)
    {
        this.listener = (listener == null) ? new DownloadListener() : listener;
    }

    /**
     * Get has download task flag.
     * @return
     */
    public boolean hasDownloadTask()
    {
        return downloadEmojiCount > 0;
    }

    /**
     * Check state.
     * @return  Return true when idle.
     */
    public boolean isIdle()
    {
        return runningTasks.isEmpty();
    }

    /**
     * Add download task.
     * @param emoji     Emoji name.
     * @param config    Download config.
     */
    private void addDownloadEmoji(Emoji emoji, DownloadConfig config)
    {
        // Skip if not found checksums..
        if(emoji.getChecksums() == null)
            return;

        // Find jsonParam from local data.
        final String emojiName = emoji.getCode();
        JsonParam localParam = findLocalParam(emojiName);

        // Copy json parameter to local.
        copyParam(localParam, emoji);

        // Add download task.
        EmojiDownloadTask.EmojiDownloadExecutor executor = null;
        for(EmojiFormat format : config.getFormats())
        {
            // Skip if emoji is already downloaded.
            if(     !config.getForceFlag()
                &&  isAlreadyDownloaded(localParam, emoji, format))
                continue;

            // Delete old file.
            final File file = new File(PathUtils.getLocalEmojiPath(emojiName, format));
            file.delete();

            // Add download task.
            if(executor == null)
            {
                final EmojiDownloadTask task = emojiDownloadTasks[nextThreadIndex];
                executor = task.new EmojiDownloadExecutor(emojiName);
                task.add(executor);
                nextThreadIndex = (nextThreadIndex + 1) % emojiDownloadTasks.length;
            }
            executor.add(
                    PathUtils.getLocalEmojiPath(emojiName, format),
                    PathUtils.getRemoteEmojiPath(emojiName, format, config.getSourceRootPath())
            );
        }

        // Add download emoji count.
        if(executor != null)
            downloadEmojiCount += executor.getDownloadCount();
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
            result.checksums = new JsonParam.Checksums();
            result.checksums.png = new HashMap<String, String>();
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
        dest.name = src.getCode();
        dest.text = src.getMoji();
        dest.category = src.getCategory();
        dest.base = src.getBase();

        final StringVector variants = src.getVariants();
        final long variantsCount = variants.size();
        if(dest.variants == null)
            dest.variants = new ArrayList<String>();
        dest.variants.clear();
        for(int i = 0;  i < variantsCount;  ++i)
            dest.variants.add(variants.get(i));
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
        // Check file exists.
        final File file = new File(PathUtils.getLocalEmojiPath(local.name, format));
        if( !file.exists() )
            return false;

        // Check checksums.
        // If emoji format is svg.
        if(format == EmojiFormat.SVG)
        {
            final String localChecksum = local.checksums.svg;
            final String remoteChecksum = remote.getChecksums().sum("svg", null);

            // Check.
            if(     remoteChecksum == null
                ||  remoteChecksum.equals(localChecksum)    )
                return true;

            // Update checksums.
            local.checksums.svg = remoteChecksum;
        }
        // If emoji format is png.
        else
        {
            final String resolution = format.getRelativeDir();
            final String localChecksum = local.checksums.png.get(resolution);
            final String remoteChecksum = remote.getChecksums().sum("png", resolution);

            // Check.
            if(     remoteChecksum == null
                ||  remoteChecksum.equals(localChecksum)    )
                return true;

            // Update checksum.
            local.checksums.png.put(resolution, remoteChecksum);
        }

        return false;
    }

    /**
     * Download result.
     */
    private static class Result
    {
        private int succeeded = 0;
        private int total = 0;
    }

    /**
     * Download task.
     */
    private abstract class AbstractDownloadTask<Executor extends AbstractDownloadTask.AbstractDownloadExecutor> extends AsyncTask<Executor, Void, Result>
    {
        private long startTime, endTime;

        @Override
        protected void onPreExecute()
        {
            runningTasks.add(this);
            startTime = System.currentTimeMillis();
            Log.d(TAG, "Task start.(runningThreadCount = " + runningTasks.size() + ")");
        }

        @Override
        protected Result doInBackground(Executor... params)
        {
            final Result result = new Result();

            for(Executor param : params)
            {
                result.total += param.getDownloadCount();

                // Skip if download cancelled.
                if(isCancelled())
                    continue;

                // Download file.
                onPreDownload(param);
                result.succeeded += param.download();
                onPostDownload(param);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Result result)
        {
            runningTasks.remove(this);
            endTime = System.currentTimeMillis();
            if(result != null)
            {
                putResultLog(result, "Download task end.(runningThreadCount = " + runningTasks.size() + ")");
                resultTotal.succeeded += result.succeeded;
                resultTotal.total += result.total;
            }
        }

        @Override
        protected void onCancelled(Result result)
        {
            runningTasks.remove(this);
            endTime = System.currentTimeMillis();

            if(result != null)
            {
                putResultLog(result, "Download task cancelled.(runningThreadCount = " + runningTasks.size() + ")");
                resultTotal.succeeded += result.succeeded;
                resultTotal.total += result.total;
            }
            if(runningTasks.isEmpty())
            {
                listener.onCancelled();
                putResultLog(resultTotal, "All download task end.");
            }
        }

        /**
         * Called when before download.
         * @param executor  Download executor.
         */
        protected abstract void onPreDownload(Executor executor);

        /**
         * Called when after download.
         * @param executor  Download executor.
         */
        protected abstract void onPostDownload(Executor executor);

        /**
         * Put result log.
         * @param result    Download result.
         */
        protected void putResultLog(Result result, String preMessage)
        {
            Log.d(TAG, preMessage + " : "
                            + "(S" + result.succeeded + " + "
                            + "F" + (result.total-result.succeeded) + ") / "
                            + result.total + " : "
                            + ((endTime-startTime) / 1000.0) + "sec"
            );
        }

        /**
         * Download executor.
         */
        public abstract class AbstractDownloadExecutor
        {
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
    }

    /**
     * Json download task.
     */
    private class JsonDownloadTask extends AbstractDownloadTask<JsonDownloadTask.AbstractJsonDownloadExecutor>
    {
        private final DownloadConfig config;
        private Collection collection;

        /**
         * Construct json download task.
         * @param config    Download config.
         */
        public JsonDownloadTask(DownloadConfig config)
        {
            this.config = config;
        }

        @Override
        protected void onPostExecute(Result result)
        {
            super.onPostExecute(result);

            if(runningTasks.isEmpty())
            {
                // Notify to listener.
                listener.onPostAllJsonDownload(EmojiDownloader.this);

                // Put log if new emoji is nothing.
                if(downloadEmojiCount == 0)
                    putResultLog(resultTotal, "All download task end.");
            }
        }

        @Override
        protected void onPreDownload(AbstractJsonDownloadExecutor executor)
        {
            listener.onPreOneJsonDownload();
        }

        @Override
        protected void onPostDownload(AbstractJsonDownloadExecutor executor)
        {
            listener.onPostOneJsonDownload(collection);
        }

        /**
         * Json download executor.
         */
        public abstract class AbstractJsonDownloadExecutor extends AbstractDownloadTask.AbstractDownloadExecutor
        {
            @Override
            public int download()
            {
                collection = downloadJson();
                final EmojiVector emojies = collection.all();
                for(int i = 0;  i < emojies.size();  ++i)
                {
                    final Emoji emoji = emojies.get(i);
                    emoji.setCode( emoji.getCode().replaceAll(" ", "_") );
                    addDownloadEmoji(emoji, config);
                }
                return 1;
            }

            @Override
            protected int getDownloadCount()
            {
                return 1;
            }

            protected abstract Collection downloadJson();
        }
    }

    private class EmojiDownloadTask extends AbstractDownloadTask<EmojiDownloadTask.EmojiDownloadExecutor>
    {
        private final byte[] buffer = new byte[4096];
        private final ArrayList<EmojiDownloadExecutor> executors = new ArrayList<EmojiDownloadExecutor>();

        public void add(EmojiDownloadExecutor executor)
        {
            executors.add(executor);
        }

        public AsyncTask start(Executor executor)
        {
            if(executors.isEmpty())
                return null;
            return executeOnExecutor(executor, executors.toArray(new EmojiDownloadExecutor[executors.size()]));
        }

        @Override
        protected void onPostExecute(Result result)
        {
            super.onPostExecute(result);

            if(runningTasks.isEmpty())
            {
                // Notify to listener.
                listener.onPostAllEmojiDownload();

                // Put log.
                putResultLog(resultTotal, "All download task end.");

                // Notify to listener.
                listener.onFinish();
            }
        }

        @Override
        protected void onPreDownload(EmojiDownloadExecutor executor)
        {
            listener.onPreOneEmojiDownload(executor.emojiName);
        }

        @Override
        protected void onPostDownload(EmojiDownloadExecutor executor)
        {
            listener.onPostOneEmojiDownload(executor.emojiName);
        }

        /**
         * Emoji download executor.
         */
        public class EmojiDownloadExecutor extends AbstractDownloadTask.AbstractDownloadExecutor
        {
            private final String emojiName;
            private final ArrayList<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();

            /**
             * Construct download executor.
             * @param emojiName     Download emoji name.
             */
            public EmojiDownloadExecutor(String emojiName)
            {
                this.emojiName = emojiName;
            }

            public void add(String dest, String src)
            {
                final DownloadInfo info = new DownloadInfo();
                info.dest = dest;
                info.src = src;
                downloadInfos.add(info);
            }

            @Override
            public int download()
            {
                int succeeded = 0;
                for(DownloadInfo info : downloadInfos)
                {
                    try
                    {
                        final URL url = new URL(info.src);

                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setAllowUserInteraction(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestMethod("GET");
                        connection.connect();

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                        {
                            final File destFile = new File(info.dest);
                            final File destParentDir = destFile.getParentFile();
                            if( !destParentDir.exists() )
                                destParentDir.mkdirs();

                            final DataInputStream dis = new DataInputStream(connection.getInputStream());
                            final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));

                            int readByte;
                            while( (readByte = dis.read(buffer)) != -1 )
                                dos.write(buffer, 0, readByte);

                            dis.close();
                            dos.close();

                            ++succeeded;
                        }
                    }
                    catch(MalformedURLException e)
                    {
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                return succeeded;
            }

            @Override
            protected int getDownloadCount()
            {
                return downloadInfos.size();
            }

            /**
             * Download information.
             */
            private class DownloadInfo
            {
                public String src;
                public String dest;
            }
        }
    }
}
