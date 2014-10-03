package org.genshin.emojidexandroid2;

import android.content.Context;

import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emojidex {
    private static Emojidex instance = new Emojidex();

    private Context context = null;

    /**
     * Image format.
     */
    public enum Format
    {
        SVG,
        PNG_LDPI,
        PNG_MDPI,
        PNG_HDPI,
        PNG_XHDPI,
        PNG_PX8,
        PNG_PX16,
        PNG_PX32,
        PNG_PX64,
        PNG_PX128,
        PNG_PX256,
        ;
    }

    /**
     * Get singleton instance.
     * @return  Singleton instance.
     */
    public static Emojidex getInstance() { return instance; }

    /**
     * Initialize emojidex.
     * @param context
     */
    public void initialize(Context context)
    {
        if(isInitialized())
            return;

        this.context = context.getApplicationContext();
    }

    /**
     * Download emoji image to local storage.
     * If already downloaded, update emoji.
     * @param formats   Download format list.
     */
    public void download(Format... formats)
    {

    }

    /**
     * Normal text encode to emojidex text.
     * @param text  Normal text.
     * @return      Emojidex text.
     */
    public CharSequence emojify(CharSequence text)
    {
        return emojify(text, true);
    }

    /**
     * Normal text encode to emojidex text.
     * @param text      Normal text.
     * @param useImage  If true, use phantom-emoji image.
     * @return          Emojidex text.
     */
    public CharSequence emojify(CharSequence text, boolean useImage)
    {
        return text;
    }

    /**
     * Emojidex text decode to normal text.
     * @param text  Emojidex text.
     * @return      Normal text.
     */
    public CharSequence deEmojify(CharSequence text)
    {
        return text;
    }

    /**
     * Get initialized flag.
     * @return  true if Emojidex object is initialized.
     */
    public boolean isInitialized()
    {
        return context != null;
    }

    /**
     * Get emoji from emoji name.
     * @param name  Emoji name.
     * @return      Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(String name)
    {
        return null;
    }

    /**
     * Get emoji from emoji codes.
     * @param codes     Emoji codes.
     * @return          Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(Integer... codes)
    {
        return null;
    }

    /**
     * Get emoji list from category name.
     * @param category  Category name.
     * @return          Emoji list.(If emoji list is not found, return null.)
     */
    public List<Emoji> getEmojiList(String category)
    {
        return null;
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public List<Emoji> getAllEmojiList()
    {
        return null;
    }

    /**
     * Get category list.
     * @return  Category list.
     */
    public List<String> getCategories()
    {
        return null;
    }

    /**
     * Construct Emojidex object.
     */
    private Emojidex() { /* nop */ }
}
