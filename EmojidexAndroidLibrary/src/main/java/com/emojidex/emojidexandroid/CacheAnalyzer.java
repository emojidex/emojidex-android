package com.emojidex.emojidexandroid;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

/**
 * Created by kou on 17/03/02.
 */

class CacheAnalyzer
{
    private final String[] OLD_CACHE_PATHS = {
            Environment.getExternalStorageDirectory().getPath() + "/.emojidex"
    };

    /**
     * Analyze caches.
     */
    public void analyze()
    {
        // If have old directory,
        // move cache directory and remove old directories.
        final File newFile = new File(PathUtils.LOCAL_ROOT_PATH);
        for(final String oldPath : OLD_CACHE_PATHS)
        {
            final File oldFile = new File(oldPath);
            if(oldFile.exists() && oldFile.canRead())
            {
                if(newFile.exists())
                {
                    deleteFile(oldFile);
                }
                else
                {
                    moveFile(oldFile, newFile);
                }
            }
        }
    }

    /**
     * Delete file.
     * @param file  File.
     * @return      true if file delete succeeded.
     */
    private boolean deleteFile(File file)
    {
        // File is not found.
        if(file == null || !file.exists())
            return false;

        // If file is directory, delete child files.
        boolean result = true;
        if(file.isDirectory())
            for(File child : file.listFiles())
                result = deleteFile(child) && result;

        // Delete file.
        return file.delete() && result;
    }

    /**
     * Move file.
     * @param src   Source file.
     * @pa  ram dest  Destination file.
     * @return      true if file move succeeded.
     */
    private boolean moveFile(File src, File dest)
    {
        boolean result = true;

        if(src.isDirectory())
        {
            dest.mkdirs();
            for(String filename : src.list())
                result = moveFile(new File(src, filename), new File(dest, filename)) && result;
        }
        else
        {
            result = copyFile(src, dest) && result;
        }

        return src.delete() && result;
    }

    /**
     * Copy file.
     * @param src   Source file.
     * @param dest  Destination file.
     * @return      true if copy file succeeded.
     */
    private boolean copyFile(File src, File dest)
    {
        boolean result = true;

        try
        {
            final FileChannel in = new FileInputStream(src).getChannel();
            final FileChannel out = new FileOutputStream(dest).getChannel();

            in.transferTo(0, in.size(), out);

            in.close();
            out.close();
        }
        catch(Exception e)
        {
            result = false;
            e.printStackTrace();
        }

        return result;
    }
}
