package com.emojidex.emojidexandroid.downloader;

import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;

/**
 * Created by kou on 17/06/06.
 */

class EmojiJsonDownloadExecutor extends AbstractJsonDownloadExecutor {
    private final String name;

    public EmojiJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask, String name)
    {
        super(downloader, config, parentTask);

        this.name = name;
    }

    @Override
    protected Collection downloadJson()
    {
        final Client client = createClient();
        final Emoji emoji = client.getSearch().find(name, true);
        final Collection collection = new Collection();
        collection.add(emoji);
        return collection;
    }
}
