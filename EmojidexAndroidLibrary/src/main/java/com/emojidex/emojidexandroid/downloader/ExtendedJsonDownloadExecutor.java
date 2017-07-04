package com.emojidex.emojidexandroid.downloader;

import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by kou on 17/06/06.
 */

class ExtendedJsonDownloadExecutor extends AbstractJsonDownloadExecutor {
    public ExtendedJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask)
    {
        super(downloader, config, parentTask);
    }

    @Override
    protected Collection downloadJson()
    {
        final Client client = createClient();
        final String locale = getConfig().getLocale();
        return client.getIndexes().extendedEmoji(locale, true);
    }
}
