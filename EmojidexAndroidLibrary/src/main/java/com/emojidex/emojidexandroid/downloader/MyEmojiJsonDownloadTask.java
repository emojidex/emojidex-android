package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.MyEmojiDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

/**
 * Created by Yoshida on 2017/11/29.
 */

class MyEmojiJsonDownloadTask extends AbstractJsonDownloadTask {
    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public MyEmojiJsonDownloadTask(MyEmojiDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.MY_EMOJI;
    }

    @Override
    protected Collection downloadJson()
    {
        final MyEmojiDownloadArguments arguments = (MyEmojiDownloadArguments)getArguments();
        final Client client = createEmojidexClient();

        final Collection collection = client.getIndexes().userEmoji(arguments.getUsername(), arguments.getStartPage(),
                                                                    arguments.getLimit(), true);
        while(collection.all().size() < collection.getTotal_count())
            collection.more();
        return collection;
    }
}
