package com.emojidex.emojidexandroid;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EmojidexProvider extends ContentProvider
{
    private static final String[] COLUMNS =
    {
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
    };

    private static final String AUTHORITIES = "com.emojidex.emojidexandroid.provider";

    public EmojidexProvider()
    {
    }

    /**
     * Get uri.
     * @param encodedPath   Encoded path.
     * @return              Uri.
     */
    public static Uri getUri(String encodedPath)
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

    /**
     * Find emojidex provider.
     * @param context   Context.
     * @return          true if find provider.
     */
    public static boolean existsProvider(Context context)
    {
        ContentProviderClient cpc = context.getContentResolver().acquireUnstableContentProviderClient(AUTHORITIES);
        if(cpc != null)
        {
            cpc.release();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException
    {
        final File file = uriToFile(uri);
        final int modeBits = toModeBits(mode);

        if((modeBits & ParcelFileDescriptor.MODE_CREATE) != 0)
        {
            final File parentDir = file.getParentFile();
            if(parentDir != null && !parentDir.exists())
                parentDir.mkdirs();
        }

        return ParcelFileDescriptor.open(file, modeBits);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        final File file = uriToFile(uri);
        return EmojidexFileUtils.deleteFiles(file) ? 1 : 0;
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
        final File file = uriToFile(uri);

        if(projection == null)
            projection = COLUMNS;

        final String[] cols = new String[projection.length];
        final Object[] values = new Object[projection.length];
        for(int i = 0;  i < projection.length;  ++i)
        {
            final String col = projection[i];
            switch(col)
            {
                case OpenableColumns.DISPLAY_NAME:
                    cols[i] = col;
                    values[i] = file.getName();
                    break;
                case OpenableColumns.SIZE:
                    cols[i] = col;
                    values[i] = EmojidexFileUtils.getFileSize(file);
                    break;
                default:
            }
        }

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
