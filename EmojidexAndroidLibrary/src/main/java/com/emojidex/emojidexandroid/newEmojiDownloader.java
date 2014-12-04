package com.emojidex.emojidexandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kou on 14/11/26.
 */
public class newEmojiDownloader {
    static final String TAG = Emojidex.TAG + "::EmojiDownloader";

    private final Context context;
    private final Result resultTotal = new Result();

    private newDownloadListener listener = new newDownloadListener();
    private int runningThreadCount = 0;

    private ArrayList<JsonParam> localJsonParams;
    private final HashMap<String, JsonParam> localJsonParamMap = new HashMap<String, JsonParam>();

    private final ArrayList<DownloadParam>[] downloadEmojiesArray;
    private int nextThreadIndex = 0;
    private int downloadEmojiCount = 0;

    /**
     * Construct EmojiDownloader object.
     * @param context   Context.
     */
    public newEmojiDownloader(Context context)
    {
        this(context, 8);
    }

    /**
     * Construct EmojiDownloader object.
     * @param context       Context.
     * @param threadCount   Download thread count.
     */
    public newEmojiDownloader(Context context, int threadCount)
    {
        // Initialize fields.
        this.context = context.getApplicationContext();
        downloadEmojiesArray = new ArrayList[threadCount];
        for(int i = 0;  i < downloadEmojiesArray.length;  ++i)
            downloadEmojiesArray[i] = new ArrayList<DownloadParam>();

        // Read local json.
        final File file = new File(PathUtils.getLocalJsonPath());
        localJsonParams = readJson(file);

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
        task.execute(downloadParam);
    }

    /**
     * Add download task from JsonParam objects.
     * @param jsonParams    JsonParam objects.
     * @param formats       Format of download images.
     * @param sourceRootPath    Root path of download image files.
     */
    public void add(ArrayList<JsonParam> jsonParams, EmojiFormat[] formats, String sourceRootPath)
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

            // Add download task.
            for(EmojiFormat format : formats)
            {
                // If file already downloaded, ignore file.
                if(format == EmojiFormat.SVG)
                {
                    final String localChecksum = localJsonParam.checksums.svg;
                    final String remoteChecksum = jsonParam.checksums.svg;
                    if( remoteChecksum == null || remoteChecksum.equals(localChecksum) )
                        continue;
                    localJsonParam.checksums.svg = remoteChecksum;
                }
                else
                {
                    final String resolution = format.getRelativeDir();
                    final String localChecksum = localJsonParam.checksums.png.get(resolution);
                    final String remoteChecksum = jsonParam.checksums.png.get(resolution);
                    if( remoteChecksum == null || remoteChecksum.equals(localChecksum) )
                        continue;
                    localJsonParam.checksums.png.put(resolution, remoteChecksum);
                }

                // Add download task.
                if(downloadParam == null)
                {
                    // Copy json parameter to local.
                    localJsonParam.name = jsonParam.name;
                    localJsonParam.text = jsonParam.text;
                    localJsonParam.category = jsonParam.category;
                    localJsonParam.name_ja = jsonParam.name_ja;

                    // Create DownloadParam.
                    downloadParam = new DownloadParam();
                    downloadParam.name = localJsonParam.name;

                    // Add download task.
                    downloadEmojiesArray[nextThreadIndex].add(downloadParam);
                    nextThreadIndex = (nextThreadIndex + 1) % downloadEmojiesArray.length;
                }
                final FileParam fileParam = new FileParam();
                fileParam.source = PathUtils.getRemoteEmojiPath(localJsonParam.name, format, sourceRootPath);
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
        // Error check.
        if(downloadEmojiCount == 0)
            return;

        // Update local json file.
        writeJson();

        // Notify to listener.
        listener.onPreAllEmojiDownload();

        // Start download task.
        for(ArrayList<DownloadParam> downloadParams : downloadEmojiesArray)
        {
            if(downloadParams.isEmpty())
                continue;

            final EmojiDownloadTask task = new EmojiDownloadTask();
            task.execute(downloadParams.toArray(new DownloadParam[downloadParams.size()]));
        }
    }

    /**
     * Set download event listener.
     * @param listener      Listener.(If value is null, set default listener.)
     */
    public void setListener(newDownloadListener listener)
    {
        this.listener = (listener == null) ? new newDownloadListener() : listener;
    }

    /**
     * Read json parameter.
     * @param file  Json file.
     * @return      Json parameter.
     */
    private ArrayList<JsonParam> readJson(File file)
    {
        final ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            final InputStream is = new FileInputStream(file);
            final TypeReference<ArrayList<JsonParam>> typeReference = new TypeReference<ArrayList<JsonParam>>(){};
            final ArrayList<JsonParam> result = objectMapper.readValue(is, typeReference);
            is.close();
            return result;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // If read failed, return ArrayList of empty.
        return new ArrayList<JsonParam>();
    }

    private void writeJson()
    {
        final File file = new File(PathUtils.getLocalJsonPath());
        if( !file.getParentFile().exists() )
            file.getParentFile().mkdirs();

        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            final OutputStream os = new FileOutputStream(file);
            objectMapper.writeValue(os, localJsonParams);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
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
        private int failed = 0;
        private int total = 0;
    }

    /**
     * Download task.
     */
    private abstract class AbstractDownloadTask extends AsyncTask<DownloadParam, Void, Result>
    {
        private final byte[] buffer = new byte[4096];

        private long startTime, endTime;

        @Override
        protected void onPreExecute()
        {
            ++runningThreadCount;
            startTime = System.currentTimeMillis();
            Log.d(TAG, "Task start.(runningThreadCount = " + runningThreadCount + ")");
        }

        @Override
        protected Result doInBackground(DownloadParam... params)
        {
            final Result result = new Result();

            for(DownloadParam downloadParam : params)
            {
                // Add total count.
                result.total += downloadParam.fileParams.size();

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
                        ++result.failed;
                        e.printStackTrace();
                    }
                    catch(IOException e)
                    {
                        ++result.failed;
                        e.printStackTrace();
                    }
                    catch(HttpException e)
                    {
                        ++result.failed;
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
            onTaskCompleted(result);
        }

        @Override
        protected void onCancelled(Result result)
        {
            onTaskCompleted(result);
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

        protected void onTaskCompleted(Result result)
        {
            --runningThreadCount;

            endTime = System.currentTimeMillis();
            putResultLog(result, "Download task end.(runningThreadCount = " + runningThreadCount + ")");

            resultTotal.succeeded += result.succeeded;
            resultTotal.failed += result.failed;
            resultTotal.total += result.total;
        }

        /**
         * Put result log.
         * @param result    Download result.
         */
        protected void putResultLog(Result result, String preMessage)
        {
            Log.d(TAG, preMessage + " : "
                            + "(S" + result.succeeded + " + "
                            + "F" + result.failed + " = "
                            + (result.succeeded + result.failed) + ") / "
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

        public JsonDownloadTask(EmojiFormat[] formats, String sourceRootPath)
        {
            this.formats = formats;
            this.sourceRootPath = sourceRootPath;
        }

        @Override
        protected void onPreDownload(DownloadParam downloadParam) {
            // Notify to listener.
            listener.onPreOneJsonDownload();
        }

        @Override
        protected void onPostDownload(DownloadParam downloadParam)
        {
            // Find new emojies.
            for(FileParam fileParam : downloadParam.fileParams)
            {
                // Read json parameter.
                final File file = new File(fileParam.destination);
                final ArrayList<JsonParam> jsonParams = readJson(file);

                // Add download task.
                add(jsonParams, formats, sourceRootPath);

                // Clean temporary file.
                file.delete();
            }

            // Notify to listener.
            listener.onPostOneJsonDownload();
        }

        @Override
        protected void onTaskCompleted(Result result) {
            super.onTaskCompleted(result);

            if(runningThreadCount <= 0)
            {
                // Notify to listener.
                listener.onPostAllJsonDownload(newEmojiDownloader.this);

                // Put log if downloadEmojiCount equal 0.
                if(downloadEmojiCount == 0)
                    putResultLog(resultTotal, "All download task end.");
            }
        }
    }

    /**
     * Download task for emoji files.
     */
    private class EmojiDownloadTask extends AbstractDownloadTask
    {
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

        @Override
        protected void onTaskCompleted(Result result) {
            super.onTaskCompleted(result);

            // Notify to listener.
            if(runningThreadCount == 0)
            {
                // Notify to listener.
                listener.onPostAllEmojiDownload();

                // Put log.
                putResultLog(resultTotal, "All download task end.");
            }
        }
    }
}
