package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * File download executor.
 */
abstract class AbstractFileDownloadExecutor extends AbstractDownloadExecutor {
    private final Context context;
    private final String description;
    private final JsonDownloadTask parentTask;
    private final ArrayList<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>();

    /**
     * Construct file download executor.
     * @param downloader    Emoji downloader.
     * @param context       Context.
     * @param description   Download file description.
     * @param parentTask    Parent task.
     */
    public AbstractFileDownloadExecutor(EmojiDownloader downloader, Context context, String description, JsonDownloadTask parentTask)
    {
        super(downloader);

        this.context = context;
        this.description = description;
        this.parentTask = parentTask;
    }

    /**
     * Add download file.
     * @param dest      Destination file path.
     * @param src       Source file path.
     */
    public void add(Uri dest, String src)
    {
        final DownloadInfo info = new DownloadInfo();
        info.dest = dest;
        info.src = src;
        downloadInfos.add(info);
    }

    /**
     * Get context.
     * @return      Context.
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * Get download file description.
     * @return      Download file description.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get parent task.
     * @return      Parent task.
     */
    public JsonDownloadTask getParentTask()
    {
        return parentTask;
    }

    /**
     * Get download informations.
     * @return      Download informations.
     */
    public ArrayList<DownloadInfo> getDownloadInfos()
    {
        return downloadInfos;
    }

    @Override
    public int download()
    {
        final byte[] buffer = new byte[4096];
        int succeeded = 0;
        for(DownloadInfo info : downloadInfos)
        {
            try
            {
                final URL url = new URL(info.src);

                final HttpURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setAllowUserInteraction(false);
                connection.setInstanceFollowRedirects(true);
                connection.setRequestMethod("GET");
                //connection.setSSLSocketFactory(HttpURLConnection.getDefaultSSLSocketFactory());
                connection.connect();

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    // Create directory if destination uri is file and not found directory.
                    if(info.dest.getScheme().equals("file"))
                    {
                        final File parentDir = new File(info.dest.getPath()).getParentFile();
                        if(!parentDir.exists())
                            parentDir.mkdirs();
                    }

                    // Download file.
                    final OutputStream os = context.getContentResolver().openOutputStream(info.dest);

                    final DataInputStream dis = new DataInputStream(connection.getInputStream());
                    final DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

                    int readByte;
                    while((readByte = dis.read(buffer)) != -1)
                        dos.write(buffer, 0, readByte);

                    dis.close();
                    dos.close();

                    ++succeeded;
                }
            } catch(MalformedURLException e)
            {
                e.printStackTrace();
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return succeeded;
    }

    @Override
    protected int getDownloadCount()
    {
        return downloadInfos.size();
    }

    /**
     * Download information.
     */
    protected class DownloadInfo {
        public String src;
        public Uri dest;
    }
}
