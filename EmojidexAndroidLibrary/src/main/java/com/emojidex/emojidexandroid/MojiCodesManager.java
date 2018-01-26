package com.emojidex.emojidexandroid;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.emojidexandroid.downloader.arguments.MojiCodesDownloadArguments;

/**
 * Created by kou on 18/01/24.
 */

class MojiCodesManager {
    private final static MojiCodesManager INSTANCE = new MojiCodesManager();

    private MojiCodes mojiCodes;

    /**
     * Get singleton instance.
     * @return      Singleton instance.
     */
    public static MojiCodesManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Construct object.
     */
    private MojiCodesManager()
    {
        // nop
    }

    /**
     * Initialize object.
     */
    public void initialize()
    {
        reload();
    }

    /**
     * Reload moji codes.
     */
    public void reload()
    {
        mojiCodes = EmojidexFileUtils.readJsonFromFile(
                EmojidexFileUtils.getLocalMojiCodesJsonUri(),
                MojiCodes.class
        );

        if(mojiCodes == null)
            mojiCodes = new MojiCodes();
    }

    /**
     * Update moji codes.
     */
    public int update()
    {
        final Emojidex emojidex = Emojidex.getInstance();
        final int handle = emojidex.getEmojiDownloader().downloadMojiCodes(
                new MojiCodesDownloadArguments()
        );

        if(handle != EmojiDownloader.HANDLE_NULL)
            emojidex.addDownloadListener(new CustomDownloadListener(handle));

        return handle;
    }

    /**
     * Get moji codes.
     * @return      Moji codes.
     */
    public MojiCodes getMojiCodes()
    {
        return mojiCodes;
    }

    /**
     * Custom download listener.
     */
    private class CustomDownloadListener extends DownloadListener
    {
        private final int handle;

        public CustomDownloadListener(int handle)
        {
            this.handle = handle;
        }

        @Override
        public void onFinish(int handle, boolean result)
        {
            if(this.handle == handle)
            {
                reload();
                Emojidex.getInstance().removeDownloadListener(this);
            }
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            if(this.handle == handle)
            {
                Emojidex.getInstance().removeDownloadListener(this);
            }
        }
    }
}
