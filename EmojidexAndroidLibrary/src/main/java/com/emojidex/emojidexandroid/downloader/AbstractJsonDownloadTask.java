package com.emojidex.emojidexandroid.downloader;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.downloader.arguments.AbstractJsonDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ArgumentsInterface;
import com.emojidex.emojidexandroid.downloader.arguments.ImageDownloadArguments;
import com.emojidex.libemojidex.EmojiVector;
import com.emojidex.libemojidex.Emojidex.Client;
import com.emojidex.libemojidex.Emojidex.Data.Collection;

import java.util.ArrayList;

/**
 * Json download task.
 */
abstract class AbstractJsonDownloadTask extends AbstractDownloadTask
{
    private Collection collection = null;

    /**
     * Construct json download task.
     * @param arguments     Download arguments.
     */
    public AbstractJsonDownloadTask(ArgumentsInterface arguments)
    {
        super(arguments);
    }

    /**
     * Create emojidex client.
     * @param username      User name.
     * @param authtoken     Auth token.
     * @return              Emojidex client.
     */
    protected Client createEmojidexClient(String username, String authtoken)
    {
        // Create client.
        final Client client = new Client();

        // Login if has username and auth_token.
        if(     username != null && !username.isEmpty()
            &&  authtoken != null && !authtoken.isEmpty()   )
        {
            client.getUser().authorize(username, authtoken);
        }

        return client;
    }

    @Override
    protected boolean download()
    {
        // Download json.
        collection = downloadJson();

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if(result)
        {
            final EmojiDownloader downloader = getDownloader();
            final EmojiVector emojies = collection.all();

            // Update local json.
            downloader.updateDatabase(getHandle(), emojies, getType());

            // Notify to listener.
            final String[] emojiNames = new String[(int)emojies.size()];
            for(int i = 0;  i < emojies.size();  ++i)
                emojiNames[i] = emojies.get(i).getCode();
            downloader.notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadJson(getHandle(), emojiNames);
                }
            });

            // Start download emoji images.
            downloadImages(emojies);
        }
        super.onPostExecute(result);
    }

    private void downloadImages(EmojiVector emojies)
    {
        // Skip if emojies is empty.
        if(emojies.isEmpty())
            return;

        // Skip if formats is empty.
        final AbstractJsonDownloadArguments arguments = (AbstractJsonDownloadArguments)getArguments();
        final java.util.Collection<EmojiFormat> formats = arguments.getFormats();
        if(formats.isEmpty())
            return;

        // Create image download arguments array.
        final java.util.Collection<ImageDownloadArguments> imageDownloadArgumentsArray = new ArrayList<ImageDownloadArguments>();
        for(int i = 0;  i < emojies.size();  ++i)
        {
            final String emojiName = emojies.get(i).getCode();
            for(EmojiFormat format : formats)
            {
                imageDownloadArgumentsArray.add(
                        new ImageDownloadArguments(emojiName)
                                .setFormat(format)
                );
            }
        }

        // Download images.
        getDownloader().downloadImages(
                    imageDownloadArgumentsArray.toArray(new ImageDownloadArguments[]{})
        );
    }

    /**
     * Download json.
     * @return          Json parameters.
     */
    protected abstract Collection downloadJson();
}
