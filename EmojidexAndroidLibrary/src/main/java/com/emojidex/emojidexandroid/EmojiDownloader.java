package com.emojidex.emojidexandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by kou on 14/11/26.
 */
public class EmojiDownloader {
    static final String TAG = Emojidex.TAG + "::EmojiDownloader";

    private final Context context;
    private final Result resultTotal = new Result();

    private DownloadListener listener = new DownloadListener();
    private final ArrayList<AbstractDownloadTask> runningTasks = new ArrayList<AbstractDownloadTask>();

    private ArrayList<JsonParam> localJsonParams;
    private final HashMap<String, JsonParam> localJsonParamMap = new HashMap<String, JsonParam>();

    private final ArrayList<DownloadParam>[] downloadEmojiesArray;
    private int nextThreadIndex = 0;
    private int downloadEmojiCount = 0;

    /**
     * Download state.
     */
    public enum State
    {
        IDLE,
        DOWNLOAD,
        ;
    }

    /**
     * Construct EmojiDownloader object.
     * @param context   Context.
     */
    public EmojiDownloader(Context context)
    {
        this(context, 0);
    }

    /**
     * Construct EmojiDownloader object.
     * @param context       Context.
     * @param threadCount   Download thread count.
     */
    public EmojiDownloader(Context context, int threadCount)
    {
        if(threadCount <= 0)
            threadCount = ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).getCorePoolSize();

        // Initialize fields.
        this.context = context.getApplicationContext();
        downloadEmojiesArray = new ArrayList[threadCount];
        for(int i = 0;  i < downloadEmojiesArray.length;  ++i)
            downloadEmojiesArray[i] = new ArrayList<DownloadParam>();

        // Read local json.
        final File file = new File(PathUtils.getLocalJsonPath());
        localJsonParams = JsonParam.readFromFile(file);

        for(JsonParam jsonParam: localJsonParams)
            localJsonParamMap.put(jsonParam.name, jsonParam);
    }

    /**
     * Add download task from json file.
     * @param jsonPath      Json file path.
     * @param formats       Format of download images.
     */
    public void add(String jsonPath, EmojiFormat[] formats)
    {
        add(jsonPath, formats, null);
    }

    /**
     * Add download task from json file.
     * @param jsonPath          Json file path.
     * @param formats           Format of download images.
     * @param sourceRootPath    Root path of download image files.
     */
    public void add(String jsonPath, EmojiFormat[] formats, String sourceRootPath)
    {
        add(jsonPath, formats, sourceRootPath, false);
    }

    /**
     * Add download task from json file.
     * @param jsonPath          Json file path.
     * @param formats           Format of download images.
     * @param sourceRootPath    Root path of download image files.
     * @param forceDownload     Force download flag.
     */
    public void add(String jsonPath, EmojiFormat[] formats, String sourceRootPath, boolean forceDownload)
    {
        // If sourceRootPath is null, create it from jsonPath.
        if(sourceRootPath == null)
        {
            try
            {
                final URI uri = URI.create(jsonPath);
                final File file = new File(uri.getPath());
                final URI parent = new URI(uri.getScheme(), uri.getHost(), file.getParent(), uri.getFragment());
                sourceRootPath = parent.toString();
            }
            catch(URISyntaxException e)
            {
                e.printStackTrace();
            }
        }

        // Add download task from json.
        final FileParam fileParam = new FileParam();
        fileParam.source = jsonPath;
        fileParam.destination = context.getExternalCacheDir() + "/" + System.currentTimeMillis();

        final DownloadParam downloadParam = new DownloadParam();
        downloadParam.name = jsonPath;
        downloadParam.fileParams.add(fileParam);

        final JsonDownloadTask task = new JsonDownloadTask(formats, sourceRootPath);
        task.forceDownload = forceDownload;
        task.result.total += downloadParam.fileParams.size();
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, downloadParam);
    }

    /**
     * Add download task from JsonParam objects.
     * @param jsonParams    JsonParam objects.
     * @param formats       Format of download images.
     * @param sourceRootPath    Root path of download image files.
     */
    public void add(ArrayList<JsonParam> jsonParams, EmojiFormat[] formats, String sourceRootPath)
    {
        add(jsonParams, formats, sourceRootPath, false);
    }

    /**
     * Add download task from JsonParam objects.
     * @param jsonParams    JsonParam objects.
     * @param formats       Format of download images.
     * @param sourceRootPath    Root path of download image files.
     * @param forceDownload     Force download flag.
     */
    public void add(ArrayList<JsonParam> jsonParams, EmojiFormat[] formats, String sourceRootPath, boolean forceDownload)
    {
        for(JsonParam jsonParam : jsonParams)
        {
            if(jsonParam.checksums == null)
                continue;

            DownloadParam downloadParam = null;

            // Find jsonParam from local data.
            // If jsonParam is not found, add new data to local data.
            JsonParam localJsonParam = localJsonParamMap.get(jsonParam.name);
            if(localJsonParam == null)
            {
                localJsonParam = new JsonParam();
                localJsonParams.add(localJsonParam);
                localJsonParamMap.put(jsonParam.name, localJsonParam);
            }
            if(localJsonParam.checksums == null)
                localJsonParam.checksums = new JsonParam.Checksums();
            if(localJsonParam.checksums.png == null)
                localJsonParam.checksums.png = new HashMap<String, String>();

            // Copy json parameter to local.
            localJsonParam.name = jsonParam.name;
            localJsonParam.text = jsonParam.text;
            localJsonParam.category = jsonParam.category;
//            localJsonParam.name_ja = jsonParam.name_ja;
            localJsonParam.variants = jsonParam.variants;
            localJsonParam.base = jsonParam.base;

            // Add download task.
            for(EmojiFormat format : formats)
            {
                // Check need download.
                if( !forceDownload && !checkNeedDownload(jsonParam, localJsonParam, format) )
                    continue;

                // Delete old file when need download.
                final File file = new File(PathUtils.getLocalEmojiPath(localJsonParam.name, format));
                file.delete();

                // Add download task.
                if(downloadParam == null)
                {
                    // Create DownloadParam.
                    downloadParam = new DownloadParam();
                    downloadParam.name = localJsonParam.name;

                    // Add download task.
                    downloadEmojiesArray[nextThreadIndex].add(downloadParam);
                    nextThreadIndex = (nextThreadIndex + 1) % downloadEmojiesArray.length;
                }
                final FileParam fileParam = new FileParam();
                String emojiName = localJsonParam.name;
                if (PathUtils.getLocaleString().equals("ja"))
                {
                    try
                    {
                        emojiName = URLEncoder.encode(localJsonParam.name, "UTF-8");
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                } else
                {
                    emojiName = emojiName.replaceAll(" ", "_");
                }
                fileParam.source = PathUtils.getRemoteEmojiPath(emojiName, format, sourceRootPath);
                fileParam.destination = PathUtils.getLocalEmojiPath(localJsonParam.name, format);
                downloadParam.fileParams.add(fileParam);
                ++downloadEmojiCount;
            }
        }
    }

    /**
     * Download done.
     */
    public void download()
    {
        // Update local json file.
        JsonParam.writeToFile(new File(PathUtils.getLocalJsonPath()), localJsonParams);

        // Notify to listener.
        listener.onPreAllEmojiDownload();

        // Error check.
        if(downloadEmojiCount == 0 || !runningTasks.isEmpty())
        {
            // Notify to listener.
            listener.onFinish();

            return;
        }

        // Start download task.
        for(ArrayList<DownloadParam> downloadParams : downloadEmojiesArray)
        {
            if(downloadParams.isEmpty())
                continue;

            final EmojiDownloadTask task = new EmojiDownloadTask();
            for(DownloadParam param : downloadParams)
                task.result.total += param.fileParams.size();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadParams.toArray(new DownloadParam[downloadParams.size()]));
        }
    }

    /**
     * Cancel download.
     */
    public void cancel()
    {
        for(AbstractDownloadTask task : runningTasks)
        {
            task.cancel(true);
        }
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
     * @return  true if has download task.
     */
    public boolean hasDownloadTask()
    {
        return downloadEmojiCount > 0;
    }

    /**
     * Get state.
     * @return  State.
     */
    public State getState()
    {
        return runningTasks.isEmpty() ? State.IDLE : State.DOWNLOAD;
    }

    /**
     * Check need download.
     * @param newJsonParam      New json parameter.
     * @param localJsonParam    Local json paramter.
     * @param format            Emoji format.
     * @return                  Return true if need download.
     */
    private boolean checkNeedDownload(JsonParam newJsonParam, JsonParam localJsonParam, EmojiFormat format)
    {
        final File file = new File(PathUtils.getLocalEmojiPath(localJsonParam.name, format));

//if(downloadEmojiCount < 18*3)
//{
//    file.delete();
//    return true;
//}

        // If file already downloaded, ignore file.
        if(format == EmojiFormat.SVG)
        {
            final String localChecksum = localJsonParam.checksums.svg;
            final String remoteChecksum = newJsonParam.checksums.svg;
            if( file.exists() &&
                (remoteChecksum == null || remoteChecksum.equals(localChecksum)) )
                return false;
            localJsonParam.checksums.svg = remoteChecksum;
        }
        else
        {
            final String resolution = format.getRelativeDir();
            final String localChecksum = localJsonParam.checksums.png.get(resolution);
            final String remoteChecksum = newJsonParam.checksums.png.get(resolution);
            if( file.exists() &&
                (remoteChecksum == null || remoteChecksum.equals(localChecksum)) )
                return false;
            localJsonParam.checksums.png.put(resolution, remoteChecksum);
        }

        return true;
    }


    /**
     * Download parameter.
     */
    private static class DownloadParam
    {
        private String name;
        final private ArrayList<FileParam> fileParams = new ArrayList<FileParam>();
    }

    /**
     * File parameter.
     */
    private static class FileParam
    {
        private String source;
        private String destination;
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
    private abstract class AbstractDownloadTask extends AsyncTask<DownloadParam, Void, Result>
    {
        public final Result result = new Result();

        private final byte[] buffer = new byte[4096];

        private long startTime, endTime;

        @Override
        protected void onPreExecute()
        {
            resultTotal.total += result.total;
            runningTasks.add(this);
            startTime = System.currentTimeMillis();
            Log.d(TAG, "Task start.(runningThreadCount = " + runningTasks.size() + ")");
        }

        @Override
        protected Result doInBackground(DownloadParam... params)
        {
            for(DownloadParam downloadParam : params)
            {
                // Skip if download cancelled.
                if(isCancelled())
                    continue;

                // Download files.
                onPreDownload(downloadParam);
                for(FileParam fileParam : downloadParam.fileParams)
                {
                    try
                    {
                        final URL url = new URL(fileParam.source);

                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setAllowUserInteraction(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestMethod("GET");
                        connection.connect();

                        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                            throw new HttpException();

                        final File destination = new File(fileParam.destination);
                        final File destinationParent = destination.getParentFile();
                        if( !destinationParent.exists() )
                            destinationParent.mkdirs();

                        final DataInputStream dis = new DataInputStream(connection.getInputStream());
                        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));

                        int readByte;
                        while( (readByte = dis.read(buffer)) != -1 )
                        {
                            dos.write(buffer, 0, readByte);
                        }

                        dis.close();
                        dos.close();

                        ++result.succeeded;
                    }
                    catch(MalformedURLException e)
                    {
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                    catch(HttpException e)
                    {
                        e.printStackTrace();
                    }
                }
                onPostDownload(downloadParam);
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
            }

            if(runningTasks.isEmpty())
            {
                listener.onCancelled();
                putResultLog(resultTotal, "All download task end.");
            }
        }

        /**
         * Called when before download.
         * @param downloadParam     Download parameter.
         */
        protected void onPreDownload(DownloadParam downloadParam)
        {
            // nop
        }

        /**
         * Called when after download.
         * @param downloadParam     Download parameter.
         */
        protected void onPostDownload(DownloadParam downloadParam)
        {
            // nop
        }

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
    }

    /**
     * Download task for json files.
     */
    private class JsonDownloadTask extends AbstractDownloadTask
    {
        private final EmojiFormat[] formats;
        private final String sourceRootPath;

        private boolean forceDownload = false;

        public JsonDownloadTask(EmojiFormat[] formats, String sourceRootPath)
        {
            this.formats = formats;
            this.sourceRootPath = sourceRootPath;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);

            if(runningTasks.isEmpty())
            {
                // Notify to listener.
                listener.onPostAllJsonDownload(EmojiDownloader.this);

                // Put log if downloadEmojiCount equal 0.
                if(downloadEmojiCount == 0)
                    putResultLog(resultTotal, "All download task end.");
            }
        }

        @Override
        protected void onPreDownload(DownloadParam downloadParam) {
            for(FileParam fileParam : downloadParam.fileParams)
            {
                // Notify to listener.
                listener.onPreOneJsonDownload(fileParam.source, fileParam.destination);
            }
        }

        @Override
        protected void onPostDownload(DownloadParam downloadParam)
        {
            // Find new emojies.
            for(FileParam fileParam : downloadParam.fileParams)
            {
                // Notify to listener.
                listener.onPostOneJsonDownload(fileParam.source, fileParam.destination);

                // Read json parameter.
                final File file = new File(fileParam.destination);
                final ArrayList<JsonParam> jsonParams = JsonParam.readFromFile(file);

                // Add download task.
                add(jsonParams, formats, sourceRootPath, forceDownload);

                // Clean temporary file.
                file.delete();
            }
        }
    }

    /**
     * Download task for emoji files.
     */
    private class EmojiDownloadTask extends AbstractDownloadTask
    {
        @Override
        protected void onPostExecute(Result result) {
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
        protected void onPreDownload(DownloadParam downloadParam) {
            // Notify to listener.
            listener.onPreOneEmojiDownload(downloadParam.name);
        }

        @Override
        protected void onPostDownload(DownloadParam downloadParam) {
            // Norify to listener.
            listener.onPostOneEmojiDownload(downloadParam.name);
        }
    }
}
