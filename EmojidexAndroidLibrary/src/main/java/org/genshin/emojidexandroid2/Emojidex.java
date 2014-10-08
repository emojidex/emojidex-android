package org.genshin.emojidexandroid2;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emojidex {
    static final String TAG = "EmojidexLibrary";

    private static final Emojidex INSTANCE = new Emojidex();
    private static final String[] KINDS = { "utf", "extended" };

    private Context context = null;

    /**
     * Get singleton instance.
     * @return  Singleton instance.
     */
    public static Emojidex getInstance() { return INSTANCE; }

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
     * @param config Configuration of download.
     */
    public void download(DownloadConfig config)
    {
        final EmojiDownloader downloader = new EmojiDownloader(context);
        downloader.download(config);
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
