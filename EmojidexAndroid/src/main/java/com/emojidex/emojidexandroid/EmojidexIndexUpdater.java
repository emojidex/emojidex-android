package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.List;

/**
 * Created by kou on 16/04/01.
 */
public class EmojidexIndexUpdater
{
    static final String TAG = MainActivity.TAG + "::EmojidexIndexUpdater";

    private final Context context;
    private final SaveDataManager indexManager;

    public EmojidexIndexUpdater(Context context)
    {
        this.context = context;
        indexManager = new SaveDataManager(this.context, SaveDataManager.Type.Index);
    }

    public boolean startUpdateThread()
    {
        return startUpdateThread(false);
    }

    public boolean startUpdateThread(boolean forceFlag)
    {
        if( !forceFlag && !checkExecUpdate() )
        {
            Log.d(TAG, "Skip index update.");
            return false;
        }

        Log.d(TAG, "Start index update.");

        final EmojiDownloader downloader = new EmojiDownloader();
        final DownloadConfig config = new DownloadConfig(
                EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)),
                EmojiFormat.toFormat(context.getString(R.string.emoji_format_key))
        );
        downloader.setListener(new CustomDownloadListener());
        downloader.downloadIndex(config);

        return true;
    }

    private boolean checkExecUpdate()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdateTime = pref.getLong(context.getString(R.string.preference_key_last_update_time_index), 0);
        final long currentTime = new Date().getTime();
        final long updateInterval = 24 * 60 * 60 * 1000;
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onPostOneJsonDownload(List<String> emojiNames)
        {
            indexManager.clear();

            for(String emoji : emojiNames)
            {
                indexManager.addLast(emoji);
            }

            indexManager.save();
        }

        @Override
        public void onPreAllEmojiDownload()
        {
            Emojidex.getInstance().reload();

            if(EmojidexIME.currentInstance != null)
                EmojidexIME.currentInstance.reloadCategory();

            if(CatalogActivity.currentInstance != null)
                CatalogActivity.currentInstance.reloadCategory();
        }

        @Override
        public void onPostOneEmojiDownload(String emojiName) {
            final Emoji emoji = Emojidex.getInstance().getEmoji(emojiName);
            if(emoji != null)
            {
                emoji.reloadImage();

                if(EmojidexIME.currentInstance != null)
                    EmojidexIME.currentInstance.invalidate(emojiName);

                if(CatalogActivity.currentInstance != null)
                    CatalogActivity.currentInstance.invalidate();
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();

            // Save update time.
            final long updateTime = new Date().getTime();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(context.getString(R.string.preference_key_last_update_time_index), updateTime);
            prefEditor.commit();

            Log.d(TAG, "End index update.");
        }
    }
}
