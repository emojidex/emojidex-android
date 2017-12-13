package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.AbstractFileDownloadArguments;

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

    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public AbstractFileDownloadTask(AbstractFileDownloadArguments arguments)
    {
        super(arguments);
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
                // Download file.
                final OutputStream os = getOutputStream();
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

    /**
     * Get remote file path.
     * @return      Remote file path.
     */
    protected abstract String getRemotePath();

    /**
     * Get output stream.
     * @return      Output stream.
     */
    protected abstract OutputStream getOutputStream();
}
