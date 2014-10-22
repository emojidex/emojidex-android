package com.emojidex.emojidexandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * Created by kou on 14/05/19.
 */
public class ProxyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set shared parameter.
        final Intent intent = (Intent)getIntent().getParcelableExtra(Intent.EXTRA_INTENT);

        // Check.
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        // Error.
        if( resolveInfos.isEmpty() )
        {
            Log.d("ime", "Proxy: Intent send failed.");

            // Create error dialog.
            // TODO Set error detail.
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error.");
            builder.setMessage("Error.");
            builder.setPositiveButton(R.string.close, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Close proxy activity if closed error dialog.
                    ProxyActivity.this.finish();
                }
            });

            // Show dialog.
            builder.create().show();
        }
        // Send shared parameter.
        else
        {
            startActivity(intent);
            Log.d("ime", "Proxy: Intent send succeeded.");

            // Close proxy activity.
            finish();
        }
    }
}
