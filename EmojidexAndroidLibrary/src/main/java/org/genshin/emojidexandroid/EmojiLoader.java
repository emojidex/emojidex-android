package org.genshin.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
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
import java.util.TreeSet;

/**
 * Created by kou on 14/08/05.
 */
public class EmojiLoader
{
    private static final String[] KINDS = { "utf", "extended" };
    private static final String JSON_FILENAME = "emoji.json";

    private final Context context;
    private final DownloadManager downloadManager;
    private final TreeSet<Long> idSet = new TreeSet<Long>();

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

    public EmojiLoader(Context context)
    {
        this.context = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);

        sourcePath = "http://assets.emojidex.com";
        destinationPath = Environment.getExternalStorageDirectory().getPath() + "/emojidex";
    }

    public void load(Format... formats)
    {
        this.formats = formats;
        registerReceiver(new JsonDownloadReceiver());

        for(String kind : KINDS)
        {
            final DownloadManager.Request request = createRequest(kind + "/" + JSON_FILENAME);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            idSet.add(downloadManager.enqueue(request));
        }
    }

    private void registerReceiver(DownloadReceiver receiver)
    {
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private DownloadManager.Request createRequest(String path)
    {
        final File destinationFile = new File(destinationPath, path);
        if(destinationFile.exists())
            destinationFile.delete();

        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(sourcePath + "/" + path));
        request.setDestinationUri(Uri.fromFile(destinationFile));
        request.setTitle(path);
        request.setVisibleInDownloadsUi(false);

        return request;
    }


    private abstract class DownloadReceiver extends BroadcastReceiver
    {
        private int failedCount = 0;
        private int successfulCount = 0;
        private int unknownCount = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final Long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(action == DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            {
                final DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                final Cursor cursor = downloadManager.query(query);

                if(cursor.moveToFirst())
                {
                    final int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                    switch(status)
                    {
                        case DownloadManager.STATUS_SUCCESSFUL:
                            ++successfulCount;
                            onSuccessful(id, cursor);
                            break;
                        case DownloadManager.STATUS_FAILED:
                            ++failedCount;
                            onFailed(id, cursor);
                            break;
                        default:
                            ++unknownCount;
                            Log.d("loader", "Unknown status : status = " + status + ", id = " + id);
                            break;
                    }
                }
                else
                    ++unknownCount;

                idSet.remove(id);
                if(idSet.isEmpty())
                {
                    onCompleted();
                    context.unregisterReceiver(this);
                }
            }
        }

        public int getSuccessfulCount() { return successfulCount; }
        public int getFailedCount() { return failedCount; }

        protected void onFailed(long id, Cursor cursor)
        {
            final String path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            Log.d("loader", "onFailed : id = " + id + ", dest = " + path);
        }

        protected void onSuccessful(long id, Cursor cursor)
        {
            // nop
        }

        protected void onCompleted()
        {
            final int successful = getSuccessfulCount();
            final int failed = getFailedCount();
            final int unknown = unknownCount;
            Log.d("loader", "onCompleted : successful = " + successful + ", failed = " + failed + ", unknown = " + unknown + ", total = " + (successful + failed));
        }
    }



    private class JsonDownloadReceiver extends DownloadReceiver
    {
        @Override
        protected void onCompleted() {
            super.onCompleted();

            registerReceiver(new EmojiDownloadReceiver());

            int hoge = 10;
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
                            if( new File(destinationPath, fileName).exists() && hoge-- <= 0 )
                                continue;
                            final DownloadManager.Request request = createRequest(fileName);
                            idSet.add(downloadManager.enqueue(request));
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }



    private class EmojiDownloadReceiver extends DownloadReceiver
    {
        @Override
        protected void onCompleted() {
            super.onCompleted();

            Log.d("loader", "Notification!!!!!!!!!!!!!!!!!!!!!!!!!!!!11");
        }
    }
}
