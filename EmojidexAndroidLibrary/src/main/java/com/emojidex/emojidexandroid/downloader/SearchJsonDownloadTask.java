package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.SearchDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Service.QueryOpts;

/**
 * Created by kou on 17/08/29.
 */

class SearchJsonDownloadTask extends AbstractJsonDownloadTask {
    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public SearchJsonDownloadTask(SearchDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.SEARCH;
    }

    @Override
    protected Collection downloadJson()
    {
        final SearchDownloadArguments arguments = (SearchDownloadArguments)getArguments();
        final Client client = createEmojidexClient();
        final QueryOpts options = new QueryOpts()
                .detailed(true)
                .limit(0x7FFFFFFF)
                ;
        if(arguments.getCategory() != null)
            options.category(arguments.getCategory());
        return client.getSearch().term(arguments.getWord(), options);
    }
}
