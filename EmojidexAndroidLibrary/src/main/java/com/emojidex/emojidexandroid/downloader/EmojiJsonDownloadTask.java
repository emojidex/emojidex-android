package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;
import com.emojidex.libemojidex.Emojidex.Data.Emoji;

/**
 * Created by kou on 17/08/29.
 */

class EmojiJsonDownloadTask extends AbstractJsonDownloadTask{
    /**
     * Construct object.
     * @param arguments     Download arguments.
     */
    public EmojiJsonDownloadTask(EmojiDownloadArguments arguments)
    {
        super(arguments);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.EMOJI;
    }

    @Override
    protected Collection downloadJson()
    {
        final EmojiDownloadArguments arguments = (EmojiDownloadArguments)getArguments();
        final Client client = createEmojidexClient(arguments.getUserName(), arguments.getAuthToken());
        final Emoji emoji = client.getSearch().find(arguments.getEmojiName(), true);
        final Collection collection = new Collection();
        if( !emoji.getCode().isEmpty() )
            collection.add(emoji);
        return collection;
    }
}
