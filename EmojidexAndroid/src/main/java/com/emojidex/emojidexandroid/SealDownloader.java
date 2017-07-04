package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.emojidex.emojidexandroid.downloader.DownloadConfig;
import com.emojidex.emojidexandroid.downloader.DownloadListener;
import com.emojidex.emojidexandroid.downloader.EmojiDownloader;

/**
 * Created by kou on 15/04/20.
 */
public class SealDownloader {
    private final Activity parentActivity;

    private ProgressDialog dialog = null;
    private boolean canceled = false;
    private DialogInterface.OnDismissListener onDismissListener = null;

    private int downloadHandle;

    /**
     * Construct object.
     * @param activity  Parent activity.
     */
    public SealDownloader(Activity activity)
    {
        parentActivity = activity;
    }

    /**
     * Download seal.
     * @param name  Emoji name.
     */
    public void download(String name)
    {
        canceled = false;

        // Download seal.
        final UserData userdata = UserData.getInstance();
        final DownloadConfig config =
                new DownloadConfig()
                        .addFormat(EmojiFormat.toFormat(parentActivity.getString(R.string.emoji_format_seal)))
                        .setUser(userdata.getUsername(), userdata.getAuthToken())
                ;

        final EmojiDownloader downloader = EmojiDownloader.getInstance();
        downloadHandle = downloader.downloadEmoji(name, config);

        if(downloadHandle == EmojiDownloader.HANDLE_NULL)
            return;

        Emojidex.getInstance().addDownloadListener(new CustomDownloadListener());

        // Show progress dialog.
        dialog = new ProgressDialog(parentActivity);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.send_seal_dialog_title);
        dialog.setMessage(parentActivity.getString(R.string.send_seal_dialog_message));

        dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                parentActivity.getString(R.string.send_seal_dialog_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloader.cancelDownload(downloadHandle);
                canceled = true;
            }
        });

        dialog.setOnDismissListener(onDismissListener);

        dialog.show();
    }

    /**
     * Set listener called when downloader finish.
     * @param listener  OnDismissListener.
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener listener)
    {
        onDismissListener = listener;
    }

    /**
     * Check download is canceled.
     * @return  true when download is canceled.
     */
    public boolean isCanceled()
    {
        return canceled;
    }


    /**
     * Custom download listener.
     */
    private class CustomDownloadListener extends DownloadListener
    {
        @Override
        public void onFinish(int handle, EmojiDownloader.Result result)
        {
            if(handle == downloadHandle)
            {
                if(dialog != null)
                {
                    dialog.dismiss();
                    dialog = null;
                }

                Emojidex.getInstance().removeDownloadListener(this);
            }
        }

        @Override
        public void onCancelled(int handle, EmojiDownloader.Result result)
        {
            if(handle == downloadHandle)
            {
                Emojidex.getInstance().removeDownloadListener(this);
            }
        }
    }
}
