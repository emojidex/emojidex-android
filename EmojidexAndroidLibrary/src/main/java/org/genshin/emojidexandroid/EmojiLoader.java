package org.genshin.emojidexandroid;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by kou on 14/08/05.
 */
public class EmojiLoader
{
    private String sourcePath;
    private String destinationPath;

    public EmojiLoader()
    {
        sourcePath = "http://assets.emojidex.com/";
        destinationPath = Environment.getExternalStorageDirectory().getPath() + "/emojidex/";
    }

    public void load()
    {
        AsyncHttpRequest task = new AsyncHttpRequest(new TestListener());
        task.execute("utf/emoji.json", "extended/emoji.json");
    }

    public class ProgressValue
    {
        private int current = 0;
        private int total = 0;

        public int getCurrentSize() { return current; }
        public int getTotalSize() { return total; }
    }

    public interface Listener
    {
        public void onPreExecute();

        public void onProgressUpdate(ProgressValue[] values);

        public void onPostExecute(Integer result);

        public void onCancelled(Integer result);
    }

    private class TestListener implements Listener
    {
        @Override
        public void onPreExecute() {
            Log.d("loader", "onPreExecute");
        }

        @Override
        public void onProgressUpdate(ProgressValue[] values) {
            for(int i = 0;  i < values.length;  ++i)
            {
                final int current = values[i].getCurrentSize();
                final int total = values[i].getTotalSize();
                Log.d("loader", "onProgressUpdate : [" + i + "]" + current + "/" + total + "(" + (total==0?0:current*100/total) + "%)");
            }
        }

        @Override
        public void onPostExecute(Integer result) {
            Log.d("loader", "onPostExecute : " + result);
        }

        @Override
        public void onCancelled(Integer result) {
            Log.d("loader", "onCancelled : " + result);
        }
    }

    private class AsyncHttpRequest extends AsyncTask<String,ProgressValue,Integer>
    {
        private final Listener listener;

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
        protected Integer doInBackground(String... params) {
            final ProgressValue[] progressValues = new ProgressValue[params.length];
            final byte[] buf = new byte[4096];
            int result = 0;

            for(int i = 0;  i < params.length;  ++i)
                progressValues[i] = new ProgressValue();

            for(int i = 0;  i < params.length;  ++i)
            {
                if(isCancelled())
                    break;

                try
                {
                    final String path = params[i];

                    final URL url = new URL(sourcePath + path);

                    {
                        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.connect();
                        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                            throw new HttpException();
                        progressValues[i].total = connection.getContentLength();
                        connection.disconnect();
                    }

                    final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setAllowUserInteraction(false);
                    connection.setInstanceFollowRedirects(true);
                    connection.setRequestMethod("GET");
                    connection.connect();

                    if(connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                        throw new HttpException();

                    final File destinationFile = new File(destinationPath + path);
                    if( !destinationFile.exists() )
                        destinationFile.getParentFile().mkdirs();

                    DataInputStream dis = new DataInputStream(connection.getInputStream());
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destinationFile)));

                    int readByte;
                    publishProgress(progressValues);
                    while( (readByte = dis.read(buf)) != -1 )
                    {
                        dos.write(buf, 0, readByte);

                        progressValues[i].current += readByte;
                        publishProgress(progressValues);
                    }

                    dis.close();
                    dos.close();

                    ++result;
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(ProgressValue... values) {
            if(listener != null)
                listener.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(listener != null)
                listener.onPostExecute(result);
        }

        @Override
        protected void onCancelled(Integer result) {
            if(listener != null)
                listener.onCancelled(result);
        }
    }
}
