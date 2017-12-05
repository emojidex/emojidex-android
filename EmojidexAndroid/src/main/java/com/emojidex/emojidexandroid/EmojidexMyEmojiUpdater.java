package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.emojidexandroid.downloader.arguments.MyEmojiDownloadArguments;

/**
 * Created by Yoshida on 2017/11/29.
 */

public class EmojidexMyEmojiUpdater {
    static final String TAG = MainActivity.TAG + "::EmojidexMyEmojiUpdater";

    private final Context context;
    private final SaveDataManager myEmojiManager;
    private String username;

    private int downloadHandle = EmojiDownloader.HANDLE_NULL;

    public EmojidexMyEmojiUpdater(Context context, String username)
    {
        this.context = context;
        this.username = username;
        myEmojiManager = SaveDataManager.getInstance(this.context, SaveDataManager.Type.MyEmoji);
    }

    public boolean startUpdateThread(boolean forceFlag)
    {
        if (downloadHandle != EmojiDownloader.HANDLE_NULL || (!forceFlag && !checkExecUpdate())
                || username == null || username.equals(""))
        {
            Log.d(TAG, "Skip my emoji update.");
            return false;
        }

        Log.d(TAG, "Start my emoji update.");

        final EmojiDownloader downloader = EmojiDownloader.getInstance();
        downloadHandle = downloader.downloadMyEmoji(
                new MyEmojiDownloadArguments(username)
        );

        if (downloadHandle != EmojiDownloader.HANDLE_NULL)
        {
            Emojidex.getInstance().addDownloadListener(new EmojidexMyEmojiUpdater.CustomDownloadListener());
            return true;
        }

        return false;
    }

    private boolean checkExecUpdate()
    {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final long lastUpdateTime = pref.getLong(context.getString(R.string.preference_key_last_update_time_my_emoji), 0);
        final long currentTime = System.currentTimeMillis();
        final long updateInterval = 24 * 60 * 60 * 1000;
        return (currentTime - lastUpdateTime) > updateInterval;
    }

    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onDownloadJson(int handle, String... emojiNames)
        {
            if (handle == downloadHandle)
            {
                myEmojiManager.clear();

                for(String emoji : emojiNames)
                    myEmojiManager.addLast(emoji);

                myEmojiManager.save();
            }
        }

        @Override
        public void onFinish(int handle, boolean result)
        {
            if (handle == downloadHandle)
            {
                // Save update time.
                // If emoji download failed, execute force update next time.
                final long updateTime = result ? System.currentTimeMillis() : 0;
                final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                final SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putLong(context.getString(R.string.preference_key_last_update_time_my_emoji), updateTime);
                prefEditor.apply();

                downloadHandle = EmojiDownloader.HANDLE_NULL;
                Emojidex.getInstance().removeDownloadListener(this);

                Log.d(TAG, "End my emoji update.");
            }
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            if (handle == downloadHandle)
            {
                downloadHandle = EmojiDownloader.HANDLE_NULL;
                Emojidex.getInstance().removeDownloadListener(this);

                Log.d(TAG, "Cancel my emoji update.");
            }
        }
    }
}
