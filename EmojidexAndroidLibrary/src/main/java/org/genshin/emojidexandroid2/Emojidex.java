package org.genshin.emojidexandroid2;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.genshin.emojidexandroidlibrary.R;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emojidex {
    static final String TAG = "EmojidexLibrary";

    private static final Emojidex INSTANCE = new Emojidex();

    private Context context = null;
    private EmojiManager manager;
    private EmojiFormat defaultFormat;

    /**
     * Get singleton instance.
     * @return  Singleton instance.
     */
    public static Emojidex getInstance() { return INSTANCE; }

    /**
     * Initialize emojidex.
     * @param context   Context of application.
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
        manager = new EmojiManager(this.context);
//        manager.add(PathGenerator.getLocalJsonPath());
        defaultFormat = EmojiFormat.toFormat(this.context.getResources().getString(R.string.emojidex_format_default));

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
     * Reload emojidex.
     */
    public void reload()
    {
        manager.reset();
        manager.add(PathGenerator.getLocalJsonPath());
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
        return emojify(text, useImage, defaultFormat);
    }

    /**
     * Normal text encode to emojidex text.
     * @param text      Normal text.
     * @param useImage  If true, use phantom-emoji image.
     * @param format    Image format.
     * @return          Emojidex text.
     */
    public CharSequence emojify(CharSequence text, boolean useImage, EmojiFormat format)
    {
        return TextConverter.emojify(text, useImage, format);
    }

    /**
     * Emojidex text decode to normal text.
     * @param text  Emojidex text.
     * @return      Normal text.
     */
    public CharSequence deEmojify(CharSequence text)
    {
        return TextConverter.deEmojify(text);
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
        return manager.getEmoji(name);
    }

    /**
     * Get emoji from emoji codes.
     * @param codes     Emoji codes.
     * @return          Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(List<Integer> codes)
    {
        return manager.getEmoji(codes);
    }

    /**
     * Get emoji list from category name.
     * @param category  Category name.
     * @return          Emoji list.(If emoji list is not found, return null.)
     */
    public Collection<Emoji> getEmojiList(String category)
    {
        return manager.getEmojiList(category);
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public Collection<Emoji> getAllEmojiList()
    {
        return manager.getAllEmojiList();
    }

    /**
     * Get category name list.
     * @return  Category name list.
     */
    public Collection<String> getCategoryNames()
    {
        return manager.getCategoryNames();
    }

    /**
     * Get default format for device.
     * @return  Default format for device.
     */
    public EmojiFormat getDefaultFormat()
    {
        return defaultFormat;
    }
}
