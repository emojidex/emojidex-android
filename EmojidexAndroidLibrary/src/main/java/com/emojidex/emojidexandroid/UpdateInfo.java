package com.emojidex.emojidexandroid;

import android.content.Context;
import android.net.Uri;

import com.emojidex.emojidexandroidlibrary.BuildConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Created by kou on 17/11/27.
 */

public class UpdateInfo {

    private static final String FILENAME = "update_info.json";
    private static final String OLD_FILENAME = "emojidex_lib_version";

    private int lastUpdateVersionCode;
    private String lastUpdateVersionName;
    private long lastUpdateTime;
    private boolean lastUpdateSucceeded;

    private boolean dirty = false;

    /**
     * Construct object.
     */
    void UpdateInfo()
    {
        reset();
    }

    /**
     * Reset update information.
     */
    void reset()
    {
        lastUpdateVersionCode = 0;
        lastUpdateVersionName = "";
        lastUpdateTime = 0;
        lastUpdateSucceeded = false;
        dirty = false;
    }

    /**
     * Get library version code of last update.
     * @return      Library version code.
     */
    public int getLastUpdateVersionCode()
    {
        return lastUpdateVersionCode;
    }

    /**
     * Get library version name of last update.
     * @return      Library version name.
     */
    public String getLastUpdateVersionName()
    {
        return lastUpdateVersionName;
    }

    /**
     * Get last update time.
     * @return      Last update time.
     */
    public long getLastUpdateTime()
    {
        return lastUpdateTime;
    }

    /**
     * Get last update succeeded flag.
     * @return      true if last update succeeded.
     */
    public boolean isLastUpdateSucceeded()
    {
        return lastUpdateSucceeded;
    }

    /**
     * Get need update flag.
     * @return      true if need update.
     */
    @JsonIgnore
    public boolean isNeedUpdate()
    {
        return      !lastUpdateSucceeded
                ||  lastUpdateTime <= 0
                ;
    }

    /**
     * Update parameter.
     * @param succeeded     Succeeded flag.
     */
    void update(boolean succeeded)
    {
        lastUpdateVersionCode = BuildConfig.VERSION_CODE;
        lastUpdateVersionName = BuildConfig.VERSION_NAME;
        lastUpdateTime = System.currentTimeMillis();
        lastUpdateSucceeded = succeeded;
        dirty = true;
    }

    /**
     * Load update info.
     * @param context   Context.
     */
    void load(Context context)
    {
        final Uri uri = EmojidexFileUtils.getLocalFileUri(FILENAME);

        // If found update info file, load from file.
        if(EmojidexFileUtils.existsLocalFile(uri))
        {
            final UpdateInfo ui = EmojidexFileUtils.readJsonFromFile(uri, UpdateInfo.class);
            lastUpdateVersionCode = ui.getLastUpdateVersionCode();
            lastUpdateVersionName = ui.getLastUpdateVersionName();
            lastUpdateTime = ui.getLastUpdateTime();
            lastUpdateSucceeded = ui.isLastUpdateSucceeded();
            dirty = false;
            return;
        }

        // If found old system file, load from old system file..
        final Uri oldUri = EmojidexFileUtils.getLocalFileUri(OLD_FILENAME);
        if(EmojidexFileUtils.existsLocalFile(oldUri))
        {
            lastUpdateVersionCode = loadVersionCodeFromOldSystem(context, oldUri);
            lastUpdateVersionName = "";
            lastUpdateTime = System.currentTimeMillis();
            lastUpdateSucceeded = true;
            dirty = true;
            save();
            EmojidexFileUtils.deleteFiles(oldUri);
            return;
        }

        // Initialize parameter.
        reset();
    }

    /**
     * Save update info.
     */
    void save()
    {
        if(!dirty)
            return;

        EmojidexFileUtils.writeJsonToFile(
                EmojidexFileUtils.getLocalFileUri(FILENAME),
                this
        );

        dirty = false;
    }

    /**
     * Get library version code from old system.
     * @param context   Context.
     * @param uri       Old file uri.
     * @return          Library version code of last update.
     */
    private int loadVersionCodeFromOldSystem(Context context, Uri uri)
    {
        int result;

        try
        {
            String version = "";
            final InputStream is = context.getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[16];
            int readByte;
            while( (readByte = is.read(buffer)) != -1 )
                version += new String(buffer, 0, readByte);
            is.close();
            result = NumberFormat.getInstance().parse(version).intValue();
        }
        catch(IOException e)
        {
            result = 0;
        }
        catch(ParseException e)
        {
            result = 0;
        }
        return result;
    }
}
