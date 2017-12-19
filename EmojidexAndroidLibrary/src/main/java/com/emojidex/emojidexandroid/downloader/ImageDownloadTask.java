package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.ImageLoader;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;

/**
 * Created by kou on 17/08/29.
 */

class ImageDownloadTask extends AbstractFileDownloadTask {
    /**
     * Construct object.
     * @param arguments     Download arguments.
     * @param context       Context.
     */
    public ImageDownloadTask(ImageDownloadArguments arguments, Context context)
    {
        super(arguments, context);
    }

    @Override
    public TaskType getType()
    {
        return TaskType.IMAGE;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if(result)
        {
            final ImageDownloadArguments arguments = (ImageDownloadArguments)getArguments();

            // Update checksums.
            getDownloader().updateChecksums(arguments.getEmojiName(), arguments.getFormat());

            // Reload image.
            ImageLoader.getInstance().reload(arguments.getEmojiName(), arguments.getFormat());

            // Norify to listener.
            getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadImages(getHandle(), arguments.getFormat(), arguments.getEmojiName());
                }
            });
        }

        super.onPostExecute(result);
    }

    @Override
    protected String getRemotePath()
    {
        final ImageDownloadArguments arguments = (ImageDownloadArguments)getArguments();
        return EmojidexFileUtils.getRemoteEmojiPath(arguments.getEmojiName(), arguments.getFormat());
    }

    @Override
    protected Uri getOutputUri()
    {
        final ImageDownloadArguments arguments = (ImageDownloadArguments)getArguments();
        return EmojidexFileUtils.getLocalEmojiUri(arguments.getEmojiName(), arguments.getFormat());
    }
}
