package org.genshin.emojidexandroid;

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
    }

    public EmojiLoader()
    {
        sourcePath = "http://assets.emojidex.com";
        destinationPath = Environment.getExternalStorageDirectory().getPath() + "/emojidex";
    }

    public void load(Format... formats)
    {
        final String[] jsonFiles = new String[KINDS.length];
        for(int i = 0;  i < jsonFiles.length;  ++i)
            jsonFiles[i] = KINDS[i] + "/" + JSON_FILENAME;

        this.formats = formats;
        final AsyncHttpRequest task = new AsyncHttpRequest(new JsonLoadListener());
        task.execute(jsonFiles);
    }


    public class ProgressInfo
    {
        private String currentPath;
        private int current;
        private int[] loadedSizes;
        private int[] fileSizes;

        public ProgressInfo(int fileCount)
        {
            currentPath = "";
            current = 0;
            loadedSizes = new int[fileCount];
            fileSizes = new int[fileCount];
            for(int i = 0;  i < fileCount;  ++i)
                loadedSizes[i] = fileSizes[i] = 0;
        }

        public String getCurrentPath() { return currentPath; }
        public int getCurrentIndex() { return current; }
        public int getFileCount() { return loadedSizes.length; }
        public int getLoadedSize(int index) { return loadedSizes[index]; }
        public int getFileSize(int index) { return fileSizes[index]; }
    }


    public class Result
    {
        private int succeeded = 0;
        private int failed = 0;
        private int total = 0;

        public int getSucceededCount() { return succeeded; }
        public int getFailedCount() { return failed; }
        public int getTotalCount() { return total; }
    }



    public interface Listener
    {
        public void onPreExecute();

        public void onProgressUpdate(ProgressInfo progressInfo);

        public void onPostExecute(Result result);

        public void onCancelled(Result result);
    }



    private class JsonLoadListener implements Listener
    {
        @Override
        public void onPreExecute() {
            // nop
        }

        @Override
        public void onProgressUpdate(ProgressInfo progressInfo) {
            // nop
        }

        @Override
        public void onPostExecute(Result result) {
            Log.d("loader", "onPostExecute.");

            final ArrayList<String> fileNames = new ArrayList<String>();

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
                        for(Format format : formats)
                        {
                            final String fileName = kind + "/" + format.relativeDir + "/" + emojiData.name + format.extension;
                            if( new File(destinationPath, fileName).exists() )
                                continue;
                            fileNames.add(fileName);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            final AsyncHttpRequest task = new AsyncHttpRequest(new TestListener());
            task.execute(fileNames.toArray(new String[fileNames.size()]));
        }

        @Override
        public void onCancelled(Result result) {
            // nop
        }
    }



    private class TestListener implements Listener
    {
        private long startTime, endTime;

        @Override
        public void onPreExecute() {
            Log.d("loader", "onPreExecute");
            startTime = System.currentTimeMillis();
        }

        @Override
        public void onProgressUpdate(ProgressInfo progressInfo) {
            final int currentIndex = progressInfo.getCurrentIndex();
            Log.d("loader",
                    "[" + currentIndex + "/" + progressInfo.getFileCount() + "] "
                +   "\"" + progressInfo.getCurrentPath()+ "\" "
                +   progressInfo.getLoadedSize(currentIndex) + "/" + progressInfo.getFileSize(currentIndex)
            );
        }

        @Override
        public void onPostExecute(Result result) {
            endTime = System.currentTimeMillis();

            final int succeeded = result.getSucceededCount();
            final int failed = result.getFailedCount();
            final int total = result.getTotalCount();

            Log.d("loader", "onPostExecute : (" + succeeded + "+" + failed + "=" + (succeeded+failed) + ")/" + total + " : " + ((endTime-startTime)/1000.0) + "sec");
        }

        @Override
        public void onCancelled(Result result) {
            Log.d("loader", "onCancelled : " + result);
        }
    }



    private class AsyncHttpRequest extends AsyncTask<String,ProgressInfo,Result>
    {
        private final Listener listener;
        private byte[] buffer = new byte[4096];

        public AsyncHttpRequest()
        {
            this(null);
        }

        public AsyncHttpRequest(Listener listener)
        {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            if(listener != null)
                listener.onPreExecute();
        }

        @Override
        protected Result doInBackground(String... params) {
            final ProgressInfo progressInfo = new ProgressInfo(params.length);
            final Result result = new Result();
            result.total = params.length;

            for(int i = 0;  i < params.length;  ++i)
            {
                if(isCancelled())
                    break;

                try
                {
                    final String path = params[i];
                    final URL url = new URL(sourcePath + "/" + path);

                    progressInfo.current = i;
                    progressInfo.currentPath = path;

                    {
                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.connect();
                        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                            throw new HttpException();
                        progressInfo.fileSizes[i] = connection.getContentLength();
                        connection.disconnect();
                    }

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
                    publishProgress(progressInfo);
                    while( (readByte = dis.read(buffer)) != -1 )
                    {
                        dos.write(buffer, 0, readByte);

                        progressInfo.loadedSizes[i] += readByte;
                        publishProgress(progressInfo);
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

            return result;
        }

        @Override
        protected void onProgressUpdate(ProgressInfo... values) {
            if(listener != null)
                listener.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(Result result) {
            if(listener != null)
                listener.onPostExecute(result);
        }

        @Override
        protected void onCancelled(Result result) {
            if(listener != null)
                listener.onCancelled(result);
        }
    }
}
