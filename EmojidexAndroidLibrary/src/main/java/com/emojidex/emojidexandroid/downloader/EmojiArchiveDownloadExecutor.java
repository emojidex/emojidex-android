package com.emojidex.emojidexandroid.downloader;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroid.EmojiFormat;
import com.emojidex.emojidexandroid.EmojidexFileUtils;
import com.emojidex.emojidexandroid.ImageLoader;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by kou on 17/06/14.
 */

class EmojiArchiveDownloadExecutor extends AbstractFileDownloadExecutor {
    private final String[] emojiNames;

    /**
     * Construct object.
     * @param downloader    Emoji downloader.
     * @param context       Context.
     * @param format        Emoji format.
     * @param parentTask    Parent task.
     * @param emojiNames    Download emoji name array.
     */
    public EmojiArchiveDownloadExecutor(EmojiDownloader downloader, Context context, EmojiFormat format, JsonDownloadTask parentTask, String[] emojiNames)
    {
        super(downloader, context, format.getResolution(), parentTask);

        this.emojiNames = emojiNames;
    }

    @Override
    public int download()
    {
        final int succeeded = super.download();

        if(succeeded > 0)
        {
            // Uncompress.
            uncompress();

            // Update cache.
            ImageLoader.getInstance().reload(
                    EmojiFormat.toFormat(getDescription())
            );

            // Notify to event listener.
            getDownloader().notifyToListener(new EmojiDownloader.NotifyInterface() {
                @Override
                public void notify(DownloadListener listener)
                {
                    listener.onDownloadEmojiArchive(getParentTask().getHandle(), emojiNames);
                }
            });
        }

        return succeeded;
    }

    /**
     * Uncompress emoji archive.
     */
    private void uncompress()
    {
        final byte[] buffer = new byte[4096];
        for(AbstractFileDownloadExecutor.DownloadInfo info : getDownloadInfos())
        {
            final File xzFile = new File(info.dest.getPath());

            try
            {
                // xz -> tar
                XZCompressorInputStream xzIn = new XZCompressorInputStream(
                        new BufferedInputStream(
                                new FileInputStream(xzFile)
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
                final EmojiFormat format = EmojiFormat.toFormat(getDescription());
                final TarArchiveInputStream tarIn = new TarArchiveInputStream(
                        new BufferedInputStream(
                                new FileInputStream(tarFile)
                        )
                );

                for(TarArchiveEntry entry = tarIn.getNextTarEntry(); entry != null; entry = tarIn.getNextTarEntry())
                {
                    String basename = entry.getName();
                    final int extPos = basename.lastIndexOf('.');
                    if(extPos != -1)
                        basename = basename.substring(0, extPos);
                    final Uri destUri = EmojidexFileUtils.getLocalEmojiUri(basename, format);

                    OutputStream os = getContext().getContentResolver().openOutputStream(destUri);
                    IOUtils.copy(tarIn, os);
                    os.close();
                }

                tarFile.delete();
            } catch(Exception e)
            {
                e.printStackTrace();
            }

            xzFile.delete();
        }
    }
}
