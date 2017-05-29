package com.emojidex.emojidexandroid;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.Locale;

/**
 * Created by kou on 14/10/03.
 */
class EmojidexFileUtils
{
    private static final String CACHE_DIR = "emojidex_caches";
    private static final String REMOTE_ROOT_PATH_DEFAULT = "https://cdn.emojidex.com";
    private static final String API_ROOT_PATH = "https://www.emojidex.com/api/v1";
    private static final String JSON_FILENAME = "emoji.json";

    private static Context context = null;
    private static boolean hasContentProvider = false;
    private static String localRoot = "";


    /**
     * Initialize path utilities.
     * @param context   Context.
     */
    public static void initialize(Context context)
    {
        EmojidexFileUtils.context = context;
        localRoot = EmojidexFileUtils.context.getFilesDir().toString();
        hasContentProvider = EmojidexProvider.existsProvider(EmojidexFileUtils.context);
    }

    /**
     * Create root uri from local storage.
     * @return  Root uri.
     */
    public static Uri getLocalRootUri()
    {
        return getLocalFileUri("");
    }

    /**
     * Create emoji uri from local storage.
     * @param name      Emoji name.
     * @param format    Emoji format.
     * @return          Emoji uri.
     */
    public static Uri getLocalEmojiUri(String name, EmojiFormat format)
    {
        return getLocalFileUri(format.getRelativeDir() + "/" + name.replace(' ', '_') + format.getExtension());
    }

    /**
     * Create json uri from local storage.
     * @return  Json uri.
     */
    public static Uri getLocalJsonUri()
    {
        return getLocalFileUri(JSON_FILENAME);
    }

    /**
     * Create uri from local storage.
     * @param relativePath  Relative file path.
     * @return              Uri.
     */
    public static Uri getLocalFileUri(String relativePath)
    {
        return hasContentProvider ?
                EmojidexProvider.getUri(CACHE_DIR + "/" + relativePath) :
                Uri.parse("file:" + localRoot + "/" + CACHE_DIR + "/" + relativePath);
    }

    /**
     * Get default root path from remote server.
     * @return      Default root path.
     */
    public static String getRemoteRootPathDefault()
    {
        return REMOTE_ROOT_PATH_DEFAULT;
    }

    /**
     * Create emoji path from remote server.
     * @param name          Emoji name.
     * @param format        Emoji format.
     * @param rootPath      Server path.
     * @return      Emoji path.
     */
    public static String getRemoteEmojiPath(String name, EmojiFormat format, String rootPath)
    {
        name = name.replace(' ', '_');
        try
        {
            name = URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return rootPath + "/emoji/"
                + format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Create emoji archive path from remote server.
     * @param format        Emoji format.
     * @param rootPath      Server path.
     * @return      Emoji archive path.
     */
    public static String getRemoteEmojiArchivePath(EmojiFormat format, String rootPath)
    {
        return rootPath + "/packs/utf-"
                + format.getRelativeDir()
                + (Locale.getDefault().equals(Locale.JAPAN) ? "-ja" : "")
                + ".tar.xz"
                ;
    }

    /**
     * Create json path from remote server.
     * @param kind      Emoji kind.
     * @param rootPath  Server path.
     * @return      Json path.
     */
    public static String getRemoteJsonPath(String kind, String rootPath)
    {
        return rootPath + "/"
                + kind + "_emoji?detailed=true&locale="
                + getLocaleString()
                ;
    }

    /**
     * Create emoji path from assets.
     * @param name      Emoji name.
     * @param format    Emoji format.
     * @return          Emoji path.
     */
    public static String getAssetsEmojiPath(String name, EmojiFormat format)
    {
        return format.getRelativeDir() + "/"
                + name + format.getExtension()
                ;
    }

    /**
     * Get api root path from remote server.
     * @return      Default API root path.
     */
    public static String getAPIRootPath()
    {
        return API_ROOT_PATH;
    }

    /**
     * Generate temporary path.
     * @return      Temporary path.
     */
    public static String getTemporaryPath()
    {
        return context.getExternalCacheDir().getPath() + "/tmp" + System.currentTimeMillis();
    }

    /**
     * Get locale string.
     * @return      en or ja.
     */
    public static String getLocaleString()
    {
        Locale locale = Locale.getDefault();
        if (Locale.JAPAN.equals(locale))
        {
            return "ja";
        }
        else
        {
            return "en";
        }
    }

    /**
     * Find emoji format directory from local storage.
     * @param format    Emoji format.
     * @return          true if exists directory.
     */
    public static boolean existsLocalEmojiFormatDirectory(EmojiFormat format)
    {
        return existsLocalFile(
                getLocalFileUri(format.getRelativeDir() + "/")
        );
    }

    public static boolean existsLocalFile(Uri uri)
    {
        boolean result = false;
        try
        {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            pfd.close();
            result = true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Delete files.
     * @param uri       File uri.
     * @return      true if delete succeeded.
     */
    public static boolean deleteFiles(Uri uri)
    {
        if(uri.getScheme().equals("file"))
            return deleteFiles(new File(uri.getPath()));

        // Use content provider if uri is not file.
        return context.getContentResolver().delete(uri, null, null) != 0;
    }

    /**
     * Delete files.
     * @param file  File.
     * @return      true if delete succeeded.
     */
    public static boolean deleteFiles(File file)
    {
        // File is not found.
        if(file == null || !file.exists())
            return false;

        // If file is directory, delete child files.
        boolean result = true;
        if(file.isDirectory())
            for(File child : file.listFiles())
                result = deleteFiles(child) && result;

        // Delete file.
        return file.delete() && result;
    }

    /**
     * Get file size.
     * @param uri   File uri.
     * @return      File size.
     */
    public static long getFileSize(Uri uri)
    {
        if(uri.getScheme().equals("file"))
            return getFileSize(new File(uri.getPath()));

        // Use content provider if uri is not file.
        final Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{ OpenableColumns.SIZE },
                null, null, null
        );
        if(cursor.moveToNext())
            return cursor.getLong(0);

        return 0;
    }

    /**
     * Get file size.
     * @param file      File.
     * @return          File size.
     */
    public static long getFileSize(File file)
    {
        if( !file.isDirectory() )
            return file.length();

        long size = 0;
        for(File child : file.listFiles())
            size += getFileSize(child);

        return size;
    }

    /**
     * Copy file.
     * @param src       Source file uri.
     * @param dest      Destination file uri.
     * @return          true if copy succeeded.
     */
    public static boolean copyFile(Uri src, Uri dest)
    {
        boolean result = false;

        try
        {
            final ContentResolver cr = context.getContentResolver();
            final FileDescriptor inFD = cr.openFileDescriptor(src, "r").getFileDescriptor();
            final FileDescriptor outFD = cr.openFileDescriptor(dest, "w").getFileDescriptor();
            final FileChannel inChannel = new FileInputStream(inFD).getChannel();
            final FileChannel outChannel = new FileOutputStream(outFD).getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);

            inChannel.close();
            outChannel.close();

            result = true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
