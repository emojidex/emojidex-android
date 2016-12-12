package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
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

import japngasm.APNGAsm;
import japngasm.APNGFrame;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji extends SimpleJsonParam {
    static final String TAG = Emojidex.TAG + "::Emoji";

    private final List<Integer> codes = new ArrayList<Integer>();
    private final DrawableParam[] drawableParams = new DrawableParam[EmojiFormat.values().length];

    private Context context;
    private Resources res;
    private boolean hasOriginalCodes = false;

    /**
     * Reload image files.
     */
    public void reloadImage()
    {
        for(EmojiFormat format : EmojiFormat.values())
        {
            final int index = format.ordinal();

            if(!isAlreadyLoading(index))
                continue;

            final DrawableParam currentParams = drawableParams[index];
            final DrawableParam newParams = loadDrawableParam(format);
            final int frameCount = Math.min(currentParams.frames.length, newParams.frames.length);

            for(int i = 0;  i < frameCount;  ++i)
            {
                final Bitmap currentBitmap = currentParams.frames[i].bitmap.get();
                final Bitmap newBitmap = newParams.frames[i].bitmap.get();

                // Create buffer.
                final int[] pixels = new int[newBitmap.getWidth() * newBitmap.getHeight()];
                final IntBuffer buffer = IntBuffer.wrap(pixels);

                // new bitmap -> buffer
                buffer.position(0);
                newBitmap.copyPixelsToBuffer(buffer);

                // current <- buffer
                buffer.position(0);
                currentBitmap.copyPixelsFromBuffer(buffer);

                // copy bitmap.
                newParams.frames[i].bitmap = currentParams.frames[i].bitmap;
            }

            drawableParams[index] = newParams;
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
        if(!isAlreadyLoading(index))
        {
            final DrawableParam dp = loadDrawableParam(format);
            drawableParams[index] = dp;
        }

        final DrawableParam drawableParam = drawableParams[index];
        Drawable result = null;

        // Animation emoji.
        if(drawableParam.frames.length > 1)
        {
            // Create drawable.
            final EmojidexAnimationDrawable drawable = new EmojidexAnimationDrawable();
            drawable.setOneShot(drawableParam.loops == 1);

            for(DrawableParam.FrameParam frame : drawableParam.frames)
            {
                drawable.addFrame(
                        bitmapToDrawable(frame.bitmap.get(), size),
                        frame.duration
                );
            }

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

            // Play animation.
            drawable.start();

            // Set result.
            result = drawable;
        }
        // Non-animation emoji.
        else
        {
            result = bitmapToDrawable(drawableParam.frames[0].bitmap.get(), size);;
        }

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
    void initialize(Context context)
    {
        this.context = context;
        res = this.context.getResources();

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
    void initialize(Context context, int codes)
    {
        text = new String(Character.toChars(codes));
        hasOriginalCodes = true;

        initialize(context);
    }

    /**
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        return !codes.isEmpty() || (text != null && text.length() > 0);
    }

    /**
     * Load bitmap from local storage.
     * @param format    Emoji format.
     * @return          Bitmap.
     */
    private DrawableParam loadDrawableParam(EmojiFormat format)
    {
        final DrawableParam result = new DrawableParam();
        final String path = getImageFilePath(format);
        final APNGAsm apngasm = new APNGAsm();
        apngasm.disassemble(path);

        final int frameCount = Math.max((int)apngasm.getFrames().size(), 1);
        result.setFrameCount(frameCount);

        if(frameCount > 1)
        {
            final String temporaryDir = context.getExternalCacheDir() + "/tmp" + System.currentTimeMillis() + "/";
            final File file = new File(temporaryDir);
            file.mkdirs();
            apngasm.savePNGs(temporaryDir);

            for(int i = 0;  i < frameCount;  ++i)
            {
                final String temporaryPath = temporaryDir + i + ".png";
                Bitmap newBitmap = loadBitmap(temporaryPath);
                if(newBitmap == null)
                    newBitmap = loadDummyBitmap(format);
                result.frames[i].bitmap = new WeakReference<Bitmap>(newBitmap.copy(newBitmap.getConfig(), true));
                newBitmap.recycle();

                final APNGFrame frame = apngasm.getFrames().get(i);
                result.frames[i].duration = (int)(1000 * frame.delayNum() / frame.delayDen());
            }

            result.loops = apngasm.getLoops();

            deleteFiles(file);
        }
        else
        {
            Bitmap newBitmap = loadBitmap(path);
            if(newBitmap == null)
                newBitmap = loadDummyBitmap(format);
            result.frames[0].bitmap = new WeakReference<Bitmap>(newBitmap.copy(newBitmap.getConfig(), true));
            newBitmap.recycle();
        }

        return result;
    }

    private Bitmap loadBitmap(String path)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap result = null;

        // Load bitmap from file.
        final File file = new File(path);
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

        return result;
    }

    private Bitmap loadDummyBitmap(EmojiFormat format)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        Bitmap result = null;

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

        return result;
    }

    private void deleteFiles(File file)
    {
        if(!file.exists())
            return;

        if(file.isDirectory())
            for(File child : file.listFiles())
                deleteFiles(child);

        file.delete();
    }

    private boolean isAlreadyLoading(int index)
    {
        final DrawableParam dp = drawableParams[index];
        if(dp == null)
            return false;
        for(DrawableParam.FrameParam frame : dp.frames)
            if(frame.bitmap.get() == null)
                return false;
        return true;
    }

    private Drawable bitmapToDrawable(Bitmap bitmap, float size)
    {
        final BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        // Resize.
        if(size > 0)
            drawable.setTargetDensity((int)(drawable.getBitmap().getDensity() * size / drawable.getIntrinsicWidth()));

        return drawable;
    }

    private static class DrawableParam
    {
        private FrameParam[] frames = null;
        private long loops = 0;

        public void setFrameCount(int count)
        {
            frames = new FrameParam[count];
            for(int i = 0;  i < count;  ++i)
                frames[i] = new FrameParam();
        }

        private static class FrameParam
        {
            WeakReference<Bitmap> bitmap = null;
            int duration = 0;
        }
    }
}
