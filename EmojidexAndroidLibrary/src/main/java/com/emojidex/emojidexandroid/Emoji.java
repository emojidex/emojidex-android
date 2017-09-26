package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji extends JsonParam {
    private Checksums current_checksums = null;
    private String type = null;

    private final List<Integer> codes = new ArrayList<Integer>();

    private Context context;
    private Resources res;
    private boolean hasOriginalCodes = false;

    @JsonProperty("current_checksums")
    public Checksums getCurrentChecksums()
    {
        if(current_checksums == null)
            current_checksums = new Checksums();
        return current_checksums;
    }

    public void setCurrentChecksums(Checksums current_checksums)
    {
        this.current_checksums = current_checksums;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * Check image is newest.
     * @param format    Emoji format.
     * @return          true if image is newest.
     */
    public boolean hasNewestImage(EmojiFormat format)
    {
        final String newest = getChecksums().get(format);
        final String current = getCurrentChecksums().get(format);
        return newest.equals(current);
    }

    /**
     * Reload image files.
     * @param format        Emoji format.
     */
    public void reloadImage(EmojiFormat format)
    {
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.reload(getCode(), format);
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
        return Emojidex.SEPARATOR + getCode() + Emojidex.SEPARATOR;
    }

    /**
     * Get emoji codes.
     * @return  Emoji codes.
     */
    @JsonIgnore
    public List<Integer> getCodes()
    {
        return codes;
    }

    /**
     * Get image of format.
     * @param format    Image format.
     * @return          Image.
     */
    @JsonIgnore
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
    @JsonIgnore
    public Drawable getDrawable(EmojiFormat format, float size)
    {
        return getDrawable(format, size, true);
    }

    /**
     * Get image of format.
     * @param format        Image format.
     * @param size          Drawable size.
     *                      If value is -1, auto set size.
     * @param autoDownload  Auto download image when image is old or not found.
     * @return              Image.
     */
    @JsonIgnore
    public Drawable getDrawable(EmojiFormat format, float size, boolean autoDownload)
    {
        // Load image.
        final ImageLoader.ImageParam imageParam = ImageLoader.getInstance().load(getCode(), format, autoDownload);

        Drawable result = null;

        // Animation emoji.
        if(imageParam.hasAnimation())
        {
            // Create drawable.
            final EmojidexAnimationDrawable drawable = new EmojidexAnimationDrawable();
            drawable.setOneShot(imageParam.isOneShot());
            drawable.setSkipFirst(imageParam.isSkipFirst());

            for(ImageLoader.ImageParam.Frame frame : imageParam.getFrames())
            {
                drawable.addFrame(
                        bitmapToDrawable(frame.getBitmap(), size),
                        frame.getDuration()
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
            result = bitmapToDrawable(imageParam.getFrames()[0].getBitmap(), size);;
        }

        return result;
    }

    /**
     * Get bitmaps of format.
     * @param format    Emoji format.
     * @return          Bitmap array.
     */
    @JsonIgnore
    public Bitmap[] getBitmaps(EmojiFormat format)
    {
        // Load image.
        final ImageLoader.ImageParam imageParam = ImageLoader.getInstance().load(getCode(), format, false);
        final ImageLoader.ImageParam.Frame[] frames = imageParam.getFrames();

        // Create bitmap array and copy bitmaps.
        final Bitmap[] result = new Bitmap[frames.length];

        for(int i = 0;  i < frames.length;  ++i)
            result[i] = frames[i].getBitmap();

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
        final String moji = getMoji();
        final int count = moji.codePointCount(0, moji.length());
        int next = 0;
        for(int i = 0;  i < count;  ++i)
        {
            final int codePoint = moji.codePointAt(next);
            next += Character.charCount(codePoint);

            // Ignore Variation selectors.
            if(Character.getType(codePoint) == Character.NON_SPACING_MARK)
                continue;

            codes.add(codePoint);
        }

        // Adjustment text.
        if(codes.size() < count)
        {
            String newMoji = "";
            for(Integer codePoint : codes)
                newMoji += String.valueOf(Character.toChars(codePoint));
            setMoji(newMoji);
        }
    }

    /**
     * Initialize emoji object.
     * @param res       Resources object.
     * @param codes     Original emoji code.
     */
    void initialize(Context context, int codes)
    {
        setMoji(new String(Character.toChars(codes)));
        hasOriginalCodes = true;

        initialize(context);
    }

    /**
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        final String moji = getMoji();
        return !codes.isEmpty() || (moji != null && moji.length() > 0);
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
