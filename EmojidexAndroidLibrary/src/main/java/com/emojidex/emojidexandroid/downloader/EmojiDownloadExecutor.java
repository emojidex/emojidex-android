package com.emojidex.emojidexandroid.downloader;

import android.content.Context;

import com.emojidex.emojidexandroid.ImageLoader;

/**
 * Created by kou on 17/06/13.
 */

class EmojiDownloadExecutor extends AbstractFileDownloadExecutor {
    /**
     * Construct object.
     * @param downloader    Emoji downloader.
     * @param context       Context.
     * @param emojiName     Emoji name.
     * @param parentTask    Parent task.
     */
    public EmojiDownloadExecutor(EmojiDownloader downloader, Context context, String emojiName, JsonDownloadTask parentTask)
    {
        super(downloader, context, emojiName, parentTask);
    }

    @Override
    public int download()
    {
        int succeeded = super.download();

        if(succeeded > 0)
        {
            final String emojiName = getDescription();

            // Update image cache.
            ImageLoader.getInstance().reload(emojiName);

            // Notify to event listener.
            getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadEmoji(getParentTask().getHandle(), emojiName);
                }
            });
        }

        return succeeded;
    }
}
