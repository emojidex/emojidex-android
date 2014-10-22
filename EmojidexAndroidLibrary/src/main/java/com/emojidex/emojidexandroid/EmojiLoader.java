package com.emojidex.emojidexandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
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
import java.util.List;

/**
 * Created by kou on 14/08/05.
 */
public class EmojiLoader
{
    private static final String[] KINDS = { "utf", "extended" };
    private static final String JSON_FILENAME = "emoji.json";

    private final Context context;
    private final int threadCount;
    private final EventListener eventListener;

    private String sourcePath;
    private String destinationPath;
    private Format[] formats;

    public enum Format
    {
        SVG(".svg", "."),
        PNG_LDPI(".png", "ldpi"),
        PNG_MDPI(".png", "mdpi"),
        PNG_HDPI(".png", "hdpi"),
        PNG_XHDPI(".png", "xhdpi"),
        PNG_PX8(".png", "px8"),
        PNG_PX16(".png", "px16"),
        PNG_PX32(".png", "px32"),
        PNG_PX64(".png", "px64"),
        PNG_PX128(".png", "px128"),
        PNG_PX256(".png", "px256"),
        ;

        private final String extension;
        private final String relativeDir;

        private Format(String extension, String relativeDir)
        {
            this.extension = extension;
            this.relativeDir = relativeDir;
        }

        public static Format getFormat(String name)
        {
            for(Format format : Format.values())
                if (name.equals(format.relativeDir))
                    return format;
            return null;
        }
    }

    public EmojiLoader(Context context)
    {
        this(context, null);
    }

    public EmojiLoader(Context context, EventListener eventListener)
    {
        this(context, eventListener, 8);
    }

    public EmojiLoader(Context context, EventListener eventListener, int threadCount)
    {
        this.context = context;
        this.threadCount = Math.max(threadCount, 1);
        this.eventListener = eventListener == null ? new EventListener() : eventListener;

        sourcePath = "http://assets.emojidex.com";
        destinationPath = Environment.getExternalStorageDirectory().getPath() + "/emojidex";
    }

    public void load(Format... formats)
    {
        final FileInfo fileInfo = new FileInfo();
        fileInfo.name = JSON_FILENAME;
        fileInfo.files.ensureCapacity(KINDS.length);
        for(int i = 0;  i < KINDS.length;  ++i)
            fileInfo.files.add(KINDS[i] + "/" + JSON_FILENAME);

        this.formats = formats;
        final JsonDownloadTask task = new JsonDownloadTask();
        task.execute(fileInfo);
    }



    public class EventListener
    {
        public void onJsonDownloadCompleted()
        {
            Log.d("loader", "EmojiLoader::EventListener: onJsonDownloadCompleted.");
        }

        public void onEmojiDownloadCompleted(String emojiName)
        {
            Log.d("loader", "EmojiLoader::EventListener: onEmojiDownloadCompleted.(emojiName = \"" + emojiName + "\")");
        }

        public void onAllDownloadCompleted()
        {
            Log.d("loader", "EmojiLoader::EventListener: onAllDownloadCompleted.");
        }
    }



    private static class FileInfo
    {
        private String name;
        private ArrayList<String> files  = new ArrayList<String>();
    }


    public static class Result
    {
        private int succeeded = 0;
        private int failed = 0;
        private int total = 0;

        public int getSucceededCount() { return succeeded; }
        public int getFailedCount() { return failed; }
        public int getTotalCount() { return total; }
    }



    private class FileDownloadTask extends AsyncTask<FileInfo,Void,Result>
    {
        private final byte[] buffer = new byte[4096];

        @Override
        protected Result doInBackground(FileInfo... params) {
            final Result result = new Result();
            result.total = 0;

            for(FileInfo fileInfo : params)
            {
                result.total += fileInfo.files.size();

                if(isCancelled())
                    continue;

                for(String path : fileInfo.files)
                {
                    try
                    {
                        final URL url = new URL(sourcePath + "/" + path);

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

        protected void onDownloadCompleted(FileInfo fileInfo)
        {
            // nop
        }
    }


    private class JsonDownloadTask extends FileDownloadTask
    {
        @Override
        protected void onPostExecute(Result result) {
            eventListener.onJsonDownloadCompleted();

            final ArrayList<ArrayList<FileInfo>> fileInfosArray = new ArrayList<ArrayList<FileInfo>>();
            fileInfosArray.ensureCapacity(threadCount);
            for(int i = 0;  i < threadCount;  ++i)
                fileInfosArray.add(new ArrayList<FileInfo>());

            int threadIndex = 0;
            for(String kind : KINDS)
            {
                try
                {
                    final ObjectMapper objectMapper = new ObjectMapper();
                    final File file = new File(destinationPath, kind + "/" + JSON_FILENAME);
                    final InputStream is = new FileInputStream(file);
                    final List<EmojiData> emojiDatas = objectMapper.readValue(is, new TypeReference<ArrayList<EmojiData>>(){});

                    for(EmojiData emojiData : emojiDatas)
                    {
                        FileInfo fileInfo = null;
                        for(Format format : formats)
                        {
                            final String fileName = kind + "/" + format.relativeDir + "/" + emojiData.name + format.extension;
                            if( new File(destinationPath, fileName).exists())
                                continue;

                            if(fileInfo == null)
                            {
                                fileInfo = new FileInfo();
                                fileInfo.name = emojiData.name;
                                fileInfosArray.get(threadIndex).add(fileInfo);
                                threadIndex = (threadIndex + 1) % threadCount;
                            }
                            fileInfo.files.add(fileName);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            for(ArrayList<FileInfo> fileInfos : fileInfosArray)
            {
                if(!fileInfos.isEmpty())
                {
                    final EmojiDownloadTask task = new EmojiDownloadTask();
                    task.execute(fileInfos.toArray(new FileInfo[fileInfos.size()]));
                }
            }
        }
    }


    private class EmojiDownloadTask extends FileDownloadTask
    {
        private long startTime, endTime;

        @Override
        protected void onPreExecute() {
            startTime = System.currentTimeMillis();
        }

        @Override
        protected void onDownloadCompleted(FileInfo fileInfo) {
            eventListener.onEmojiDownloadCompleted(fileInfo.name);
        }

        @Override
        protected void onPostExecute(final Result result) {
            putResult(result, "Complete.");
        }

        @Override
        protected void onCancelled(final Result result) {
            putResult(result, "Cancel.");
        }

        private void putResult(Result result, String title)
        {
            endTime = System.currentTimeMillis();

            final int succeeded = result.getSucceededCount();
            final int failed = result.getFailedCount();
            final int total = result.getTotalCount();

            Log.d("loader", title + " : (" + succeeded + "+" + failed + "=" + (succeeded+failed) + ")/" + total + " : " + ((endTime-startTime)/1000.0) + "sec");
        }
    }
}