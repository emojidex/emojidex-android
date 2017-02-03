package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * Created by kou on 15/03/23.
 */
class EmojidexUpdater {
    static final String TAG = MainActivity.TAG + "::EmojidexUpdater";

    private final Context context;
    private final Emojidex emojidex;

    /**
     * Construct object.
     * @param context
     */
    public EmojidexUpdater(Context context)
    {
        this.context = context;
        emojidex = Emojidex.getInstance();
    }

    /**
     * Start update thread.
     * @return  false when not start update.
     */
    public boolean startUpdateThread()
    {
        return startUpdateThread(false);
    }

    /**
     * Start update thread.
     * @param forceFlag     Force update flag.
     * @return              false when not start update.
     */
    public boolean startUpdateThread(boolean forceFlag)
    {
        if( !checkExecUpdate() && !forceFlag )
        {
            Log.d(TAG, "Skip update.");
            return false;
        }

        Log.d(TAG, "Start update.");

        final UserData userdata = UserData.getInstance();
        final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_key)));
        return userdata.isLogined() ?
                emojidex.download(formats.toArray(new EmojiFormat[formats.size()]), new CustomDownloadListener(forceFlag), userdata.getUsername(), userdata.getAuthToken()) :
                emojidex.download(formats.toArray(new EmojiFormat[formats.size()]), new CustomDownloadListener(forceFlag));
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkExecUpdate()
    {
        return checkChangeDeviceLanguage() || checkUpdateTime();
    }

    /**
     * Get change language flag.
     * @return  true when the language settings have been changed.
     */
    private boolean checkChangeDeviceLanguage()
    {
        // Check change device language.
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final String lastLanguage = pref.getString(context.getString(R.string.preference_key_last_language), "en");
        final String currentLanguage = PathUtils.getLocaleString();
        final boolean result = !lastLanguage.equals(currentLanguage);

        // Overwrite language pref.
        final SharedPreferences.Editor editor = pref.edit();
        editor.putString(context.getString(R.string.preference_key_last_language), currentLanguage);
        editor.commit();

        // Clear cache if change device language.
        if(result)
        {
            // Clear log.
            for(SaveDataManager.Type type : SaveDataManager.Type.values())
                SaveDataManager.getInstance(context, type).deleteFile();

            // Clear cache.
            emojidex.deleteLocalCache();

            // Reset last update time log.
            pref.edit().putLong(context.getString(R.string.preference_key_last_update_time), 0).commit();
        }

        // Return result.
        return result;
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkUpdateTime()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdateTime = pref.getLong(context.getString(R.string.preference_key_last_update_time), 0);
        final long currentTime = new Date().getTime();
        final long updateInterval = Long.parseLong(pref.getString(context.getString(R.string.preference_key_update_interval), context.getString(R.string.preference_entryvalue_update_interval_default)));
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    /**
     * Custom download listener.
     */
    class CustomDownloadListener extends DownloadListener {
        private final boolean force;

        public CustomDownloadListener(boolean forceFlag)
        {
            force = forceFlag;
        }

        @Override
        public void onPreAllEmojiDownload() {
            emojidex.reload();

            if(EmojidexIME.currentInstance != null)
                EmojidexIME.currentInstance.reload();

            if(CatalogActivity.currentInstance != null)
                CatalogActivity.currentInstance.reloadCategory();
        }

        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader)
        {
            super.onPostAllJsonDownload(downloader);

            new EmojidexIndexUpdater(context).startUpdateThread(2, force);
        }

        @Override
        public void onPostOneEmojiDownload(String emojiName) {
            final Emoji emoji = emojidex.getEmoji(emojiName);
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
        public void onFinish(EmojiDownloader.Result result) {
            super.onFinish(result);

            // Save update time.
            // If emoji download failed, execute force update next time.
            final long updateTime = result.getFailedCount() > 0 ? 0 : new Date().getTime();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(context.getString(R.string.preference_key_last_update_time), updateTime);
            prefEditor.commit();

            // Show message.
            Toast.makeText(context, R.string.ime_message_update_complete, Toast.LENGTH_SHORT).show();

            Log.d(TAG, "End update.");
        }
    }
}
