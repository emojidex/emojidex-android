package com.emojidex.emojidexandroid.imageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.Emojidex;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by kou on 16/12/21.
 */
public class ImageLoader
{
    public static final int HANDLE_NULL = -1;

    private static final ImageLoader INSTANCE = new ImageLoader();

    private final Map<String, ImageParamEx> imageParams = new HashMap<String, ImageParamEx>();

    private Context context = null;
    private Resources res = null;

    private final ArrayList<ImageLoadListener> listeners = new ArrayList<ImageLoadListener>();
    private final DownloadListener downloadListener;

    private final TaskManager taskManager = new TaskManager();
    private final ArrayList<ImageLoadArguments> idleArgumentsArray = new ArrayList<ImageLoadArguments>();

    /**
     * Get singleton instance.
     * @return
     */
    public static ImageLoader getInstance()
    {
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private ImageLoader()
    {
        downloadListener = new DownloadListener(){
            @Override
            public void onDownloadImages(int handle, EmojiFormat format, String... emojiNames)
            {
                loadStart();
            }
        };
    }

    /**
     * Initialize ImageLoader.
     * @param c
     */
    public void initialize(Context c)
    {
        if( !isInitialized() )
            Emojidex.getInstance().addDownloadListener(downloadListener);

        context = c;
        res = context.getResources();
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
        // nothing to do...
    }

    /**
     * Add image load listener.
     * @param listener      Image load listener.
     */
    public void addListener(ImageLoadListener listener)
    {
        if( !listeners.contains(listener) )
            listeners.add(listener);
    }

    /**
     * Remove image load listener.
     * @param listener      Image load listener.
     */
    public void removeListener(ImageLoadListener listener)
    {
        listeners.remove(listener);
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

        ImageParamEx param = isAlreadyLoading(name, format);
        if(param != null && !param.isDummy)
            return param.imageParam;

        // Load image.
        final ImageLoadArguments arguments =
                new ImageLoadArguments(res, name)
                        .setFormat(format);
        if(autoDownload)
        {
            if( !idleArgumentsArray.contains(arguments) )
                idleArgumentsArray.add(arguments);
            loadStart();
        }
        else
            taskManager.regist(arguments);

        // Register image param.
        if(param == null)
        {
            final String key = createCacheKey(name, format);
            param = createDummyImageParam(format);
            imageParams.put(key, param);
        }
        return param.imageParam;
    }

    /**
     * Reload image.
     * @param result    Image load result.
     */
    void reload(ImageLoadTask.LoadResult result)
    {
        final ImageParam newParam = result.param;
        final ImageLoadArguments arg = result.arguments;
        final ImageParamEx param = isAlreadyLoading(arg.getEmojiName(), arg.getFormat());

        for (int i = 0;  i < param.imageParam.frames.length;  ++i)
        {
            // Overwrite and replace bitmap.
            final Bitmap oldBitmap = param.imageParam.frames[i].bitmap;
            final Bitmap newBitmap = newParam.frames[i].bitmap;

            final int[] pixels = new int[newBitmap.getWidth() * newBitmap.getHeight()];
            final IntBuffer buffer = IntBuffer.wrap(pixels);

            buffer.position(0);
            newBitmap.copyPixelsToBuffer(buffer);

            buffer.position(0);
            oldBitmap.copyPixelsFromBuffer(buffer);

            newParam.frames[i].bitmap.recycle();
            newParam.frames[i].bitmap = param.imageParam.frames[i].bitmap;
        }

        param.imageParam = newParam;
        param.isDummy = !result.succeeded;
    }

    /**
     * Preload image.
     * @param name          Emoji name.
     * @param format        Emoji format.
     * @return handle
     */
    public int preload(String name, EmojiFormat format)
    {
        // Already loading
        ImageParamEx param = isAlreadyLoading(name, format);
        if (param != null && !param.isDummy)
            return HANDLE_NULL;

        // Load image.
        int handle = taskManager.regist(new ImageLoadArguments(res, name).setFormat(format));

        // Register image param.
        if (param == null)
            imageParams.put(createCacheKey(name, format), createDummyImageParam(format));

        return handle;
    }

    void finishTask(int handle)
    {
        taskManager.finishTask(handle);
    }

    void notifyToListener(int handle, EmojiFormat format, String emojiName)
    {
        for(ImageLoadListener listener : listeners)
        {
            listener.onLoad(handle, format, emojiName);
        }
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
    private ImageParamEx isAlreadyLoading(String name, EmojiFormat format)
    {
        final String key = createCacheKey(name, format);
        return imageParams.get(key);
    }

    /**
     * Create dummy image parameter.
     * @param format    Emoji format.
     * @return          Dummy image parameter.
     */
    private ImageParamEx createDummyImageParam(EmojiFormat format)
    {
        final ImageParamEx param = new ImageParamEx();
        param.imageParam = new ImageParam();
        final ImageParam.Frame frame = new ImageParam.Frame();
        frame.bitmap = ImageLoadUtils.loadDummyBitmap(res, format);

        param.imageParam.frames = new ImageParam.Frame[1];
        param.imageParam.frames[0] = frame;
        param.isDummy = true;

        return param;
    }

    /**
     * Start load images if file exists.
     */
    private void loadStart()
    {
        final Iterator<ImageLoadArguments> it = idleArgumentsArray.iterator();
        while(it.hasNext())
        {
            final ImageLoadArguments arguments = it.next();
            if(EmojidexFileUtils.existsLocalFile(EmojidexFileUtils.getLocalEmojiUri(arguments.getEmojiName(), arguments.getFormat())))
            {
                taskManager.regist(arguments);
                it.remove();
            }
        }
    }

    /**
     * Image parameter ex.
     */
    private class ImageParamEx
    {
        public ImageParam imageParam;
        public boolean isDummy;
    }
}
