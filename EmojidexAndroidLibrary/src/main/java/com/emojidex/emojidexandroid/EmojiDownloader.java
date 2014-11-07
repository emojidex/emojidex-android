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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by kou on 14/10/03.
 */
class EmojiDownloader {
    static final String TAG = Emojidex.TAG + "::EmojiDownloader";

    private final Context context;
    private final DownloadConfig config = new DownloadConfig();
    private final Result downloadResult = new Result();

    private int runningThreadCount = 0;

    /**
     * Construct EmojiDownloader object.
     * @param context   Context of application.
     */
    public EmojiDownloader(Context context)
    {
        this.context = context.getApplicationContext();
    }

    /**
     * Download emoji.
     * @param config    Configuration of download.
     */
    public void download(DownloadConfig config)
    {
        // Now runnning.
        if(runningThreadCount > 0)
        {
            Log.d(TAG, "Download task is already running.");
            return;
        }

        // Reset fields.
        downloadResult.succeeded = 0;
        downloadResult.failed = 0;
        downloadResult.total = 0;

        // Set parameter from config.
        this.config.copy(config);
        if(this.config.listener == null)
            this.config.listener = new DownloadListener();
        this.config.threadCount = Math.max(this.config.threadCount, 1);

        // Create json file list.
        final int kindCount = this.config.kinds.size();
        final FileInfo[] fileInfos = new FileInfo[kindCount];
        for(int i = 0;  i< kindCount;  ++i)
        {
            fileInfos[i] = new FileInfo();
            fileInfos[i].name = PathUtils.JSON_FILENAME;
            fileInfos[i].kind = this.config.kinds.get(i);
        }

        // Download start.
        final JsonDownloadTask task = new JsonDownloadTask();
        task.execute(fileInfos);
    }

    /**
     * Create temporary path.
     * @param kind  Emoji kind.
     * @return      Temporary path.
     */
    private String getTemporaryJsonPath(String kind)
    {
        return context.getExternalCacheDir() + "/" + kind + "/" + PathUtils.JSON_FILENAME;
    }

    /**
     * File information.
     */
    private static class FileInfo
    {
        protected String name;
        protected String kind;
    }

    /**
     * File information for Emoji.
     */
    private static class EmojiFileInfo extends FileInfo
    {
        protected final ArrayList<EmojiFormat> formats = new ArrayList<EmojiFormat>();
    }

    /**
     * Download result.
     */
    private static class Result
    {
        private int succeeded = 0;
        private int failed = 0;
        private int total = 0;

        public int getSucceededCount() { return succeeded; }
        public int getFailedCount() { return failed; }
        public int getTotalCount() { return total; }
    }

    /**
     * Download task.
     */
    private abstract class AbstractDownloadTask extends AsyncTask<FileInfo,Void,Result>
    {
        private final byte[] buffer = new byte[4096];

        private long startTime, endTime;

        protected class PathInfo
        {
            public String destination;
            public String source;
        }

        @Override
        protected void onPreExecute() {
            ++runningThreadCount;
            startTime = System.currentTimeMillis();
            Log.d(TAG, "Task start.(runningThreadCount = " + runningThreadCount + ")");
        }

        @Override
        protected Result doInBackground(FileInfo... params) {
            final Result result = new Result();

            for(FileInfo fileInfo : params)
            {
                PathInfo[] pathInfos = createPathInfos(fileInfo);

                // Add total count.
                result.total += pathInfos.length;

                // Download cancelled.
                if(isCancelled())
                    continue;

                // Download files.
                for(PathInfo pathInfo : pathInfos)
                {
                    try
                    {
                        final URL url = new URL(pathInfo.source);

                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setAllowUserInteraction(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestMethod("GET");
                        connection.connect();

                        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                            throw new HttpException();

                        final File destinationFile = new File(pathInfo.destination);
                        if( !destinationFile.getParentFile().exists() )
                            destinationFile.getParentFile().mkdirs();

                        final DataInputStream dis = new DataInputStream(connection.getInputStream());
                        final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)));

                        int readByte;
                        while( (readByte = dis.read(buffer)) != -1 )
                        {
                            dos.write(buffer, 0, readByte);
                        }

                        dis.close();
                        dos.close();

                        ++result.succeeded;
                    }
                    catch(Exception e)
                    {
                        ++result.failed;
                        e.printStackTrace();
                    }
                }
                onDownloadCompleted(fileInfo);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            finalize(result);
        }

        @Override
        protected void onCancelled(Result result) {
            finalize(result);
        }

        /**
         * Called when file download completed.
         * @param fileInfo  Downloaded file information.
         */
        protected void onDownloadCompleted(FileInfo fileInfo)
        {
            // nop
        }

        protected abstract PathInfo[] createPathInfos(FileInfo fileInfo);

        /**
         * Finalize download task.
         * @param result    Download result.
         */
        private void finalize(Result result)
        {
            --runningThreadCount;

            // Put log.
            endTime = System.currentTimeMillis();
            putResultLog(result, "Task end.(runningThreadCount = " + runningThreadCount + ")");

            // Add all result.
            downloadResult.succeeded += result.succeeded;
            downloadResult.failed += result.failed;
            downloadResult.total += result.total;

            // Check all completed.
            if(runningThreadCount <= 0)
            {
                config.listener.onAllDownloadCompleted();
                putResultLog(downloadResult, "All task end.");
            }
        }

        /**
         * Put result log.
         * @param result    Download result.
         */
        private void putResultLog(Result result, String preMessage)
        {
            Log.d(TAG, preMessage + " : "
                            + "(S" + result.getSucceededCount() + " + "
                            + "F" + result.getFailedCount() + " = "
                            + (result.getSucceededCount() + result.getFailedCount()) + ") / "
                            + result.getTotalCount() + " : "
                            + ((endTime-startTime) / 1000.0) + "sec"
            );
        }
    }

    /**
     * Download task for json files.
     */
    private class JsonDownloadTask extends AbstractDownloadTask
    {
        private final ArrayList<FileInfo> jsonFileInfos = new ArrayList<FileInfo>();

        @Override
        protected void onPostExecute(Result result) {
            // Call listener method.
            config.listener.onJsonDownloadCompleted();

            // Load local data.
            final File localJsonFile = new File(PathUtils.getLocalJsonPath());
            final ArrayList<JsonParam> localJsonParams = readJson(localJsonFile);
            final HashMap<String, JsonParam> localJsonParamMap = new HashMap<String, JsonParam>();

            // vvvvvvvv DEBUG vvvvvvvv
            for(int i = 0;  i < Math.min(10, localJsonParams.size());  ++i)
            {
                final JsonParam jsonParam = localJsonParams.get(i);
                final String defaultFormat = Emojidex.getInstance().getDefaultFormat().getRelativeDir();
                if(i / 2 == 0)
                    jsonParam.checksums.png.remove(defaultFormat);
                else
                    jsonParam.checksums.png.put(defaultFormat, "hoge");
            }
            // ^^^^^^^^ DEBUG ^^^^^^^^

            for(JsonParam jsonParam: localJsonParams)
                localJsonParamMap.put(jsonParam.name, jsonParam);

            Log.d(TAG, localJsonFile.getAbsolutePath() + " : param count = " + localJsonParams.size());

            // Create emoji file information list.
            final ArrayList<ArrayList<FileInfo>> fileInfosArray = new ArrayList<ArrayList<FileInfo>>();
            fileInfosArray.ensureCapacity(config.threadCount);
            for(int i = 0;  i < config.threadCount;  ++i)
                fileInfosArray.add(new ArrayList<FileInfo>());

            int downloadEmojiCount = 0;
            int threadIndex = 0;
            for(FileInfo jsonFileInfo : jsonFileInfos)
            {
                final File jsonFile = new File(getTemporaryJsonPath(jsonFileInfo.kind));

                // Load json data.
                final ArrayList<JsonParam> newJsonParams = readJson(jsonFile);

                Log.d(TAG, jsonFile.getAbsolutePath() + " : param count = " + newJsonParams.size());

                // Add file information to download list.
                for(JsonParam jsonParam : newJsonParams)
                {
                    if(jsonParam.checksums == null)
                        continue;

                    EmojiFileInfo fileInfo = null;

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

                    // Add file information.
                    for(EmojiFormat format : config.formats)
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

                        // Add to list.
                        if(fileInfo == null)
                        {
                            // Copy json parameter to local.
                            localJsonParam.name = jsonParam.name;
                            localJsonParam.text = jsonParam.text;
                            localJsonParam.category = jsonParam.category;
                            localJsonParam.name_ja = jsonParam.name_ja;

                            // Create and add file information.
                            fileInfo = new EmojiFileInfo();
                            fileInfo.name = localJsonParam.name;
                            fileInfo.kind = jsonFileInfo.kind;
                            fileInfosArray.get(threadIndex).add(fileInfo);

                            // To next thread.
                            threadIndex = (threadIndex + 1) % config.threadCount;
                        }
                        fileInfo.formats.add(format);
                        ++downloadEmojiCount;
                    }
                }

                // Clean temporary file.
                jsonFile.delete();
            }

            // Emoji download start.
            if(downloadEmojiCount > 0)
            {
                // Backup old json file.
                final File tmpJsonFile = new File(getTemporaryJsonPath("."));
                tmpJsonFile.delete();
                localJsonFile.renameTo(tmpJsonFile);

                // Update local json file.
                writeJson(localJsonParams);

                // Check update start.
                if(config.listener.onPreEmojiDownload(downloadEmojiCount))
                {
                    // Delete temporary file.
                    tmpJsonFile.delete();

                    // Execute download tasks.
                    for(ArrayList<FileInfo> fileInfos : fileInfosArray)
                    {
                        if(fileInfos.isEmpty())
                            continue;

                        final EmojiDownloadTask task = new EmojiDownloadTask();
                        task.execute(fileInfos.toArray(new FileInfo[fileInfos.size()]));
                    }
                }
                else
                {
                    // Rollback local json file.
                    localJsonFile.delete();
                    tmpJsonFile.renameTo(localJsonFile);
                }
            }

            Log.d(TAG, localJsonFile.getAbsolutePath() + " : param count = " + localJsonParams.size());

            // Thread end.
            super.onPostExecute(result);
        }

        @Override
        protected void onDownloadCompleted(FileInfo fileInfo) {
            jsonFileInfos.add(fileInfo);
        }

        @Override
        protected PathInfo[] createPathInfos(FileInfo fileInfo) {
            final PathInfo[] result = new PathInfo[1];

            result[0] = new PathInfo();
            result[0].destination = getTemporaryJsonPath(fileInfo.kind);
            result[0].source = PathUtils.getRemoteJsonPath(fileInfo.kind, config.sourcePath);

            return result;
        }

        /**
         * Read json parameter.
         * @param file  Json file.
         * @return      Json parameter.
         */
        private ArrayList<JsonParam> readJson(File file)
        {
            try
            {
                final ObjectMapper objectMapper = new ObjectMapper();
                final InputStream is = new FileInputStream(file);
                final ArrayList<JsonParam> result = objectMapper.readValue(is, new TypeReference<ArrayList<JsonParam>>(){});
                is.close();
                return result;
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            return new ArrayList<JsonParam>();
        }

        /**
         * Write json parameter to local storage.
         */
        private void writeJson(ArrayList<JsonParam> jsonParams)
        {
            try
            {
                final ObjectMapper objectMapper = new ObjectMapper();
                final File file = new File(PathUtils.getLocalJsonPath());
                final OutputStream os = new FileOutputStream(file);
                objectMapper.writeValue(os, jsonParams);
                os.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Download task for emoji files.
     */
    private class EmojiDownloadTask extends AbstractDownloadTask
    {
        @Override
        protected void onDownloadCompleted(FileInfo fileInfo) {
            // Call listener method.
            config.listener.onEmojiDownloadCompleted(fileInfo.name);
        }

        @Override
        protected PathInfo[] createPathInfos(FileInfo fileInfo) {
            final EmojiFileInfo emojiFileInfo = (EmojiFileInfo)fileInfo;
            final int formatCount = emojiFileInfo.formats.size();
            final PathInfo[] result = new PathInfo[formatCount];

            for(int i = 0;  i < formatCount;  ++i)
            {
                final EmojiFormat format = emojiFileInfo.formats.get(i);
                result[i] = new PathInfo();
                result[i].destination = PathUtils.getLocalEmojiPath(emojiFileInfo.name, format);
                result[i].source = PathUtils.getRemoteEmojiPath(emojiFileInfo.name, format, emojiFileInfo.kind, config.sourcePath);
            }

            return result;
        }
    }
}
