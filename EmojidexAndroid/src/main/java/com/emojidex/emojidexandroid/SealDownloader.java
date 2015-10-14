package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by kou on 15/04/20.
 */
public class SealDownloader {
    private final Activity parentActivity;

    private ProgressDialog dialog = null;
    private boolean canceled = false;
    private DialogInterface.OnDismissListener onDismissListener = null;

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

        // URL encode.
        try
        {
            name = URLEncoder.encode(name, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        // Download seal.
        final EmojiDownloader downloader = new EmojiDownloader(parentActivity);
        final String url = PathUtils.getAPIRootPath() + "/emoji/" + name + "?detailed=true";
        final EmojiFormat[] formats = {EmojiFormat.toFormat(parentActivity.getString(R.string.emoji_format_seal))};
        downloader.setListener(new CustomDownloadListener());
        downloader.add(url, formats, PathUtils.getRemoteRootPathDefault() + "/emoji");

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
                downloader.cancel();
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
        public void onPostOneJsonDownload(String source, String destination) {
            super.onPostOneJsonDownload(source, destination);

            // Replace whitespace to underbar.
            final File file = new File(destination);
            final ArrayList<JsonParam> emojies = JsonParam.readFromFile(file);
            for(JsonParam emoji : emojies)
            {
                emoji.name = emoji.name.replaceAll(" ", "_");
            }
            JsonParam.writeToFile(file, emojies);
        }

        @Override
        public void onFinish() {
            super.onFinish();

            if(dialog != null)
            {
                dialog.dismiss();
                dialog = null;
            }
        }
    }
}
