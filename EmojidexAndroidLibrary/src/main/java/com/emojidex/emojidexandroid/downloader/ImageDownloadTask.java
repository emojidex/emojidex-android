package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.ImageLoader;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kou on 17/08/29.
 */

class ImageDownloadTask extends AbstractDownloadTask{
    private static final int BUFFER_SIZE = 4096;
    private static final int CONNECT_TIMEOUT = 10 * 1000;   // milliseconds
    private static final int READ_TIMEOUT = 10 * 1000;      // milliseconds

    private final Context context;

    /**
     * Construct object.
     * @param arguments     Download arguments.
     * @param context       Context.
     */
    public ImageDownloadTask(ImageDownloadArguments arguments, Context context)
    {
        super(arguments);

        this.context = context;
    }

    @Override
    public TaskType getType()
    {
        return TaskType.IMAGE;
    }

    @Override
    protected boolean download()
    {
        // Download image.
        final ImageDownloadArguments arguments = (ImageDownloadArguments)getArguments();
        HttpsURLConnection connection = null;

        try
        {
            final URL url = new URL(EmojidexFileUtils.getRemoteEmojiPath(arguments.getEmojiName(), arguments.getFormat()));

            connection = (HttpsURLConnection)url.openConnection();
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.connect();

            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                final Uri dest = EmojidexFileUtils.getLocalEmojiUri(arguments.getEmojiName(), arguments.getFormat());

                // Create directory if destination uri is file and not found directory.
                if(dest.getScheme().equals("file"))
                {
                    final File parentDir = new File(dest.getPath()).getParentFile();
                    if(!parentDir.exists())
                        parentDir.mkdirs();
                }

                // Download file.
                final OutputStream os = context.getContentResolver().openOutputStream(dest);

                final DataInputStream dis = new DataInputStream(connection.getInputStream());
                final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

                final byte[] buffer = new byte[BUFFER_SIZE];
                int readByte;
                while((readByte = dis.read(buffer)) != -1)
                    dos.write(buffer, 0, readByte);

                dis.close();
                dos.close();
            }

            connection.disconnect();
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();

            if(connection != null)
                connection.disconnect();

            return false;
        }
        catch(IOException e)
        {
            e.printStackTrace();

            if(connection != null)
                connection.disconnect();

            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if(result)
        {
            final ImageDownloadArguments arguments = (ImageDownloadArguments)getArguments();

            // Update checksums.
            getDownloader().updateChecksums(arguments.getEmojiName(), arguments.getFormat());

            // Reload image.
            ImageLoader.getInstance().reload(arguments.getEmojiName(), arguments.getFormat());

            // Norify to listener.
            getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadImage(getHandle(), arguments.getEmojiName(), arguments.getFormat());
                }
            });
        }

        super.onPostExecute(result);
    }
}
