package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.UTFDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by kou on 17/08/28.
 */

class UTFJsonDownloadTask extends AbstractJsonDownloadTask {
    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public UTFJsonDownloadTask(UTFDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.UTF;
    }

    @Override
    protected Collection downloadJson()
    {
        final UTFDownloadArguments arguments = (UTFDownloadArguments)getArguments();
        final Client client = createEmojidexClient();
        return client.getIndexes().utfEmoji(arguments.getLocale().getLocale(), true);
    }
}
