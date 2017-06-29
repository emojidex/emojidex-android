package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.emojidex.emojidexandroid.downloader.DownloadConfig;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Created by kou on 15/03/23.
 */
class EmojidexUpdater {
    static final String TAG = MainActivity.TAG + "::EmojidexUpdater";

    private final Context context;
    private final Emojidex emojidex;

    private final Collection<Integer> downloadHandles = new LinkedHashSet<Integer>();

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
        if(     !downloadHandles.isEmpty()
            ||  (!checkExecUpdate() && !forceFlag) )
        {
            Log.d(TAG, "Skip update.");
            return false;
        }

        Log.d(TAG, "Start update.");

        final UserData userdata = UserData.getInstance();
        final DownloadConfig config =
                new DownloadConfig()
                        .addFormat(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)))
                        .addFormat(EmojiFormat.toFormat(context.getString(R.string.emoji_format_key)))
                        .setUser(userdata.getUsername(), userdata.getAuthToken())
                ;
        final EmojiDownloader downloader = emojidex.getEmojiDownloader();

        boolean result = false;

        // UTF
        int handle = downloader.downloadUTFEmoji(config);
        if(handle != EmojiDownloader.HANDLE_NULL)
        {
            downloadHandles.add(handle);
            result = true;
        }

        // Extended
        handle = downloader.downloadExtendedEmoji(config);
        if(handle != EmojiDownloader.HANDLE_NULL)
        {
            downloadHandles.add(handle);
            result = true;
        }

        if(result)
            emojidex.addDownloadListener(new CustomDownloadListener());

        return result;
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
        final String currentLanguage = EmojidexFileUtils.getLocaleString();
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
        final long currentTime = System.currentTimeMillis();
        final long updateInterval = Long.parseLong(pref.getString(context.getString(R.string.preference_key_update_interval), context.getString(R.string.preference_entryvalue_update_interval_default)));
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, EmojiDownloader.Result result)
        {
            if(     downloadHandles.remove(handle)
                &&  downloadHandles.isEmpty()       )
            {
                // If emoji download failed, execute force update next time.
                final long updateTime = result.getFailedCount() > 0 ? 0 : System.currentTimeMillis();
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                final SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putLong(context.getString(R.string.preference_key_last_update_time), updateTime);
                prefEditor.commit();

                // Show message.
                Toast.makeText(context, R.string.ime_message_update_complete, Toast.LENGTH_SHORT).show();

                // Remove listener.
                emojidex.removeDownloadListener(this);

                Log.d(TAG, "End update.");
            }
        }

        @Override
        public void onCancelled(int handle, EmojiDownloader.Result result)
        {
            if(     downloadHandles.remove(handle)
                    &&  downloadHandles.isEmpty()       )
            {
                // Remove listener.
                emojidex.removeDownloadListener(this);

                Log.d(TAG, "Cancel update.");
            }
        }
    }
}
