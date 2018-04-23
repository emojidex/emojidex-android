package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.downloader.arguments.AbstractFileDownloadArguments;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kou on 17/12/05.
 */

abstract class AbstractFileDownloadTask extends AbstractDownloadTask {
    private static final int BUFFER_SIZE = 4096;
    private static final int CONNECT_TIMEOUT = 10 * 1000;   // milliseconds
    private static final int READ_TIMEOUT = 10 * 1000;      // milliseconds

    private final Context context;

    /**
     * Construct object.
     * @param arguments     Download arguments.
     * @param context       Context.
     */
    public AbstractFileDownloadTask(AbstractFileDownloadArguments arguments, Context context)
    {
        super(arguments);

        this.context = context;
    }

    @Override
    protected boolean download()
    {
        // Download image.
        HttpsURLConnection connection = null;

        try
        {
            final URL url = new URL(getRemotePath());

            connection = (HttpsURLConnection)url.openConnection();
            connection.setAllowUserInteraction(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.connect();

            if(connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
            {
                final Uri dest = getOutputUri();

                // Create directory if destination uri is file and not found directory.
                if(dest.getScheme().equals("file"))
                {
                    final File parentDir = new File(dest.getPath()).getParentFile();
                    if(!parentDir.exists())
                        parentDir.mkdirs();
                }

                // Download file.
                final OutputStream os = context.getContentResolver().openOutputStream(dest);
                final InputStream is = connection.getInputStream();

                final byte[] buffer = new byte[BUFFER_SIZE];
                int readByte;
                while((readByte = is.read(buffer)) != -1)
                    os.write(buffer, 0, readByte);

                is.close();
                os.close();
            }

            connection.disconnect();
        }
        catch(MalformedURLException e)
        {
            if(connection != null)
                connection.disconnect();

            return false;
        }
        catch(IOException e)
        {
            if(connection != null)
                connection.disconnect();

            return false;
        }

        return true;
    }

    /**
     * Get context.
     * @return      Context.
     */
    protected Context getContext()
    {
        return context;
    }

    /**
     * Get remote file path.
     * @return      Remote file path.
     */
    protected abstract String getRemotePath();

    /**
     * Get output uri.
     * @return      Output uri.
     */
    protected abstract Uri getOutputUri();
}
