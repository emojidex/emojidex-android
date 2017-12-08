package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.emojidex.emojidexandroid.downloader.DownloadListener;

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

        downloadHandles.addAll(
                emojidex.update()
        );

        final boolean hasHandle = !downloadHandles.isEmpty();
        if(hasHandle)
        {
            emojidex.addDownloadListener(new CustomDownloadListener());
        }

        return hasHandle;
    }

    /**
     * Get update flag.
     * @return  true when execution update.
     */
    private boolean checkExecUpdate()
    {
        return      checkChangeDeviceLanguage()
                ||  emojidex.getUpdateInfo().isNeedUpdate()
                ||  checkUpdateTime()
                ;
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
        final long lastUpdateTime = emojidex.getUpdateInfo().getLastUpdateTime();
        final long currentTime = System.currentTimeMillis();
        final long updateInterval = Long.parseLong(pref.getString(context.getString(R.string.preference_key_update_interval), context.getString(R.string.preference_entryvalue_update_interval_default)));
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, boolean result)
        {
            finishMethod(handle, "End update.");
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            finishMethod(handle, "Cancel update.");
        }

        private void finishMethod(int handle, String msg)
        {
            if(downloadHandles.remove(handle))
            {
                // End update.
                if(downloadHandles.isEmpty())
                {
                    // Show message.
                    Toast.makeText(context, R.string.ime_message_update_complete, Toast.LENGTH_SHORT).show();

                    // Remove listener.
                    emojidex.removeDownloadListener(this);

                    Log.d(TAG, msg);
                }
            }
        }
    }
}
