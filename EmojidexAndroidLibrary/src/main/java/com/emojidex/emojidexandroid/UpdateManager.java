package com.emojidex.emojidexandroid;

import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;
import com.emojidex.emojidexandroid.downloader.arguments.EmojiDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.ExtendedDownloadArguments;
import com.emojidex.emojidexandroid.downloader.arguments.UTFDownloadArguments;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by kou on 17/11/28.
 */

class UpdateManager {
    private UpdateManager()
    {
        // nop
    }

    /**
     * Update emojidex database.
     * @return      Download handle array.
     */
    public static Collection<Integer> update()
    {
        final Emojidex emojidex = Emojidex.getInstance();
        final EmojiDownloader downloader = emojidex.getEmojiDownloader();
        final Collection<Integer> handles = new LinkedHashSet<Integer>();

        // Add listener.
        emojidex.addDownloadListener(new UpdateListener(handles));

        // UTF
        {
            final int handle = downloader.downloadUTFEmoji(
                    new UTFDownloadArguments()
            );
            if(handle != EmojiDownloader.HANDLE_NULL)
                handles.add(handle);
        }

        // Extended
        {
            final int handle = downloader.downloadExtendedEmoji(
                    new ExtendedDownloadArguments()
            );
            if(handle != EmojiDownloader.HANDLE_NULL)
                handles.add(handle);
        }

        // Other
        {
            final List<Emoji> otherEmojies = emojidex.getOtherEmojiList();
            if(     !otherEmojies.isEmpty()
                    &&  !emojidex.getUTFEmojiList().isEmpty()
                    &&  !emojidex.getExtendedEmojiList().isEmpty()   )
            {
                final int count = otherEmojies.size();
                final EmojiDownloadArguments[] argumentsArray = new EmojiDownloadArguments[count];

                for(int i = 0;  i < count;  ++i)
                {
                    final Emoji emoji = otherEmojies.get(i);
                    argumentsArray[i] = new EmojiDownloadArguments(emoji.getCode());
                }

                final int[] results = downloader.downloadEmojies(argumentsArray);

                for(int handle : results)
                {
                    if(handle != EmojiDownloader.HANDLE_NULL)
                        handles.add(handle);
                }
            }
        }

        return new LinkedHashSet<Integer>(handles);
    }

    /**
     * Download listener for update.
     */
    private static class UpdateListener extends DownloadListener
    {
        private final Collection<Integer> handles;
        private boolean succeeded = true;

        public UpdateListener(Collection<Integer> handles)
        {
            this.handles = handles;
        }

        @Override
        public void onFinish(int handle, boolean result)
        {
            onFinishImpl(handle, result);
        }

        @Override
        public void onCancelled(int handle, boolean result)
        {
            onFinishImpl(handle, false);
        }

        /**
         * Finish event.
         * @param handle    Download handle.
         * @param result    true if download is succeeded.
         */
        private void onFinishImpl(int handle, boolean result)
        {
            // Skip if handle is unknown.
            if( !handles.remove(handle) )
                return;

            // Check result.
            succeeded = (succeeded && result);

            // Skip if handles is not empty.
            if( !handles.isEmpty() )
                return;

            // End update.
            final Emojidex emojidex = Emojidex.getInstance();
            emojidex.getUpdateInfo().update(succeeded);
            emojidex.removeDownloadListener(this);
        }
    }
}
