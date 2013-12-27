package org.genshin.emojidexandroid;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nazuki on 2013/08/28.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmojiData
{
    private static final String NOT_UNICODE_MOJI = "\uFFFC";

    @JsonProperty("moji")           private String moji = NOT_UNICODE_MOJI;
//    @JsonProperty("alto")           private String[] alto;
    @JsonProperty("code")           private String name;
//    @JsonProperty("code-ja")        private String nameJa;
//    @JsonProperty("emoticon")       private String emoticon;
    @JsonProperty("category")       private String category;
//    @JsonProperty("unicode")        private String unicode;
//    @JsonProperty("attribution")    private String attribution;
//    @JsonProperty("contributor")    private String contributor;
//    @JsonProperty("url")            private String url;

    private Drawable icon = null;
    private List<Integer> codes = new ArrayList<Integer>();
    private boolean isUnicode = true;


    /**
     * Initialize EmojiData object.
     * @param res
     * @param dir
     */
    public void initialize(Resources res, String dir)
    {
        // Set emoji code.
        // Whether national flag.
        final int count = moji.codePointCount(0, moji.length());
        for(int i = 0;  i < count;  ++i)
            codes.add(moji.codePointAt(i));

        // Load icon image.
        try
        {
            InputStream is = res.getAssets().open(dir + name + ".png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            icon = new BitmapDrawable(res, bitmap);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Initialize EmojiData object.
     * @param res
     * @param dir
     * @param code
     */
    public void initialize(Resources res, String dir, int code)
    {
        // Set fields.
        moji = new String(Character.toChars(code));
        isUnicode = false;

        // Initialize emoji data.
        initialize(res, dir);
    }

    /**
     * Create image string.
     * @return      Image string.
     */
    public CharSequence createImageString()
    {
        final ImageSpan imageSpan = new ImageSpan(icon);
        final SpannableString str = new SpannableString(moji);
        str.setSpan(imageSpan, 0, str.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }

    /**
     * Get unicode character.
     * @return  Unicode character.
     */
    public String getMoji()
    {
        return moji;
    }

    /**
     * Get emoji name.
     * @return      Emoji name.
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
     * Get icon image.
     * @return      Icon image.
     */
    public Drawable getIcon()
    {
        return icon;
    }

    /**
     * Get emoji codes.
     * @return      Emoji codes.
     */
    public List<Integer> getCodes()
    {
        return codes;
    }

    /**
     * Get flag of has code.
     * @return
     */
    public boolean hasCode()
    {
        return !codes.isEmpty() || !moji.equals(NOT_UNICODE_MOJI);
    }

    /**
     * Get flag of unicode emoji.
     * @return
     */
    public boolean isUnicodeEmoji()
    {
        return isUnicode;
    }
}
