package com.emojidex.emojidexandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * Created by kou on 15/03/23.
 */
class EmojidexUpdater {
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
     * @return  false when now updating.
     */
    public boolean startUpdateThread()
    {
        final LinkedHashSet<EmojiFormat> formats = new LinkedHashSet<EmojiFormat>();
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_key)));
        formats.add(EmojiFormat.toFormat(context.getString(R.string.emoji_format_seal)));
        emojidex.download(formats.toArray(new EmojiFormat[formats.size()]), new CustomDownloadListener());
        return true;
    }


    /**
     * Custom download listener.
     */
    class CustomDownloadListener extends DownloadListener {
        @Override
        public void onPostAllJsonDownload(EmojiDownloader downloader) {
            super.onPostAllJsonDownload(downloader);
        }

        @Override
        public void onPreAllEmojiDownload() {
            emojidex.reload();

            if(EmojidexIME.currentInstance != null)
                EmojidexIME.currentInstance.reloadCategory();
        }

        @Override
        public void onPostOneEmojiDownload(String emojiName) {
            final Emoji emoji = emojidex.getEmoji(emojiName);
            if(emoji != null)
            {
                emoji.reloadImage();

                if(EmojidexIME.currentInstance != null)
                    EmojidexIME.currentInstance.invalidate(emojiName);
            }
        }

        @Override
        public void onPostAllEmojiDownload() {
            super.onPostAllEmojiDownload();

            // Save update time.
            final long updateTime = new Date().getTime();
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putLong(context.getString(R.string.preference_key_last_update_time), updateTime);
            prefEditor.commit();
        }
    }
}
