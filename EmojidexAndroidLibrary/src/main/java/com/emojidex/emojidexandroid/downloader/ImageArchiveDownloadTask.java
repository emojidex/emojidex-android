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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by kou on 17/12/05.
 */

public class ImageArchiveDownloadTask extends AbstractFileDownloadTask {
    private static final int BUFFER_SIZE = 4096;

    private final Context context;
    private final Uri temporaryUri;

    private final ArrayList<String> emojiNames = new ArrayList<String>();

    /**
     * Construct object.
     * @param arguments     Download arguments.
     * @param context       Context.
     */
    public ImageArchiveDownloadTask(ImageArchiveDownloadArguments arguments, Context context)
    {
        super(arguments, context);
        this.context = context;
        temporaryUri = Uri.parse("file:" + EmojidexFileUtils.getTemporaryPath());
    }

    @Override
    public TaskType getType()
    {
        return TaskType.IMAGE_ARCHIVE;
    }

    @Override
    protected String getRemotePath()
    {
        final ImageArchiveDownloadArguments arguments = (ImageArchiveDownloadArguments)getArguments();
        return EmojidexFileUtils.getRemoteEmojiArchivePath(arguments.getFormat());
    }

    @Override
    protected Uri getLocalUri()
    {
        return temporaryUri;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        if(result)
        {
            final EmojiFormat format = ((ImageArchiveDownloadArguments)getArguments()).getFormat();

            // Uncompress archive file.
            uncompress();

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
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ContentResolver cr = context.getContentResolver();

        try
        {
            // xz -> tar
            final XZCompressorInputStream xzIn = new XZCompressorInputStream(
                    new BufferedInputStream(
                            cr.openInputStream(temporaryUri)
                    )
            );
            final File tarFile = new File(EmojidexFileUtils.getTemporaryPath());
            final FileOutputStream tarOut = new FileOutputStream(tarFile);

            int n;
            while((n = xzIn.read(buffer)) != -1)
                tarOut.write(buffer, 0, n);

            tarOut.close();
            xzIn.close();

            // tar -> files
            final EmojiFormat format = arguments.getFormat();
            final TarArchiveInputStream tarIn = new TarArchiveInputStream(
                    new BufferedInputStream(
                            new FileInputStream(tarFile)
                    )
            );

            for(TarArchiveEntry entry = tarIn.getNextTarEntry(); entry != null; entry = tarIn.getNextTarEntry())
            {
                String emojiName = entry.getName();
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

            tarFile.delete();
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        EmojidexFileUtils.deleteFiles(temporaryUri);
    }
}
