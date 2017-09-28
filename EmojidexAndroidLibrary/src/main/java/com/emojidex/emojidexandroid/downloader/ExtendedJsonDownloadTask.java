package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by kou on 17/08/29.
 */

class ExtendedJsonDownloadTask extends AbstractJsonDownloadTask{
    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public ExtendedJsonDownloadTask(ExtendedDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.EXTENDED;
    }

    @Override
    protected Collection downloadJson()
    {
        final ExtendedDownloadArguments arguments = (ExtendedDownloadArguments)getArguments();
        final Client client = createEmojidexClient();
        return client.getIndexes().extendedEmoji(arguments.getLocale().getLocale(), true);
    }
}
