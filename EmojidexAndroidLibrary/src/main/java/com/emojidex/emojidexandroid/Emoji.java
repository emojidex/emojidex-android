package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji extends SimpleJsonParam {
    static final String TAG = Emojidex.TAG + "::Emoji";

    private final List<Integer> codes = new ArrayList<Integer>();

    private Context context;
    private Resources res;
    private boolean hasOriginalCodes = false;

    /**
     * Reload image files.
     */
    public void reloadImage()
    {
        final ImageLoader imageLoader = ImageLoader.getInstance();
        for(EmojiFormat format : EmojiFormat.values())
            imageLoader.reload(name, format);
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
        final ImageLoader.ImageParam imageParam = ImageLoader.getInstance().load(name, format);

        Drawable result = null;

        // Animation emoji.
        if(imageParam.hasAnimation())
        {
            // Create drawable.
            final EmojidexAnimationDrawable drawable = new EmojidexAnimationDrawable();
            drawable.setOneShot(imageParam.isOneShot);
            drawable.setSkipFirst(imageParam.isSkipFirst);

            for(ImageLoader.ImageParam.Frame frame : imageParam.frames)
            {
                drawable.addFrame(
                        bitmapToDrawable(frame.bitmap, size),
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
            result = bitmapToDrawable(imageParam.frames[0].bitmap, size);;
        }

        return result;
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

    public int getScore()
    {
        return score;
    }

    /**
     * Initialize emoji object.
     * @param kind  Emoji kind.
     */
    void initialize(Context context)
    {
        this.context = context;
        res = this.context.getResources();

        // Initialize image loader.
        final ImageLoader imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInitialized())
            imageLoader.initialize(this.context);

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

    private Drawable bitmapToDrawable(Bitmap bitmap, float size)
    {
        final BitmapDrawable drawable = new BitmapDrawable(res, bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        // Resize.
        if(size > 0)
            drawable.setTargetDensity((int)(drawable.getBitmap().getDensity() * size / drawable.getIntrinsicWidth()));

        return drawable;
    }
}
