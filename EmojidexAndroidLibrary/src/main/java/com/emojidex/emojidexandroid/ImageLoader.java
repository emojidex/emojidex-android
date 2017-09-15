package com.emojidex.emojidexandroid;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;

import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import japngasm.APNGAsm;
import japngasm.APNGFrame;

/**
 * Created by kou on 16/12/21.
 */
public class ImageLoader
{
    private static final ImageLoader INSTANCE = new ImageLoader();

    private final Map<String, ImageParam> imageParams = new HashMap<String, ImageParam>();

    private Context context = null;
    private Resources res = null;
    private BitmapCache bitmapCache = null;

    /**
     * Image parameter.
     */
    public static class ImageParam
    {
        private boolean oneShot;
        private boolean skipFirst;
        private Frame[] frames;

        public static class Frame
        {
            private Bitmap bitmap;
            private int duration;

            public Bitmap getBitmap()
            {
                return bitmap;
            }

            public int getDuration()
            {
                return duration;
            }
        }

        public boolean isOneShot()
        {
            return oneShot;
        }

        public boolean isSkipFirst()
        {
            return skipFirst;
        }

        public Frame[] getFrames()
        {
            return frames;
        }

        public boolean hasAnimation()
        {
            return frames.length > 1;
        }
    }

    /**
     * Get singleton instance.
     * @return
     */
    public static ImageLoader getInstance()
    {
        return INSTANCE;
    }

    /**
     * Initialize ImageLoader.
     * @param c
     */
    public void initialize(Context c)
    {
        context = c;
        res = context.getResources();

        final int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 8;

        bitmapCache = new BitmapCache(cacheSize);
    }

    /**
     * Check ImageLoader is initialized.
     * @return
     */
    public boolean isInitialized()
    {
        return context != null;
    }

    /**
     * Clear cache.
     */
    public void clearCache()
    {
        if(bitmapCache != null)
            bitmapCache.evictAll();
    }

    /**
     * Load image.
     * @param name          Emoji name.
     * @param format        Emoji format.
     * @param autoDownload  Auto download image when image is old or not found.
     * @return
     */
    public ImageParam load(String name, EmojiFormat format, boolean autoDownload)
    {
        if(autoDownload)
        {
            Emojidex.getInstance().getEmojiDownloader().downloadImages(
                    new ImageDownloadArguments(name)
                        .setFormat(format)
            );
        }

        final ImageParam param = isAlreadyLoading(name, format);
        if(param != null)
            return param;

        // Load image.
        return loadImageParam(name, format, true);
    }

    /**
     * Reload image.
     * @param name
     * @param format
     */
    public void reload(String name, EmojiFormat format)
    {
        // Skip reload when not loaded.
        final ImageParam oldParam = isAlreadyLoading(name, format);
        if(oldParam == null)
            return;

        // Reload image.
        final ImageParam newParam = loadImageParam(name, format, false);
        final String key = createCacheKey(name, format);

        for(int i = 0;  i < newParam.frames.length;  ++i)
        {
            if(i >= oldParam.frames.length)
            {
                final Bitmap old = bitmapCache.put(
                        key + i,
                        newParam.frames[i].bitmap
                );
                if(old != null)
                    old.recycle();
                continue;
            }

            // Overwrite and replace bitmap.
            final Bitmap oldBitmap = oldParam.frames[i].bitmap;
            final Bitmap newBitmap = newParam.frames[i].bitmap;

            final int[] pixels = new int[newBitmap.getWidth() * newBitmap.getHeight()];
            final IntBuffer buffer = IntBuffer.wrap(pixels);

            buffer.position(0);
            newBitmap.copyPixelsToBuffer(buffer);

            buffer.position(0);
            oldBitmap.copyPixelsFromBuffer(buffer);

            newParam.frames[i].bitmap.recycle();
            newParam.frames[i].bitmap = oldParam.frames[i].bitmap;
        }

        imageParams.put(key, newParam);
    }

    public void reload(String emojiName)
    {
        for(EmojiFormat format : EmojiFormat.values())
            reload(emojiName, format);
    }

    public void reload(EmojiFormat format)
    {
        for(String key : imageParams.keySet())
        {
            // keyParts[0]: Emoji resolution
            // keyParts[1]: Emoji name
            final String[] keyParts = key.split("/");

            if(keyParts[0].equals(format.getResolution()))
                reload(keyParts[1], format);
        }
    }

    /**
     * Private constructor.
     */
    private ImageLoader()
    {
        // nop
    }

    /**
     * Create cache key.
     * @param name
     * @param format
     * @return
     */
    private String createCacheKey(String name, EmojiFormat format)
    {
        return format.getResolution() + "/" + name;
    }

    /**
     * Check already loading.
     * @param name
     * @param format
     * @return
     */
    private ImageParam isAlreadyLoading(String name, EmojiFormat format)
    {
        final String key = createCacheKey(name, format);
        final ImageParam param = imageParams.get(key);
        if(param == null)
            return null;
        for(int i = 0;  i < param.frames.length;  ++i)
            if(bitmapCache.get(key + i) == null)
                return null;
        return param;
    }

    /**
     * Load image parameter.
     * @param name
     * @param format
     * @param registToCache
     * @return
     */
    private ImageParam loadImageParam(String name, EmojiFormat format, boolean registToCache)
    {
        // Copy temporary file.
        final Uri uri = EmojidexFileUtils.getLocalEmojiUri(name, format);
        final File tmpFile = new File(EmojidexFileUtils.getTemporaryPath() + format.getExtension());
        final Uri tmpUri = Uri.fromFile(tmpFile);

        EmojidexFileUtils.copyFile(uri, tmpUri);

        // Load image parameter from temporary file.
        final String key = createCacheKey(name, format);
        final String path = tmpUri.getPath();

        final APNGAsm apngasm = new APNGAsm();
        apngasm.disassemble(path);

        final int frameCount = Math.max((int)apngasm.getFrames().size(), 1);

        final ImageParam param = new ImageParam();
        param.frames = new ImageParam.Frame[frameCount];

        if(param.hasAnimation())
        {
            // Set param.
            param.oneShot = apngasm.getLoops() == 1;
            param.skipFirst = apngasm.isSkipFirst();

            // Create temporar image files.
            final String tmpDir = EmojidexFileUtils.getTemporaryPath() + "/";
            final File file = new File(tmpDir);
            file.mkdirs();
            apngasm.savePNGs(tmpDir);

            // Load frames.
            for(int i = 0;  i < frameCount;  ++i)
            {
                final ImageParam.Frame frame = new ImageParam.Frame();

                final String tmpPath = tmpDir + i + ".png";
                frame.bitmap = loadBitmap(tmpPath, format, registToCache ? key + i : null);

                final APNGFrame af = apngasm.getFrames().get(i);
                frame.duration = (int)(1000 * af.delayNum() / af.delayDen());

                param.frames[i] = frame;
            }

            EmojidexFileUtils.deleteFiles(file);
        }
        else
        {
            final ImageParam.Frame frame = new ImageParam.Frame();

            frame.bitmap = loadBitmap(path, format, registToCache ? key + 0 : null);

            param.frames[0] = frame;
        }

        if(registToCache)
            imageParams.put(key, param);

        // Delete temporary file.
        EmojidexFileUtils.deleteFiles(tmpFile);

        return param;
    }

    /**
     * Load bitmap and regist cache.
     * If load failed, return dummy bitmap.
     * @param path
     * @param format
     * @param cacheKey
     * @return
     */
    private Bitmap loadBitmap(String path, EmojiFormat format, String cacheKey)
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
                e.printStackTrace();
            }
        }

        // If load failed, load dummy bitmap.
        if(result == null)
        {
            try
            {
                final InputStream is = res.getAssets().open(EmojidexFileUtils.getAssetsEmojiPath("not_found", format));
                result = BitmapFactory.decodeStream(is);
                is.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        // Regist cache.
        if(cacheKey != null)
        {
            final Bitmap old = bitmapCache.put(cacheKey, result);
            if(old != null)
                old.recycle();
        }

        return result;
    }

    /**
     * LruCache for bitmap.
     */
    private class BitmapCache extends LruCache<String, Bitmap>
    {
        public BitmapCache(int maxSize)
        {
            super(maxSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value)
        {
            return value.getRowBytes() * value.getHeight();
        }
    }
}
