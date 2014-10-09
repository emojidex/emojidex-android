package org.genshin.emojidexandroid2;

import android.graphics.drawable.Drawable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Emoji {
    @JsonProperty("code")       private String name;
    @JsonProperty("moji")       private String text;
    @JsonProperty("category")   private String category;

    private final List<Integer> codes = new ArrayList<Integer>();
    private final Drawable[] drawables = new Drawable[EmojiFormat.values().length];

    private String kind;

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
    public Drawable getDrawable(EmojiFormat format)
    {
        return drawables[format.ordinal()];
    }

    /**
     * Initialize emoji object.
     * @param kind  Emoji kind.
     */
    void initialize(String kind)
    {
        this.kind = kind;
    }
}
