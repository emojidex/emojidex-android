package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojidexLocale;
import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Service.User;

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
        final User user = client.getUser();

        // When logined and premium or pro user.
        if(     user.getStatus().equals(User.AuthStatusCode.VERIFIED)
            &&  (user.getPremium() || user.getPro())  )
        {
            final String target = arguments.getLocale().equals(EmojidexLocale.JAPANESE) ? "絵文字デックス" : "emojidex";
            final Collection collection = client.getIndexes().userEmoji(target, 1, 0x7FFFFFFF, true);
            while(collection.all().size() < collection.getTotal_count())
                collection.more();
            return collection;
        }

        return client.getIndexes().extendedEmoji(arguments.getLocale().getLocale(), true);
    }
}
