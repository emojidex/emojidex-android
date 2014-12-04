package com.emojidex.emojidexandroid;

import android.content.Context;
import android.util.Log;

import com.emojidex.emojidexandroidlibrary.R;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Created by kou on 14/10/03.
 */
public class Emojidex {
    static final String TAG = "EmojidexLibrary";

    public static final String SEPARATOR = ":";

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
        manager.add(PathUtils.getLocalJsonPath());
        defaultFormat = EmojiFormat.toFormat(this.context.getResources().getString(R.string.emoji_format_default));

        Log.d(TAG, "Initialize complete.");
    }

    /**
     * Download emoji image to local storage.
     * @param formats       Emoji format array.
     */
    public void download(EmojiFormat[] formats)
    {
        download(formats, null);
    }

    /**
     * Download emoji image to local storage.
     * @param formats       Emoji format array.
     * @param listener      Download event listener.
     */
    public void download(EmojiFormat[] formats, DownloadListener listener)
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        final EmojiDownloader downloader = new EmojiDownloader(context);
        final String rootPath = "http://assets.emojidex.com";
        if(listener != null)
            downloader.setListener(listener);
        downloader.add(
                PathUtils.getRemoteJsonPath("utf", rootPath),
                formats
        );
        downloader.add(
                PathUtils.getRemoteJsonPath("extended", rootPath),
                formats
        );
    }

    /**
     * Reload emojidex.
     */
    public void reload()
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        manager.reset();
        manager.add(PathUtils.getLocalJsonPath());
    }

    /**
     * Delete all cache files in local storage.
     */
    public void deleteLocalCache()
    {
        deleteFile(new File(PathUtils.LOCAL_ROOT_PATH));
        Log.d(TAG, "Delete all cache files in local storage.");
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
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return TextConverter.emojify(text, useImage, format);
    }

    /**
     * Emojidex text decode to normal text.
     * @param text  Emojidex text.
     * @return      Normal text.
     */
    public CharSequence deEmojify(CharSequence text)
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

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
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return manager.getEmoji(name);
    }

    /**
     * Get emoji from emoji codes.
     * @param codes     Emoji codes.
     * @return          Emoji.(If emoji is not found, return null.)
     */
    public Emoji getEmoji(List<Integer> codes)
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return manager.getEmoji(codes);
    }

    /**
     * Get emoji list from category name.
     * @param category  Category name.
     * @return          Emoji list.(If emoji list is not found, return null.)
     */
    public List<Emoji> getEmojiList(String category)
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return manager.getEmojiList(category);
    }

    /**
     * Ger all emoji list.
     * @return  All emoji list.
     */
    public List<Emoji> getAllEmojiList()
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return manager.getAllEmojiList();
    }

    /**
     * Get category name list.
     * @return  Category name list.
     */
    public Collection<String> getCategoryNames()
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();

        return manager.getCategoryNames();
    }

    /**
     * Get default format for device.
     * @return  Default format for device.
     */
    public EmojiFormat getDefaultFormat()
    {
        if( !isInitialized() )
            throw new EmojidexIsNotInitializedException();
        
        return defaultFormat;
    }

    /**
     * Delete file.
     * @param file  File.
     */
    private void deleteFile(File file)
    {
        // File is not found.
        if(file == null || !file.exists())
            return;

        // If file is directory, delete child files.
        if(file.isDirectory())
            for(File child : file.listFiles())
                deleteFile(child);

        // Delete file.
        file.delete();
    }
}
