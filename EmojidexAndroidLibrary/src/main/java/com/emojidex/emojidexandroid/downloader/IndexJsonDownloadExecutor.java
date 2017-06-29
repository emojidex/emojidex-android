package com.emojidex.emojidexandroid.downloader;

import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by kou on 17/06/06.
 */

class IndexJsonDownloadExecutor extends AbstractJsonDownloadExecutor {
    private final int limit;
    private final int startPage;
    private final int endPage;

    public IndexJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask, int limit, int startPage, int endPage)
    {
        super(downloader, config, parentTask);

        this.limit = limit;
        this.startPage = startPage;
        this.endPage = endPage;
    }

    @Override
    protected Collection downloadJson()
    {
        final Client client = createClient();
        final Collection collection = client.getIndexes().emoji(startPage, limit, true);
        for(int i = startPage + 1;  i < (endPage + 1);  ++i)
            collection.more();
        return collection;
    }
}
