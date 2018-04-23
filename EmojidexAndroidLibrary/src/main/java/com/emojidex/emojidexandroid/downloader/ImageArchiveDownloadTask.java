package com.emojidex.emojidexandroid.downloader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.ImageLoader;
import com.emojidex.emojidexandroid.downloader.arguments.ImageArchiveDownloadArguments;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by kou on 17/12/05.
 */

class ImageArchiveDownloadTask extends AbstractFileDownloadTask {
    private static final int BUFFER_SIZE = 4096;

    private final Uri outputUri;

    private final ArrayList<String> emojiNames = new ArrayList<String>();

    /**
     * Construct object.
     * @param arguments     Download arguments.
     * @param context       Context.
     */
    public ImageArchiveDownloadTask(ImageArchiveDownloadArguments arguments, Context context)
    {
        super(arguments, context);

        outputUri = Uri.parse("file:" + EmojidexFileUtils.getTemporaryPath());
    }

    @Override
    public TaskType getType()
    {
        return TaskType.IMAGE_ARCHIVE;
    }

    @Override
    protected boolean download()
    {
        final boolean result = super.download();
        if(result)
            uncompress();
        return result;
    }

    @Override
    protected String getRemotePath()
    {
        final ImageArchiveDownloadArguments arguments = (ImageArchiveDownloadArguments)getArguments();
        return EmojidexFileUtils.getRemoteEmojiArchivePath(arguments.getFormat());
    }

    @Override
    protected Uri getOutputUri()
    {
        return outputUri;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if(result)
        {
            final EmojiFormat format = ((ImageArchiveDownloadArguments)getArguments()).getFormat();

            // Reload image.
            ImageLoader.getInstance().reload(format);

            // Notify to listener.
            getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadImages(getHandle(), format, emojiNames.toArray(new String[emojiNames.size()]));
                }
            });
        }

        super.onPostExecute(result);
    }

    private void uncompress()
    {
        final ImageArchiveDownloadArguments arguments = (ImageArchiveDownloadArguments)getArguments();
        final ContentResolver cr = getContext().getContentResolver();

        try
        {
            // xz -> tar
            final XZCompressorInputStream xzIn = new XZCompressorInputStream(
                    cr.openInputStream(outputUri)
            );
            final File tarFile = new File(EmojidexFileUtils.getTemporaryPath());
            final FileOutputStream tarOut = new FileOutputStream(tarFile);

            final byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n = xzIn.read(buffer)) != -1)
                tarOut.write(buffer, 0, n);

            tarOut.close();
            xzIn.close();
            EmojidexFileUtils.deleteFiles(outputUri);

            // tar -> files
            final EmojiFormat format = arguments.getFormat();
            final TarArchiveInputStream tarIn = new TarArchiveInputStream(
                    new FileInputStream(tarFile)
            );

            for(TarArchiveEntry entry = tarIn.getNextTarEntry(); entry != null; entry = tarIn.getNextTarEntry())
            {
                String emojiName = new File(entry.getName()).getName();
                final int extPos = emojiName.lastIndexOf('.');
                if(extPos != -1)
                    emojiName = emojiName.substring(0, extPos);
                final Uri destUri = EmojidexFileUtils.getLocalEmojiUri(emojiName, format);

                final OutputStream os = cr.openOutputStream(destUri);
                IOUtils.copy(tarIn, os);
                os.close();

                // Update checksums.
                getDownloader().updateChecksums(emojiName, format);

                // Add to list.
                emojiNames.add(emojiName);
            }

            tarIn.close();
            tarFile.delete();
        } catch(Exception e)
        {
            EmojidexFileUtils.deleteFiles(outputUri);
        }
    }
}
