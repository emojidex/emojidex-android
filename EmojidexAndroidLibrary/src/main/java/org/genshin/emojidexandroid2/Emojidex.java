package org.genshin.emojidexandroid2;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emojidex {
    private static final String TAG = "EmojidexLibrary";
    private static final Emojidex instance = new Emojidex();

    private Context context = null;

    /**
     * Image format.
     */
    public enum Format
    {
        SVG(".svg", "."),
        PNG_LDPI(".png", "ldpi"),
        PNG_MDPI(".png", "mdpi"),
        PNG_HDPI(".png", "hdpi"),
        PNG_XHDPI(".png", "xhdpi"),
        PNG_PX8(".png", "px8"),
        PNG_PX16(".png", "px16"),
        PNG_PX32(".png", "px32"),
        PNG_PX64(".png", "px64"),
        PNG_PX128(".png", "px128"),
        PNG_PX256(".png", "px256"),
        ;

        private final String extension;
        private final String relativeDir;

        /**
         * Get format from resolution name.
         * @param resolution    Resolution name.
         * @return              Format of resolution.(If resolution is not found, return null.)
         */
        public static Format getFormat(String resolution)
        {
            for(Format format : Format.values())
                if (resolution.equals(format.relativeDir))
                    return format;
            return null;
        }

        String getExtension()
        {
            return extension;
        }

        String getRelativeDir()
        {
            return relativeDir;
        }

        /** Construct format. */
        private Format(String extension, String relativeDir)
        {
            this.extension = extension;
            this.relativeDir = relativeDir;
        }
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
        Log.d(TAG, "Initialize start.");
        if(isInitialized())
        {
            Log.d(TAG, "Already initialized.");
            return;
        }

        this.context = context.getApplicationContext();

        Log.d(TAG, "Initialize complete.");
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
