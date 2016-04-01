package com.emojidex.emojidexandroid;

import android.content.Context;

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

    public void startUpdateThread()
    {
        final EmojiDownloader downloader = new EmojiDownloader();
        final DownloadConfig config = new DownloadConfig(
                EmojiFormat.toFormat(context.getString(R.string.emoji_format_default)),
                EmojiFormat.toFormat(context.getString(R.string.emoji_format_key))
        );
        downloader.setListener(new CustomDownloadListener());
        downloader.downloadIndex(config);
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
    }
}
