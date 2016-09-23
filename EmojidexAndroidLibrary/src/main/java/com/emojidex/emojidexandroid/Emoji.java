package com.emojidex.emojidexandroid;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji extends SimpleJsonParam {
    static final String TAG = Emojidex.TAG + "::Emoji";

    private final List<Integer> codes = new ArrayList<Integer>();
    private final WeakReference<Bitmap>[] bitmaps = new WeakReference[EmojiFormat.values().length];

    private Resources res;
    private boolean hasOriginalCodes = false;

    /**
     * Reload image files.
     */
    public void reloadImage()
    {
        for(EmojiFormat format : EmojiFormat.values())
        {
            if(bitmaps[format.ordinal()] == null || bitmaps[format.ordinal()].get() == null)
                continue;

            final Bitmap current = bitmaps[format.ordinal()].get();

            // Load bitmap and create buffer.
            final Bitmap newBitmap = loadBitmap(format);
            final int[] pixels = new int[newBitmap.getWidth() * newBitmap.getHeight()];
            final IntBuffer buffer = IntBuffer.wrap(pixels);

            // new bitmap -> buffer
            buffer.position(0);
            newBitmap.copyPixelsToBuffer(buffer);

            // current <- buffer
            buffer.position(0);
            current.copyPixelsFromBuffer(buffer);

            // release.
            newBitmap.recycle();
        }
    }

    /**
     * Emoji object to emojidex string.
     * @return          String.
     */
    public CharSequence toEmojidexString() {
        return toEmojidexString(Emojidex.getInstance().getDefaultFormat());
    }

    /**
     * Emoji object to emojidex string.
     * @param format    Image format.
     * @return          String.
     */
    public CharSequence toEmojidexString(EmojiFormat format)
    {
        return TextConverter.createEmojidexText(this, Emojidex.getInstance().getDefaultFormat());
    }

    /**
     * Emoji object to tag string.
     * @return      Tag string.
     */
    public CharSequence toTagString()
    {
        return Emojidex.SEPARATOR + name + Emojidex.SEPARATOR;
    }

    /**
     * Get emoji name.
     * @return  Emoji name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get emoji text.
     * @return  Emoji text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Get category name.
     * @return  Category name.
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Get emoji codes.
     * @return  Emoji codes.
     */
    public List<Integer> getCodes()
    {
        return codes;
    }

    /**
     * Get image of format.
     * @param format    Image format.
     * @return          Image.
     */
    public Drawable getDrawable(EmojiFormat format)
    {
        return getDrawable(format, -1);
    }

    /**
     * Get image of format.
     * @param format    Image format.
     * @param size      Drawable size.
     * @return          Image.
     */
    public Drawable getDrawable(EmojiFormat format, float size)
    {
        final int index = format.ordinal();

        // Load image.
        if(bitmaps[index] == null || bitmaps[index].get() == null)
        {
            final Bitmap newBitmap = loadBitmap(format);
            bitmaps[index] = new WeakReference<Bitmap>(newBitmap.copy(newBitmap.getConfig(), true));
            newBitmap.recycle();
        }

        // Create drawable.
        final BitmapDrawable result = new BitmapDrawable(res, bitmaps[index].get());
        result.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

        // Resize.
        if(size > 0)
            result.setTargetDensity((int)(result.getBitmap().getDensity() * size / result.getIntrinsicWidth()));

        return result;
    }

    public String getImageFilePath(EmojiFormat format)
    {
        return PathUtils.getLocalEmojiPath(name, format);
    }

    /**
     * Check emoji has original codes.
     * @return
     */
    public boolean hasOriginalCodes()
    {
        return hasOriginalCodes;
    }

    public List<String> getVariants()
    {
        return variants;
    }

    public String getBase()
    {
        return base;
    }

    /**
     * Initialize emoji object.
     * @param kind  Emoji kind.
     */
    void initialize(Resources res)
    {
        this.res = res;

        // Set codes.
        final int count = text.codePointCount(0, text.length());
        int next = 0;
        for(int i = 0;  i < count;  ++i)
        {
            final int codePoint = text.codePointAt(next);
            next += Character.charCount(codePoint);

            // Ignore Variation selectors.
            if(Character.getType(codePoint) == Character.NON_SPACING_MARK)
                continue;

            codes.add(codePoint);
        }

        // Adjustment text.
        if(codes.size() < count)
        {
            text = "";
            for(Integer codePoint : codes)
                text += String.valueOf(Character.toChars(codePoint));
        }
    }

    /**
     * Initialize emoji object.
     * @param res       Resources object.
     * @param codes     Original emoji code.
     */
    void initialize(Resources res, int codes)
    {
        text = new String(Character.toChars(codes));
        hasOriginalCodes = true;

        initialize(res);
    }

    /**
     * Load bitmap from local storage.
     * @param format    Emoji format.
     * @return          Bitmap.
     */
    Bitmap loadBitmap(EmojiFormat format)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap result = null;

        // Load bitmap from file.
        final File file = new File(getImageFilePath(format));
        if(file.exists())
        {
            try
            {
                final InputStream is = new FileInputStream(file);
                result = BitmapFactory.decodeStream(is, null, options);
                is.close();
            }
            catch(FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        // If load failed, use default image.
        if(result == null)
        {
            // If file not found, use default image.
            try
            {
                final InputStream is = res.getAssets().open(PathUtils.getAssetsEmojiPath("not_found", format));
                result = BitmapFactory.decodeStream(is, null, options);
                is.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        return !codes.isEmpty() || (text != null && text.length() > 0);
    }
}
