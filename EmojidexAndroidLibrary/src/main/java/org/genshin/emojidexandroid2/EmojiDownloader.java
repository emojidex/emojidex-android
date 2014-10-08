package org.genshin.emojidexandroid2;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kou on 14/10/03.
 */
class EmojiDownloader {
    static final String TAG = Emojidex.TAG + "::EmojiDownloader";

    private static final String JSON_FILENAME = "emoji.json";

    private final Context context;
    private final DownloadConfig config = new DownloadConfig();

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

        // Set parameter from config.
        this.config.copy(config);
        if(this.config.listener == null)
            this.config.listener = new DownloadListener();
        this.config.threadCount = Math.max(this.config.threadCount, 1);

        // Create json file list.
        final FileInfo fileInfo = new FileInfo();
        fileInfo.name = JSON_FILENAME;
        fileInfo.files.ensureCapacity(this.config.kinds.size());
        for(String kind : this.config.kinds)
            fileInfo.files.add(PathGenerator.getJsonRelativePath(kind));

        // Download start.
        final JsonDownloadTask task = new JsonDownloadTask();
        task.execute(fileInfo);
    }

    /**
     * File information.
     */
    private static class FileInfo
    {
        private String name;
        private ArrayList<String> files = new ArrayList<String>();
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

        @Override
        protected void onPreExecute() {
            ++runningThreadCount;
            startTime = System.currentTimeMillis();
            Log.d(TAG, "Task start.(runningThreadCount = " + runningThreadCount + ")");
        }

        @Override
        protected Result doInBackground(FileInfo... params) {
            final Result result = new Result();
            final String destinationPath = PathGenerator.getLocalRootPath();

            for(FileInfo fileInfo : params)
            {
                // Add total count.
                result.total += fileInfo.files.size();

                // Download cancelled.
                if(isCancelled())
                    continue;

                // Download files.
                for(String path : fileInfo.files)
                {
                    try
                    {
                        final URL url = new URL(config.sourcePath + "/" + path);

                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setAllowUserInteraction(false);
                        connection.setInstanceFollowRedirects(true);
                        connection.setRequestMethod("GET");
                        connection.connect();

                        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                            throw new HttpException();

                        final File destinationFile = new File(destinationPath + "/" + path);
                        if( !destinationFile.getParentFile().exists() )
                            destinationFile.getParentFile().mkdirs();

                        DataInputStream dis = new DataInputStream(connection.getInputStream());
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)));

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

        /**
         * Finalize download task.
         * @param result    Download result.
         */
        private void finalize(Result result)
        {
            --runningThreadCount;

            endTime = System.currentTimeMillis();
            Log.d(TAG, "Task end.(runningThreadCount = " + runningThreadCount + ") : "
                            + "(S" + result.getSucceededCount() + " + "
                            + "F" + result.getFailedCount() + " = "
                            + (result.getSucceededCount() + result.getFailedCount()) + ") / "
                            + result.getTotalCount() + " : "
                            + ((endTime-startTime) / 1000.0) + "sec"
            );

            if(runningThreadCount <= 0)
                config.listener.onAllDownloadCompleted();
        }
    }

    /**
     * Download task for json files.
     */
    private class JsonDownloadTask extends AbstractDownloadTask
    {
        @Override
        protected void onPostExecute(Result result) {
            final String destinationPath = PathGenerator.getLocalRootPath();

            // Call listener method.
            config.listener.onJsonDownloadCompleted();

            // Create emoji file information list.
            final ArrayList<ArrayList<FileInfo>> fileInfosArray = new ArrayList<ArrayList<FileInfo>>();
            fileInfosArray.ensureCapacity(config.threadCount);
            for(int i = 0;  i < config.threadCount;  ++i)
                fileInfosArray.add(new ArrayList<FileInfo>());

            int threadIndex = 0;
            for(String kind : config.kinds)
            {
                try
                {
                    // Load emoji parameter from json.
                    final ObjectMapper objectMapper = new ObjectMapper();
                    final File file = new File(destinationPath, PathGenerator.getJsonRelativePath(kind));
                    final InputStream is = new FileInputStream(file);
                    final ArrayList<Emoji> emojies = objectMapper.readValue(is, new TypeReference<ArrayList<Emoji>>(){});

                    // Create download file list.
                    for(Emoji emoji : emojies)
                    {
                        FileInfo fileInfo = null;
                        for(EmojiFormat format : config.formats)
                        {
                            final String filePath = PathGenerator.getEmojiRelativePath(emoji.getName(), format, kind);

                            // If file already downloaded, ignore file.
                            // TODO : Use MD5.
                            if(new File(destinationPath, filePath).exists())
                                continue;

                            // Add to list.
                            if(fileInfo == null)
                            {
                                fileInfo = new FileInfo();
                                fileInfo.name = emoji.getName();
                                fileInfosArray.get(threadIndex).add(fileInfo);
                                threadIndex = (threadIndex + 1) % config.threadCount;
                            }
                            fileInfo.files.add(filePath);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            // Emoji download start.
            for(ArrayList<FileInfo> fileInfos : fileInfosArray)
            {
                if(fileInfos.isEmpty())
                    continue;

                final EmojiDownloadTask task = new EmojiDownloadTask();
                task.execute(fileInfos.toArray(new FileInfo[fileInfos.size()]));
            }

            // Thread end.
            super.onPostExecute(result);
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
    }
}
