package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by kou on 14/05/19.
 */
public class SendSealActivity extends Activity {
    static final String TAG = MainActivity.TAG + "::SendSealActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create intent.
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final String targetPackageName = am.getRunningTasks(2).get(1).baseActivity.getPackageName();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.setPackage(targetPackageName);

        // Check.
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // Error.
        if( resolveInfos.isEmpty() )
        {
            Log.d(TAG, "Proxy: Intent send failed.(Target package name = " + intent.getPackage() + ")");

            // Create error dialog.
            // TODO Set error detail.
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage(getString(R.string.message_unsupported_seal));
            builder.setPositiveButton(R.string.close, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Close proxy activity if closed error dialog.
                    SendSealActivity.this.finish();
                }
            });

            // Show dialog.
            builder.create().show();
        }
        // Send shared parameter.
        else
        {
            final String emojiName = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            intent.putExtra(Intent.EXTRA_STREAM, getURI(emojiName));
            startActivity(intent);
            Log.d(TAG, "Proxy: Intent send succeeded.");

            // Close proxy activity.
            finish();
        }
    }

    /**
     * Get URI of send emoji file.
     * @param emojiName     Emoji name.
     * @return              URI of send emoji file.
     */
    private Uri getURI(String emojiName)
    {
        final Emoji emoji = Emojidex.getInstance().getEmoji(emojiName);
        final String formatName = getString(R.string.emoji_format_seal);
        final EmojiFormat format = EmojiFormat.toFormat(formatName);
        final File file = new File(emoji.getImageFilePath(format));
        final File temporaryFile = new File(getExternalCacheDir(), "tmp" + System.currentTimeMillis() + ".png");

        // Create temporary file.
        try
        {
            // Load bitmap.
            final FileInputStream is = new FileInputStream(file);
            final Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            // Change background color to white.
            final Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            newBitmap.eraseColor(Color.WHITE);
            final Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Save temporary file.
            final FileOutputStream os = new FileOutputStream(temporaryFile);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return Uri.fromFile(temporaryFile);
    }
}
