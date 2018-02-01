package com.emojidex.emojidexandroid;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.emojidexandroid.downloader.arguments.MojiCodesDownloadArguments;

import java.util.HashMap;
import java.util.List;

/**
 * Created by kou on 18/01/24.
 */

class MojiCodesManager {
    private final static MojiCodesManager INSTANCE = new MojiCodesManager();

    private MojiCodes mojiCodes;
    private String mojiRegex;
    private HashMap<String, String> c2mTable = new HashMap<String, String>();

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
        // Update moji_codes.json if file is not found.
        if( !EmojidexFileUtils.existsLocalFile(EmojidexFileUtils.getLocalMojiCodesJsonUri()) )
            update();

        // Load data from moji_codes.json.
        reload();
    }

    /**
     * Reload moji codes.
     */
    public void reload()
    {
        // Load moji codes json file.
        mojiCodes = EmojidexFileUtils.readJsonFromFile(
                EmojidexFileUtils.getLocalMojiCodesJsonUri(),
                MojiCodes.class
        );

        if(mojiCodes == null)
            mojiCodes = new MojiCodes();

        // Initialize regex mojiRegex.
        final List<String> mojiArray = mojiCodes.getMojiArray();

        mojiRegex = mojiArray.isEmpty() ? "" : mojiArray.get(0);
        final int count = mojiArray.size();
        for(int i = 1;  i < count;  ++i)
            mojiRegex += "|" + mojiArray.get(i);

        // Initialize code to utf table.
        for(HashMap.Entry<String, String> entry : mojiCodes.getMojiIndex().entrySet())
            c2mTable.put(entry.getValue().replace("_", " "), entry.getKey());
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
     * Convert moji to code.
     * @param moji      Moji.
     * @return          Code.
     */
    public String MojiToCode(String moji)
    {
        return mojiCodes.getMojiIndex().get(moji).replace("_", " ");
    }

    /**
     * Convert code to moji.
     * @param code      Code
     * @return          Moji.
     */
    public String CodeToMoji(String code)
    {
        return c2mTable.get(code);
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
     * Get regex moji string.
     * @return      Regex moji string.
     */
    public String getMojiRegex()
    {
        return mojiRegex;
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
