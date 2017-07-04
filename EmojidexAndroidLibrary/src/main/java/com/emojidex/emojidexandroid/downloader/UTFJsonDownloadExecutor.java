package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kou on 17/06/06.
 */

class UTFJsonDownloadExecutor extends AbstractJsonDownloadExecutor {
    public UTFJsonDownloadExecutor(EmojiDownloader downloader, DownloadConfig config, JsonDownloadTask parentTask)
    {
        super(downloader, config.clone(), parentTask);
    }

    @Override
    protected List<FileDownloadTask> createEmojiDownloadTasks(EmojiVector emojies)
    {
        final ArrayList<FileDownloadTask> result = new ArrayList<FileDownloadTask>();
        final DownloadConfig config = getConfig();
        final List<EmojiFormat> downloadArchiveFormats = new LinkedList<EmojiFormat>();

        Iterator<EmojiFormat> it = config.getFormats().iterator();
        while(it.hasNext())
        {
            final EmojiFormat format = it.next();
            if( !EmojidexFileUtils.existsLocalEmojiFormatDirectory(format) )
            {
                it.remove();
                downloadArchiveFormats.add(format);
            }
        }

        EmojiArchiveDownloadExecutor[] executors = getDownloader().createEmojiArchiveDownloadExecutors(emojies, downloadArchiveFormats, config, getParentTask());
        if(executors != null && executors.length > 0)
        {
            final FileDownloadTask task = new FileDownloadTask(getDownloader(), getListener());
            for(EmojiArchiveDownloadExecutor executor : executors)
                task.add(executor);
            result.add(task);
        }

        result.addAll(super.createEmojiDownloadTasks(emojies));

        return result;
    }

    @Override
    protected Collection downloadJson()
    {
        final DownloadConfig config = getConfig();
        final Client client = createClient();
        final String locale = config.getLocale();
        return client.getIndexes().utfEmoji(locale, true);
    }
}
