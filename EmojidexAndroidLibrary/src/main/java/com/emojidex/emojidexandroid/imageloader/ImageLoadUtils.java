package com.emojidex.emojidexandroid.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

abstract class ImageLoadUtils {
    /**
     * Load bitmap.
     * If load failed, return dummy bitmap.
     * @param res       Resources object.
     * @param path      Bitmap file path.
     * @param format    Bitmap format.
     * @return          Bitmap.
     */
    public static Bitmap loadBitmap(Resources res, String path, EmojiFormat format)
    {
        final File file = new File(path);
        Bitmap result = null;

        // Load bitmap from file.
        if(file.exists())
        {
            try
            {
                final InputStream is = new FileInputStream(file);
                result = BitmapFactory.decodeStream(is);
                is.close();
            }
            catch(Exception e)
            {
                result = null;
            }
        }

        // If load failed, load dummy bitmap.
        if(result == null)
            result = loadDummyBitmap(res, format);

        return result;
    }
    /**
     * Load dummy bitmap.
     * If load failed, return null.
     * @param res       Resources object.
     * @param format    Bitmap format.
     * @return          Dummy bitmap.
     */
    public static Bitmap loadDummyBitmap(Resources res, EmojiFormat format)
    {
        Bitmap result = null;

        try
        {
            final InputStream is = res.getAssets().open(EmojidexFileUtils.getAssetsEmojiPath("not_found", format));
            result = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch(Exception e)
        {
            result = null;
        }

        return result;
    }
}
