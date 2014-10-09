package org.genshin.emojidexandroid2;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.genshin.emojidexandroidlibrary.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Emoji {
    @JsonProperty("code")       private String name;
    @JsonProperty("moji")       private String text = null;
    @JsonProperty("category")   private String category;

    private final List<Integer> codes = new ArrayList<Integer>();
    private final Drawable[] drawables = new Drawable[EmojiFormat.values().length];

    private Resources res;
    private String kind;
    private boolean hasOriginalCodes = false;

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
        final int index = format.ordinal();

        // Load image.
        if(drawables[index] == null)
        {
            try
            {
                final File file = new File(PathGenerator.getLocalRootPath(), PathGenerator.getEmojiRelativePath(name, format, kind));
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
    void initialize(Resources res, String kind)
    {
        this.res = res;
        this.kind = kind;

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

    void initialize(Resources res, String kind, int codes)
    {
        text = new String(Character.toChars(codes));
        hasOriginalCodes = true;

        initialize(res, kind);
    }

    /**
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        return !codes.isEmpty() || text == null;
    }
}
