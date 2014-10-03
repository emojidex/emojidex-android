package org.genshin.emojidexandroid2;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emoji {
    private String name;
    private String category;
    private final List<Integer> codes = new ArrayList<Integer>();
    private String text;

    private final Drawable[] drawables = new Drawable[Emojidex.Format.values().length];

    /**
     * Get emoji name.
     * @return  Emoji name.
     */
    public String getName()
    {
        return name;
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
     * Get emoji text.
     * @return  Emoji text.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Get image of format.
     * @param format    Image format.
     * @return          Image.
     */
    public Drawable getDrawable(Emojidex.Format format)
    {
        return drawables[format.ordinal()];
    }
}
