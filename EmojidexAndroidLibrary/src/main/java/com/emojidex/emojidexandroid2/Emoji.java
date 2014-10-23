package com.emojidex.emojidexandroid2;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.emojidex.emojidexandroidlibrary.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji extends SimpleJsonParam {
    private final List<Integer> codes = new ArrayList<Integer>();
    private final Drawable[] drawables = new Drawable[EmojiFormat.values().length];

    private Resources res;
    private boolean hasOriginalCodes = false;

    @Override
    public String toString() {
        return toString(Emojidex.getInstance().getDefaultFormat());
    }

    /**
     * Emoji object to string.
     * @param format    Image format.
     * @return          String.
     */
    public String toString(EmojiFormat format)
    {
        return (String)TextConverter.createEmojidexText(this, Emojidex.getInstance().getDefaultFormat());
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
        final int index = format.ordinal();

        // Load image.
        if(drawables[index] == null)
        {
            try
            {
                final File file = new File(PathGenerator.getLocalEmojiPath(name, format));
                if(file.exists())
                {
                    final InputStream is = new FileInputStream(file);
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    final Drawable[] drawableArray = {new BitmapDrawable(res, bitmap)};
                    drawables[index] = new LayerDrawable(drawableArray);
                }
                else
                {
                    final Drawable[] drawableArray = { res.getDrawable(R.drawable.ic_launcher) };
                    drawables[index] = new LayerDrawable(drawableArray);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        // Return image.
        return drawables[index];
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
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        return !codes.isEmpty() || text != null;
    }
}
