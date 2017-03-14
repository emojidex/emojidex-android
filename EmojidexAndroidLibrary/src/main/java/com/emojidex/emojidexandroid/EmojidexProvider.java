package com.emojidex.emojidexandroid;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EmojidexProvider extends ContentProvider
{
    private static String AUTHORITIES = "com.emojidex.emojidexandroid.provider";
    private static String CACHE_DIR = "emojidex_caches";

    public EmojidexProvider()
    {
    }

    /**
     * Get json file uri.
     * @return  Json file uri.
     */
    public static Uri getJsonUri()
    {
        return getUri(CACHE_DIR + "/emoji.json");
    }

    /**
     * Get emoji uri.
     * @param emojiName     Emoji name.
     * @param format        Emoji format.
     * @return              Emoji uri.
     */
    public static Uri getEmojiUri(String emojiName, EmojiFormat format)
    {
        return getUri(CACHE_DIR + "/" + format.getRelativeDir() + "/" + emojiName + format.getExtension());
    }

    /**
     * Get uri.
     * @param encodedPath   Encoded path.
     * @return              Uri.
     */
    private static Uri getUri(String encodedPath)
    {
        return new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITIES)
                .encodedPath(encodedPath)
                .build();
    }

    /**
     * Convert uri to file.
     * @param uri   Uri.
     * @return      File.
     */
    private File uriToFile(Uri uri)
    {
        File file = null;
        try
        {
            file = new File(
                    getContext().getFilesDir(),
                    uri.getEncodedPath()
            ).getCanonicalFile();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * Convert to file access mode bits.
     * @param mode  File access mode.
     * @return      File access mode bits.
     */
    private int toModeBits(String mode)
    {
        switch(mode)
        {
            case "r":
                return ParcelFileDescriptor.MODE_READ_ONLY;
            case "w":
            case "wt":
                return ParcelFileDescriptor.MODE_WRITE_ONLY
                        |   ParcelFileDescriptor.MODE_CREATE
                        |   ParcelFileDescriptor.MODE_TRUNCATE;
            case "wa":
                return ParcelFileDescriptor.MODE_WRITE_ONLY
                        |   ParcelFileDescriptor.MODE_CREATE
                        |   ParcelFileDescriptor.MODE_APPEND;
            case "rw":
                return ParcelFileDescriptor.MODE_READ_WRITE
                        |   ParcelFileDescriptor.MODE_CREATE;
            case "rwt":
                return ParcelFileDescriptor.MODE_READ_WRITE
                        |   ParcelFileDescriptor.MODE_CREATE
                        |   ParcelFileDescriptor.MODE_TRUNCATE;
            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException
    {
        final File file = uriToFile(uri);
        final int modeBits = toModeBits(mode);

        final File parentDir = file.getParentFile();
        if(parentDir != null && !parentDir.exists())
            parentDir.mkdirs();

        return ParcelFileDescriptor.open(file, modeBits);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri)
    {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate()
    {
        // TODO: Implement this to initialize your content provider on startup.
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        // TODO: Implement this to handle query requests from clients.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
