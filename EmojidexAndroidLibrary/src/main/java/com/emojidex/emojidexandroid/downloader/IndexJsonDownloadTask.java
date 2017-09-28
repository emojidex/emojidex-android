package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.IndexDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by kou on 17/08/29.
 */

class IndexJsonDownloadTask extends AbstractJsonDownloadTask{

    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public IndexJsonDownloadTask(IndexDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.INDEX;
    }

    @Override
    protected Collection downloadJson()
    {
        final IndexDownloadArguments arguments = (IndexDownloadArguments)getArguments();
        final Client client = createEmojidexClient();
        final Collection collection = client.getIndexes().emoji(arguments.getStartPage(), arguments.getLimit(), true);
        for(int i = arguments.getStartPage();  i < arguments.getEndPage();  ++i)
            collection.more();
        return collection;
    }
}
