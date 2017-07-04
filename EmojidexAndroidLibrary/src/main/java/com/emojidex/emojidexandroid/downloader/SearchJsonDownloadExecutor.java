package com.emojidex.emojidexandroid.downloader;

import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Service.QueryOpts;

/**
 * Created by kou on 17/06/06.
 */

class SearchJsonDownloadExecutor extends AbstractJsonDownloadExecutor {
    private final String word;
    private final String category;

    public SearchJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask, String word, String category)
    {
        super(downloader, config, parentTask);

        this.word = word;
        this.category = category;
    }

    @Override
    protected Collection downloadJson()
    {
        final Client client = createClient();
        final QueryOpts options = new QueryOpts();
        options.detailed(true);
        options.limit(0x7FFFFFFF);
        if(category != null)
            options.category(category);
        return client.getSearch().term(word, options);
    }
}
