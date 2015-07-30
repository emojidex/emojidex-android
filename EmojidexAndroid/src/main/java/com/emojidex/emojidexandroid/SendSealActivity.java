package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * Created by kou on 14/05/19.
 */
public class SendSealActivity extends Activity {
    static final String TAG = MainActivity.TAG + "::SendSealActivity";

    private final Intent sendIntent = new Intent(Intent.ACTION_SEND);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize intent.
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final String targetPackageName = am.getRunningTasks(2).get(1).baseActivity.getPackageName();
        sendIntent.setType("image/png");
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        sendIntent.setPackage(targetPackageName);

        // Error check.
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if(resolveInfos.isEmpty())
        {
            Log.d(TAG, "Intent send failed.(Target package name = " + sendIntent.getPackage() + ")");

            // Create error dialog.
            // TODO Set error detail.
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage(R.string.message_unsupported_seal);
            builder.setPositiveButton(R.string.close, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Close activity if closed error dialog.
                    SendSealActivity.this.finish();
                }
            });

            // Show dialog.
            builder.create().show();

            return;
        }

        // Send seal.
        sendSeal();
    }

    /**
     * Send seal.
     */
    private void sendSeal()
    {
        final String emojiName = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        // Download seal.
        final SealDownloader downloader = new SealDownloader(this);

        downloader.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(downloader.isCanceled())
                {
                    Log.d(TAG, "Intent send canceled.(Target package name = " + sendIntent.getPackage() + ")");
                    finish();
                    return;
                }

                // Generate seal.
                final SealGenerator generator = new SealGenerator(SendSealActivity.this);
                generator.generate(emojiName);

                // If use low quality image, show dialog.
                if(generator.useLowQuality())
                {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SendSealActivity.this);
                    alertDialog.setMessage(R.string.send_seal_not_found);
                    alertDialog.setPositiveButton(R.string.send_seal_not_found_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            sendIntent(generator.getUri());
                            finish();
                        }
                    });
                    alertDialog.show();

                    return;
                }

                // Send intent and finish.
                sendIntent(generator.getUri());
                finish();
            }
        });

        downloader.download(emojiName);
    }

    /**
     * Send intent.
     */
    private void sendIntent(Uri uri) {
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(sendIntent);
        Log.d(TAG, "Intent send succeeded.(Target package name = " + sendIntent.getPackage() + ")");
    }
}
