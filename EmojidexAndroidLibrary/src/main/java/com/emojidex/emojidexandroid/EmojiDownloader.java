package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;
import com.emojidex.libemojidex.Emojidex.Service.QueryOpts;
import com.emojidex.libemojidex.Emojidex.Service.User;
import com.emojidex.libemojidex.StringVector;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import javax.net.ssl.HttpsURLConnection;


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

    private final EmojiArchiveDownloadTask emojiArchiveDownloadTask;
    private final EmojiDownloadTask[] emojiDownloadTasks;
    private int nextThreadIndex = 0;
    private int downloadEmojiCount = 0;

    private Context context = null;

    /**
     * Construct EmojiDownloader object.
     * @param context   Context.
     */
    public EmojiDownloader(Context context)
    {
        this(context, null, null, 0);
    }

    /**
     * Construct EmojiDownloader object.
     * @param context       Context
     * @param username      User name.
     * @param authtoken     Auth token.
     */
    public EmojiDownloader(Context context, String username, String authtoken)
    {
        this(context, username, authtoken, 0);
    }

    /**
     * Construct EmojiDownloader object.
     * @param context       Context
     * @param threadCount   Download thread count.
     */
    public EmojiDownloader(Context context, int threadCount)
    {
        this(context, null, null, threadCount);
    }

    /**
     * Construct EmojiDownloader object.
     * @param context       Context
     * @param username      User name.
     * @param authtoken     Auth token.
     * @param threadCount   Download thread count.
     */
    public EmojiDownloader(Context context, String username, String authtoken, int threadCount)
    {
        this.context = context;

        // Initialize download thread count.
        emojiArchiveDownloadTask = new EmojiArchiveDownloadTask();
        if(threadCount <= 0)
            threadCount = Math.max(((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).getCorePoolSize() - 1, 1);
        emojiDownloadTasks = new EmojiDownloadTask[threadCount];
        for(int i = 0;  i < threadCount;  ++i)
            emojiDownloadTasks[i] = new EmojiDownloadTask();

        // Read local json.
        final ArrayList<JsonParam> jsonParams = JsonParam.readFromFile(
                context,
                EmojidexFileUtils.getLocalJsonUri()
        );

        for(JsonParam param : jsonParams)
            localJsonParams.put(param.name, param);

        // Create libemojidex object.
        client = new Client();
        if(     username != null && !username.isEmpty()
            &&  authtoken != null && !authtoken.isEmpty() )
        {
            final User user = client.getUser();
            user.authorize(username, authtoken);
        }
        locale = Locale.getDefault().equals(Locale.JAPAN) ? "ja" : "en";
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     */
    public void downloadIndex(DownloadConfig config)
    {
        downloadIndex(config, 50);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     */
    public void downloadIndex(DownloadConfig config, final int limit)
    {
        downloadIndex(config, limit, 1);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     * @param startpage     Start page.(value >= 1)
     */
    public void downloadIndex(DownloadConfig config, final int limit, final int startpage)
    {
        downloadIndex(config, limit, startpage, startpage);
    }

    /**
     * Download index emojies.
     * @param config        Download config.
     * @param limit         Emoji count of one page.
     * @param startpage     Start page.(value >= 1)
     * @param endpage       End page.
     */
    public void downloadIndex(DownloadConfig config, final int limit, final int startpage, final int endpage)
    {
        final JsonDownloadTask task = new JsonDownloadTask(config);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, task.new AbstractJsonDownloadExecutor() {
            @Override
            protected Collection downloadJson()
            {
                final Collection collection = client.getIndexes().emoji(startpage, limit, true);
                for(int i = startpage + 1;  i < (endpage + 1);  ++i)
                    collection.more();
                return collection;
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
            protected void downloadEmojies(EmojiVector emojies)
            {
                ArrayList<EmojiFormat> firstFormats = new ArrayList<EmojiFormat>();
                Iterator<EmojiFormat> it = task.config.getFormats().iterator();
                while(it.hasNext())
                {
                    final EmojiFormat format = it.next();
                    if( !EmojidexFileUtils.existsLocalEmojiFormatDirectory(format) )
                    {
                        it.remove();
                        firstFormats.add(format);
                    }
                }

                super.downloadEmojies(emojies);

                if(firstFormats.size() > 0)
                {
                    addDownloadEmojiArchive(emojies, firstFormats, task.config);
                }
            }

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
                options.limit(0x7FFFFFFF);
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
                context,
                EmojidexFileUtils.getLocalJsonUri(),
                localJsonParams.values()
        );

        // Error check.
        if(downloadEmojiCount <= 0 || !runningTasks.isEmpty())
        {
            // Notify to listener.
            listener.onFinish(resultTotal);
            return;
        }

        // Notify to listener.
        listener.onPreAllEmojiDownload();

        // Start download task
        emojiArchiveDownloadTask.start(AsyncTask.THREAD_POOL_EXECUTOR);
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
        EmojiDownloadTask.FileDownloadExecutor executor = null;
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
                final EmojiDownloadTask task = emojiDownloadTasks[nextThreadIndex];
                executor = task.new FileDownloadExecutor(emojiName);
                task.add(executor);
                nextThreadIndex = (nextThreadIndex + 1) % emojiDownloadTasks.length;
            }
            executor.add(
                    uri,
                    EmojidexFileUtils.getRemoteEmojiPath(emojiName, format, config.getSourceRootPath())
            );
        }

        // Add download emoji count.
        if(executor != null)
            downloadEmojiCount += executor.getDownloadCount();
    }

    /**
     * Add emoji archive download task.
     * @param emojies   Emoji parameters.
     * @param formats   Emoji formats.
     * @param config    Download config.
     */
    private void addDownloadEmojiArchive(EmojiVector emojies, ArrayList<EmojiFormat> formats, DownloadConfig config)
    {
        // Copy emoji parameters.
        for(int i = 0;  i < emojies.size();  ++i)
        {
            final Emoji emoji = emojies.get(i);
            final JsonParam localParam = findLocalParam(emoji.getCode());

            copyParam(localParam, emoji);

            for(EmojiFormat format : formats)
            {
                if(format == EmojiFormat.SVG)
                {
                    localParam.checksums.svg = emoji.getChecksums().getSvg();
                }
                else
                {
                    final String resolution = format.getRelativeDir();
                    localParam.checksums.png.put(
                            resolution,
                            emoji.getChecksums().sum("png", resolution)
                    );
                }
            }
        }

        // Download emoji archive.
        for(EmojiFormat format : formats)
        {
            final String resolution = format.getRelativeDir();

            final EmojiArchiveDownloadTask.FileDownloadExecutor executor = emojiArchiveDownloadTask.new FileDownloadExecutor(resolution);

            executor.add(
                    Uri.parse("file:" + EmojidexFileUtils.getTemporaryPath()),
                    EmojidexFileUtils.getRemoteEmojiArchivePath(format, config.getSourceRootPath())
            );
            emojiArchiveDownloadTask.add(executor);

            ++downloadEmojiCount;
        }
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
        dest.score = src.getScore();

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
        boolean existsFile = EmojidexFileUtils.existsLocalFile(EmojidexFileUtils.getLocalEmojiUri(local.name, format));

        // Check checksums.
        // If emoji format is svg.
        if(format == EmojiFormat.SVG)
        {
            final String localChecksum = local.checksums.svg;
            final String remoteChecksum = remote.getChecksums().sum("svg", null);

            // Check.
            if(     existsFile
                &&  (   remoteChecksum == null
                ||  remoteChecksum.equals(localChecksum)   ))
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
            if(     existsFile
                &&  (   remoteChecksum == null
                ||  remoteChecksum.equals(localChecksum)   ))
                return true;

            // Update checksum.
            local.checksums.png.put(resolution, remoteChecksum);
        }

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
                            + "(S" + result.getSucceededCount() + " + "
                            + "F" + result.getFailedCount() + ") / "
                            + result.getTotalCount() + " : "
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
        private ArrayList<String> emojiNames = new ArrayList<String>();

        /**
         * Construct json download task.
         * @param config    Download config.
         */
        public JsonDownloadTask(DownloadConfig config)
        {
            this.config = config.clone();
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
            listener.onPostOneJsonDownload(emojiNames);
        }

        /**
         * Json download executor.
         */
        public abstract class AbstractJsonDownloadExecutor extends AbstractDownloadTask.AbstractDownloadExecutor
        {
            @Override
            public int download()
            {
                final Collection collection = downloadJson();
                final EmojiVector emojies = collection.all();
                downloadEmojies(emojies);
                return emojies.size() > 0 ? 1 : 0;
            }

            @Override
            protected int getDownloadCount()
            {
                return 1;
            }

            protected void downloadEmojies(EmojiVector emojies)
            {
                for(int i = 0;  i < emojies.size();  ++i)
                {
                    final Emoji emoji = emojies.get(i);
                    emoji.setCode( emoji.getCode().replaceAll(" ", "_") );
                    addDownloadEmoji(emoji, config);
                    emojiNames.add(emoji.getCode());
                }
            }

            protected abstract Collection downloadJson();
        }
    }

    private abstract class AbstractFileDownloadTask extends AbstractDownloadTask<AbstractFileDownloadTask.FileDownloadExecutor>
    {
        private final byte[] buffer = new byte[4096];
        private final ArrayList<FileDownloadExecutor> executors = new ArrayList<FileDownloadExecutor>();

        public void add(FileDownloadExecutor executor)
        {
            executors.add(executor);
        }

        public AsyncTask start(Executor executor)
        {
            if(executors.isEmpty())
                return null;
            return executeOnExecutor(executor, executors.toArray(new FileDownloadExecutor[executors.size()]));
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
                listener.onFinish(resultTotal);
            }
        }

        /**
         * File download executor.
         */
        public class FileDownloadExecutor extends AbstractDownloadTask.AbstractDownloadExecutor
        {
            private final String fileName;
            private final ArrayList<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();

            /**
             * Construct file download executor.
             * @param fileName      Download file name.
             */
            public FileDownloadExecutor(String fileName)
            {
                this.fileName = fileName;
            }

            public void add(Uri dest, String src)
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

                        final HttpURLConnection connection = (HttpsURLConnection)url.openConnection();
                        connection.setAllowUserInteraction(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestMethod("GET");
                        //connection.setSSLSocketFactory(HttpURLConnection.getDefaultSSLSocketFactory());
                        connection.connect();

                        if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                        {
                            // Create directory if destination uri is file and not found directory.
                            if(info.dest.getScheme().equals("file"))
                            {
                                final File parentDir = new File(info.dest.getPath()).getParentFile();
                                if( !parentDir.exists() )
                                    parentDir.mkdirs();
                            }

                            // Download file.
                            final OutputStream os = context.getContentResolver().openOutputStream(info.dest);

                            final DataInputStream dis = new DataInputStream(connection.getInputStream());
                            final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

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
            protected class DownloadInfo
            {
                public String src;
                public Uri dest;
            }
        }
    }

    private class EmojiDownloadTask extends AbstractFileDownloadTask
    {
        @Override
        protected void onPreDownload(FileDownloadExecutor executor)
        {
            listener.onPreOneEmojiDownload(executor.fileName);
        }

        @Override
        protected void onPostDownload(FileDownloadExecutor executor)
        {
            listener.onPostOneEmojiDownload(executor.fileName);
        }
    }

    private class EmojiArchiveDownloadTask extends EmojiDownloadTask
    {
        @Override
        protected void onPreDownload(FileDownloadExecutor executor)
        {
        }

        @Override
        protected void onPostDownload(FileDownloadExecutor executor)
        {
            for(FileDownloadExecutor.DownloadInfo info : executor.downloadInfos)
            {
                final File xzFile = new File(info.dest.getPath());

                try
                {
                    // xz -> tar
                    XZCompressorInputStream xzIn = new XZCompressorInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(xzFile)
                            )
                    );
                    final File tarFile = new File(EmojidexFileUtils.getTemporaryPath());
                    final FileOutputStream tarOut = new FileOutputStream(tarFile);

                    final byte[] buffer = new byte[4096];
                    int n = 0;
                    while( (n = xzIn.read(buffer)) != -1)
                        tarOut.write(buffer, 0, n);

                    tarOut.close();
                    xzIn.close();

                    // tar -> files
                    final EmojiFormat format = EmojiFormat.toFormat(executor.fileName);
                    final TarArchiveInputStream tarIn = new TarArchiveInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(tarFile)
                            )
                    );

                    for(TarArchiveEntry entry = tarIn.getNextTarEntry();  entry != null;  entry = tarIn.getNextTarEntry())
                    {
                        String basename = entry.getName();
                        final int extPos = basename.lastIndexOf('.');
                        if(extPos != -1)
                            basename = basename.substring(0, extPos);
                        final Uri destUri = EmojidexFileUtils.getLocalEmojiUri(basename, format);

                        OutputStream os = context.getContentResolver().openOutputStream(destUri);
                        IOUtils.copy(tarIn, os);
                        os.close();
                    }

                    tarFile.delete();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

                xzFile.delete();
            }
            listener.onPostOneEmojiArchiveDownload(executor.fileName);
        }
    }
}
